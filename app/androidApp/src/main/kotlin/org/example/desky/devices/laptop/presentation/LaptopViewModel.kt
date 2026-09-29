package org.example.desky.devices.laptop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.util.checkSocketConnection
import org.example.desky.devices.laptop.presentation.contract.LaptopEffect
import org.example.desky.devices.laptop.presentation.contract.LaptopIntent
import org.example.desky.devices.laptop.presentation.contract.LaptopState

class LaptopViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LaptopState())
    val state: StateFlow<LaptopState> = _state.asStateFlow()

    private val _effect = Channel<LaptopEffect>(Channel.BUFFERED)
    val effect: Flow<LaptopEffect> = _effect.receiveAsFlow()

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
            is LaptopIntent.RefreshStatus -> handleRefreshStatus()
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
                    // TODO: Wire to domain layer (ESP32 relay pulse & polling status)
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
                powerState = PowerState.SHUTTING_DOWN
            )
        }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Shutting down workstation..."))
            // TODO: Wire to domain layer (Laptop Ktor server /shutdown & ESP relay off)
            _state.update {
                it.copy(
                    powerState = PowerState.OFF,
                    connectionStatus = DeviceConnectionStatus.Offline("Offline")
                )
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
        _state.update { it.copy(showRestartDialog = false) }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Restarting workstation..."))
            // TODO: Wire to domain layer (Laptop Ktor server /restart)
        }
    }

    private fun handleDismissRestartDialog() {
        _state.update { it.copy(showRestartDialog = false) }
    }

    private fun handleLock() {
        _state.update {
            it.copy(telemetry = it.telemetry?.copy(isLocked = true))
        }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Workstation locked"))
            // TODO: Wire to domain layer (Laptop Ktor server /lock)
        }
    }

    private fun handleSleep() {
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Sleep command sent"))
            // TODO: Wire to domain layer (Laptop Ktor server /sleep)
        }
    }

    private fun handleRetryConnection() {
        handleRefreshStatus()
    }

    private fun handleRefreshStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val endpoint = settingsRepository.laptopEndpointFlow.first()
            val status = checkSocketConnection(endpoint)
            _state.update {
                it.copy(
                    connectionStatus = status,
                    isRefreshing = false
                )
            }
            val message = if (status is DeviceConnectionStatus.Online) {
                "Workstation online (${status.latencyMs}ms)"
            } else {
                "Workstation offline"
            }
            _effect.send(LaptopEffect.ShowSnackbar(message))
        }
    }
}
