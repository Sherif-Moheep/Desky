package org.example.desky.devices.laptop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.laptop.presentation.contract.LaptopEffect
import org.example.desky.devices.laptop.presentation.contract.LaptopIntent
import org.example.desky.devices.laptop.presentation.contract.LaptopState
import org.example.desky.devices.laptop.presentation.model.LaptopPowerState

class LaptopViewModel : ViewModel() {

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
            LaptopPowerState.ON -> {
                _state.update { it.copy(showShutdownDialog = true) }
            }
            LaptopPowerState.OFF -> {
                _state.update {
                    it.copy(
                        powerState = LaptopPowerState.BOOTING,
                        connectionStatus = DeviceConnectionStatus.Pinging
                    )
                }
                viewModelScope.launch {
                    _effect.send(LaptopEffect.ShowSnackbar("Relay triggered - starting workstation..."))
                    // TODO: Wire to domain layer (ESP32 relay pulse & polling status)
                }
            }
            LaptopPowerState.BOOTING,
            LaptopPowerState.SHUTTING_DOWN -> {
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
                powerState = LaptopPowerState.SHUTTING_DOWN
            )
        }
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Shutting down workstation..."))
            // TODO: Wire to domain layer (Laptop Ktor server /shutdown & ESP relay off)
            _state.update {
                it.copy(
                    powerState = LaptopPowerState.OFF,
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
            it.copy(telemetry = it.telemetry.copy(isLocked = true))
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
        viewModelScope.launch {
            _effect.send(LaptopEffect.ShowSnackbar("Pinging laptop server..."))
            // TODO: Wire to domain layer (Health ping to laptop server)
        }
    }

    private fun handleRefreshStatus() {
        // TODO: Wire to domain layer (Periodic or manual status fetch)
    }
}
