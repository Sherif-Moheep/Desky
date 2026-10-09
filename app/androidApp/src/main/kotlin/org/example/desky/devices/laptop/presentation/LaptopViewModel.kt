package org.example.desky.devices.laptop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.devices.esp.domain.usecase.EspRelayUseCases
import org.example.desky.devices.laptop.domain.usecase.LaptopUseCases
import org.example.desky.devices.laptop.presentation.contract.LaptopEffect
import org.example.desky.devices.laptop.presentation.contract.LaptopIntent
import org.example.desky.devices.laptop.presentation.contract.LaptopState
import kotlin.time.Duration.Companion.milliseconds

class LaptopViewModel(
    private val useCases: LaptopUseCases,
    private val espUseCases: EspRelayUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(LaptopState())
    val state: StateFlow<LaptopState> = _state.asStateFlow()

    private val _effect = Channel<LaptopEffect>(Channel.BUFFERED)
    val effect: Flow<LaptopEffect> = _effect.receiveAsFlow()

    init {
        handleRefreshStatus(showToast = false)
    }

    fun sendIntent(intent: LaptopIntent) {
        when (intent) {
            is LaptopIntent.PowerClick -> handlePowerClick()
            is LaptopIntent.RequestShutdown -> handleRequestShutdown()
            is LaptopIntent.ConfirmShutdown -> handleConfirmShutdown()
            is LaptopIntent.DismissShutdownDialog -> handleDismissShutdownDialog()
            is LaptopIntent.RequestRestart -> handleRequestRestart()
            is LaptopIntent.ConfirmRestart -> handleConfirmRestart()
            is LaptopIntent.DismissRestartDialog -> handleDismissRestartDialog()
            is LaptopIntent.Lock -> handleLock()
            is LaptopIntent.Sleep -> handleSleep()
            is LaptopIntent.RetryConnection -> handleRetryConnection()
            is LaptopIntent.RefreshStatus -> handleRefreshStatus(showToast = true)
        }
    }

    private fun handlePowerClick() {
        when (_state.value.powerState) {
            PowerState.ON -> {
                _state.update { it.copy(showShutdownDialog = true) }
            }
            PowerState.OFF -> {
                _state.update {
                    it.copy(
                        powerState = PowerState.BOOTING,
                        connectionStatus = DeviceConnectionStatus.Pinging
                    )
                }
                viewModelScope.launch {
                    _effect.send(LaptopEffect.ShowSnackbar("Relay triggered - starting workstation..."))
                    val relayResult = espUseCases.powerOn()
                    if (relayResult.isFailure) {
                        _state.update {
                            it.copy(
                                powerState = PowerState.OFF,
                                connectionStatus = DeviceConnectionStatus.Offline("Relay unreachable")
                            )
                        }
                        _effect.send(LaptopEffect.ShowSnackbar("Failed to trigger relay: ${relayResult.exceptionOrNull()?.message}"))
                    } else {
                        // Poll for workstation server to come online
                        var attempts = 0
                        while (attempts < 15) {
                            delay(2000.milliseconds)
                            val status = useCases.checkStatus()
                            if (status is DeviceConnectionStatus.Online) {
                                val telemetry = useCases.getTelemetry().getOrNull()
                                _state.update {
                                    it.copy(
                                        powerState = PowerState.ON,
                                        connectionStatus = status,
                                        telemetry = telemetry ?: it.telemetry
                                    )
                                }
                                _effect.send(LaptopEffect.ShowSnackbar("Workstation online"))
                                break
                            }
                            attempts++
                        }
                    }
                }
            }
            PowerState.BOOTING,
            PowerState.SHUTTING_DOWN -> {
                // Ignore clicks during transitional states
            }
        }
    }

    private fun handleRequestShutdown() {
        _state.update { it.copy(showShutdownDialog = true) }
    }

    private fun handleConfirmShutdown() {
        _state.update {
            it.copy(
                showShutdownDialog = false,
                powerState = PowerState.SHUTTING_DOWN,
                isBusy = true
            )
        }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Shutting down workstation..."))
            val result = useCases.shutdown()
            // Reset relay to OFF state so it's ready for next boot cycle
            espUseCases.powerOff()
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        powerState = PowerState.OFF,
                        connectionStatus = DeviceConnectionStatus.Offline("Powered Down"),
                        telemetry = null,
                        isBusy = false
                    )
                }
                _effect.send(LaptopEffect.ShowSnackbar("Shutdown command sent & relay reset"))
            } else {
                _state.update { it.copy(isBusy = false) }
                _effect.send(LaptopEffect.ShowSnackbar("Failed to shut down: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private fun handleDismissShutdownDialog() {
        _state.update { it.copy(showShutdownDialog = false) }
    }

    private fun handleRequestRestart() {
        _state.update { it.copy(showRestartDialog = true) }
    }

    private fun handleConfirmRestart() {
        _state.update {
            it.copy(
                showRestartDialog = false,
                isBusy = true
            )
        }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Restarting workstation..."))
            val result = useCases.restart()
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        powerState = PowerState.BOOTING,
                        connectionStatus = DeviceConnectionStatus.Pinging,
                        telemetry = null,
                        isBusy = false
                    )
                }
                _effect.send(LaptopEffect.ShowSnackbar("Restart command sent"))
            } else {
                _state.update { it.copy(isBusy = false) }
                _effect.send(LaptopEffect.ShowSnackbar("Failed to restart: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private fun handleDismissRestartDialog() {
        _state.update { it.copy(showRestartDialog = false) }
    }

    private fun handleLock() {
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val result = useCases.lock()
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        telemetry = it.telemetry?.copy(isLocked = true),
                        isBusy = false
                    )
                }
                _effect.send(LaptopEffect.ShowSnackbar("Workstation locked"))
            } else {
                _state.update { it.copy(isBusy = false) }
                _effect.send(LaptopEffect.ShowSnackbar("Failed to lock: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private fun handleSleep() {
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Putting workstation to sleep..."))
            val result = useCases.sleep()
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        powerState = PowerState.OFF,
                        connectionStatus = DeviceConnectionStatus.Offline("Sleeping"),
                        telemetry = null,
                        isBusy = false
                    )
                }
                _effect.send(LaptopEffect.ShowSnackbar("Sleep command sent"))
            } else {
                _state.update { it.copy(isBusy = false) }
                _effect.send(LaptopEffect.ShowSnackbar("Failed to put workstation to sleep: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    private fun handleRetryConnection() {
        handleRefreshStatus(showToast = true)
    }

    private fun handleRefreshStatus(showToast: Boolean = true) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val status = useCases.checkStatus()
            if (status is DeviceConnectionStatus.Online) {
                val telemetryResult = useCases.getTelemetry()
                val telemetry = telemetryResult.getOrNull()
                _state.update {
                    it.copy(
                        powerState = PowerState.ON,
                        connectionStatus = status,
                        telemetry = telemetry ?: it.telemetry,
                        isRefreshing = false
                    )
                }
                if (showToast) {
                    _effect.send(LaptopEffect.ShowSnackbar("Workstation online (${status.latencyMs}ms)"))
                }
            } else {
                _state.update {
                    it.copy(
                        powerState = PowerState.OFF,
                        connectionStatus = status,
                        telemetry = null,
                        isRefreshing = false
                    )
                }
                if (showToast) {
                    _effect.send(LaptopEffect.ShowSnackbar("Workstation offline"))
                }
            }
        }
    }
}
