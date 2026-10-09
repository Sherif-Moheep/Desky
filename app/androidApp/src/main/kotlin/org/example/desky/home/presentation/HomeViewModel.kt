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
import org.example.desky.devices.esp.domain.usecase.EspRelayUseCases
import org.example.desky.devices.laptop.domain.usecase.LaptopUseCases
import org.example.desky.devices.led.domain.usecase.LedUseCases
import org.example.desky.home.presentation.contract.HomeEffect
import org.example.desky.home.presentation.contract.HomeIntent
import org.example.desky.home.presentation.contract.HomeState
import org.example.desky.home.presentation.model.HomeTelemetryUiState
import kotlin.time.Duration.Companion.milliseconds

class HomeViewModel(
    private val espUseCases: EspRelayUseCases,
    private val laptopUseCases: LaptopUseCases,
    private val ledUseCases: LedUseCases
) : ViewModel() {

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
            val relayResult = espUseCases.powerOn()
            ledUseCases.setPower(true)
            if (relayResult.isFailure) {
                _state.update {
                    it.copy(
                        powerState = PowerState.OFF,
                        connectionStatus = DeviceConnectionStatus.Offline("Relay unreachable")
                    )
                }
                _effect.send(HomeEffect.ShowSnackbar("Failed to trigger relay: ${relayResult.exceptionOrNull()?.message}"))
            } else {
                _state.update {
                    it.copy(
                        telemetry = it.telemetry.copy(
                            light = it.telemetry.light.copy(isOnline = true, isOn = true),
                            relay = it.telemetry.relay.copy(isOnline = true, isAcActive = true)
                        )
                    )
                }
                var attempts = 0
                while (attempts < 15) {
                    delay(2000.milliseconds)
                    val status = laptopUseCases.checkStatus()
                    if (status is DeviceConnectionStatus.Online) {
                        _state.update {
                            it.copy(
                                powerState = PowerState.ON,
                                connectionStatus = status,
                                telemetry = it.telemetry.copy(
                                    laptop = it.telemetry.laptop.copy(isOnline = true, isLocked = false)
                                )
                            )
                        }
                        _effect.send(HomeEffect.ShowSnackbar("Desk power ON - workstation & peripherals ready"))
                        break
                    }
                    attempts++
                }
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
            laptopUseCases.shutdown()
            espUseCases.powerOff()
            ledUseCases.setPower(false)
            delay(1200.milliseconds)
            _state.update {
                it.copy(
                    powerState = PowerState.OFF,
                    connectionStatus = DeviceConnectionStatus.Offline("Desk Off"),
                    telemetry = it.telemetry.copy(
                        laptop = it.telemetry.laptop.copy(isOnline = false),
                        light = it.telemetry.light.copy(isOnline = true, isOn = false),
                        relay = it.telemetry.relay.copy(isOnline = true, isAcActive = false)
                    )
                )
            }
            _effect.send(HomeEffect.ShowSnackbar("Desk powered down & relay reset"))
        }
    }

    private fun handleDismissPowerOffDialog() {
        _state.update { it.copy(showPowerOffDialog = false) }
    }

    private fun handleRefreshTelemetry() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val espStatus = espUseCases.checkStatus()
            val laptopStatus = laptopUseCases.checkStatus()
            val ledStatus = ledUseCases.observeConnectionStatus().value

            _state.update {
                val isAnyOnline = espStatus is DeviceConnectionStatus.Online || laptopStatus is DeviceConnectionStatus.Online
                it.copy(
                    isRefreshing = false,
                    connectionStatus = if (isAnyOnline) (if (laptopStatus is DeviceConnectionStatus.Online) laptopStatus else espStatus) else DeviceConnectionStatus.Offline("Offline"),
                    telemetry = it.telemetry.copy(
                        laptop = it.telemetry.laptop.copy(isOnline = laptopStatus is DeviceConnectionStatus.Online),
                        relay = it.telemetry.relay.copy(isOnline = espStatus is DeviceConnectionStatus.Online, isAcActive = it.powerState == PowerState.ON),
                        light = it.telemetry.light.copy(isOnline = ledStatus is DeviceConnectionStatus.Online)
                    )
                )
            }
            _effect.send(HomeEffect.ShowSnackbar("Desk telemetry refreshed"))
        }
    }

    private fun handleRetryConnection() {
        handleRefreshTelemetry()
    }
}
