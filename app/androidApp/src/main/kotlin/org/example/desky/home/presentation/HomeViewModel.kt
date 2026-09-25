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
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.home.presentation.contract.HomeEffect
import org.example.desky.home.presentation.contract.HomeIntent
import org.example.desky.home.presentation.contract.HomeState
import org.example.desky.home.presentation.model.DeskPowerState
import org.example.desky.home.presentation.model.HomeTelemetryUiState

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    fun sendIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleDeskPower -> handleToggleDeskPower()
            is HomeIntent.ConfirmPowerOff -> handleConfirmPowerOff()
            is HomeIntent.DismissPowerOffDialog -> handleDismissPowerOffDialog()
            is HomeIntent.RefreshTelemetry -> handleRefreshTelemetry()
            is HomeIntent.RetryConnection -> handleRetryConnection()
        }
    }

    private fun handleToggleDeskPower() {
        when (_state.value.powerState) {
            DeskPowerState.ON -> {
                _state.update { it.copy(showPowerOffDialog = true) }
            }
            DeskPowerState.OFF -> {
                handlePowerOn()
            }
            DeskPowerState.BOOTING,
            DeskPowerState.SHUTTING_DOWN -> {
                // Ignore clicks during transitional states
            }
        }
    }

    private fun handlePowerOn() {
        _state.update {
            it.copy(
                powerState = DeskPowerState.BOOTING,
                connectionStatus = DeviceConnectionStatus.Pinging
            )
        }
        viewModelScope.launch {
            _effect.send(HomeEffect.ShowSnackbar("Relay triggered — starting desk setup..."))
            // Simulate / future wire to domain layer (ESP32 relay pulse & polling status)
            delay(1800)
            _state.update {
                it.copy(
                    powerState = DeskPowerState.ON,
                    connectionStatus = DeviceConnectionStatus.Online(12),
                    telemetry = it.telemetry.copy(
                        laptop = it.telemetry.laptop.copy(isOnline = true, isLocked = false),
                        light = it.telemetry.light.copy(isOnline = true, isOn = true),
                        relay = it.telemetry.relay.copy(isOnline = true, isAcActive = true)
                    )
                )
            }
            _effect.send(HomeEffect.ShowSnackbar("Desk power ON — workstation & peripherals ready"))
        }
    }

    private fun handleConfirmPowerOff() {
        _state.update {
            it.copy(
                showPowerOffDialog = false,
                powerState = DeskPowerState.SHUTTING_DOWN
            )
        }
        viewModelScope.launch {
            _effect.send(HomeEffect.ShowSnackbar("Powering down desk setup..."))
            // Simulate / future wire to domain layer (Laptop Ktor server /shutdown & ESP relay off)
            delay(1500)
            _state.update {
                it.copy(
                    powerState = DeskPowerState.OFF,
                    connectionStatus = DeviceConnectionStatus.Online(12),
                    telemetry = it.telemetry.copy(
                        laptop = it.telemetry.laptop.copy(isOnline = false),
                        light = it.telemetry.light.copy(isOnline = true, isOn = false),
                        relay = it.telemetry.relay.copy(isOnline = true, isAcActive = false)
                    )
                )
            }
        }
    }

    private fun handleDismissPowerOffDialog() {
        _state.update { it.copy(showPowerOffDialog = false) }
    }

    private fun handleRefreshTelemetry() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            _effect.send(HomeEffect.ShowSnackbar("Checking desk telemetry..."))
            // TODO: Wire to domain layer for live ping / status check
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun handleRetryConnection() {
        viewModelScope.launch {
            _effect.send(HomeEffect.ShowSnackbar("Pinging local devices..."))
            // TODO: Wire to domain layer health pings
        }
    }
}
