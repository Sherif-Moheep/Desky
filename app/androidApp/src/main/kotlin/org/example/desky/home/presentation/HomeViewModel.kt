package org.example.desky.home.presentation

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
import org.example.desky.core.domain.usecase.DeskUseCases
import org.example.desky.home.presentation.contract.HomeEffect
import org.example.desky.home.presentation.contract.HomeIntent
import org.example.desky.home.presentation.contract.HomeState
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(
    private val deskUseCases: DeskUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            deskUseCases.observeDeskState().collect { snapshot ->
                val maxChannel = snapshot.ledDeviceState?.let { maxOf(it.red, it.green, it.blue) } ?: 0
                val brightnessPercent = if (maxChannel > 0) ((maxChannel / 255f) * 100).roundToInt() else 0

                _state.update { current ->
                    current.copy(
                        powerState = snapshot.derivedPowerState,
                        connectionStatus = snapshot.primaryConnectionStatus,
                        telemetry = current.telemetry.copy(
                            laptop = current.telemetry.laptop.copy(
                                isOnline = snapshot.laptopStatus is DeviceConnectionStatus.Online,
                                isLocked = snapshot.laptopTelemetry?.isLocked ?: false
                            ),
                            relay = current.telemetry.relay.copy(
                                isOnline = snapshot.espStatus is DeviceConnectionStatus.Online,
                                isAcActive = snapshot.isAcActive
                            ),
                            light = current.telemetry.light.copy(
                                isOnline = snapshot.ledStatus is DeviceConnectionStatus.Online,
                                isOn = snapshot.isLedPoweredOn,
                                brightnessPercent = if (snapshot.isLedPoweredOn) brightnessPercent else current.telemetry.light.brightnessPercent,
                                colorName = snapshot.ledColorName
                            ),
                            network = current.telemetry.network.copy(
                                isConnected = snapshot.isAnyDeviceOnline,
                                latencyMs = snapshot.primaryLatencyMs.toInt()
                            )
                        )
                    )
                }
            }
        }

        // Startup telemetry synchronization (silent, no snackbar)
        handleRefreshTelemetry(isManual = false)
    }

    fun sendIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleDeskPower -> handleToggleDeskPower()
            is HomeIntent.ConfirmPowerOff -> handleConfirmPowerOff()
            is HomeIntent.DismissPowerOffDialog -> handleDismissPowerOffDialog()
            is HomeIntent.RefreshTelemetry -> handleRefreshTelemetry(isManual = true)
            is HomeIntent.RetryConnection -> handleRetryConnection()
        }
    }

    private fun handleToggleDeskPower() {
        when (_state.value.powerState) {
            PowerState.ON -> {
                _state.update { it.copy(showPowerOffDialog = true) }
            }
            PowerState.OFF -> {
                handlePowerOn()
            }
            PowerState.BOOTING,
            PowerState.SHUTTING_DOWN -> {
                // Ignore clicks during transitional states
            }
        }
    }

    private fun handlePowerOn() {
        _state.update {
            it.copy(
                powerState = PowerState.BOOTING,
                connectionStatus = DeviceConnectionStatus.Pinging
            )
        }
        viewModelScope.launch {
            _effect.send(HomeEffect.ShowSnackbar("Relay triggered - starting desk setup..."))
            val result = deskUseCases.powerOnDesk()
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        powerState = PowerState.OFF,
                        connectionStatus = DeviceConnectionStatus.Offline("Relay unreachable")
                    )
                }
                _effect.send(HomeEffect.ShowSnackbar("Failed to trigger relay: ${result.exceptionOrNull()?.message}"))
            } else {
                _effect.send(HomeEffect.ShowSnackbar("Desk power ON - workstation & peripherals ready"))
            }
        }
    }

    private fun handleConfirmPowerOff() {
        _state.update {
            it.copy(
                showPowerOffDialog = false,
                powerState = PowerState.SHUTTING_DOWN
            )
        }
        viewModelScope.launch {
            _effect.send(HomeEffect.ShowSnackbar("Powering down desk setup..."))
            deskUseCases.powerOffDesk()
            delay(1200.milliseconds)
            _state.update {
                it.copy(
                    powerState = PowerState.OFF,
                    connectionStatus = DeviceConnectionStatus.Offline("Desk Off")
                )
            }
            _effect.send(HomeEffect.ShowSnackbar("Desk powered down & relay reset"))
        }
    }

    private fun handleDismissPowerOffDialog() {
        _state.update { it.copy(showPowerOffDialog = false) }
    }

    private fun handleRefreshTelemetry(isManual: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            deskUseCases.refreshDeskState()
            _state.update { it.copy(isRefreshing = false) }
            if (isManual) {
                _effect.send(HomeEffect.ShowSnackbar("Desk telemetry refreshed"))
            }
        }
    }

    private fun handleRetryConnection() {
        handleRefreshTelemetry(isManual = true)
    }
}
