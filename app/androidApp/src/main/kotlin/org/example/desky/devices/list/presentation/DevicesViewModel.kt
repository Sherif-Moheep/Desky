package org.example.desky.devices.list.presentation

import androidx.compose.ui.graphics.Color
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
import org.example.desky.core.domain.usecase.DeskUseCases
import org.example.desky.core.domain.usecase.ObserveSettingsUseCase
import org.example.desky.devices.list.presentation.contract.DevicesEffect
import org.example.desky.devices.list.presentation.contract.DevicesIntent
import org.example.desky.devices.list.presentation.contract.DevicesState

class DevicesViewModel(
    private val observeSettings: ObserveSettingsUseCase,
    private val deskUseCases: DeskUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()

    private val _effect = Channel<DevicesEffect>(Channel.BUFFERED)
    val effect: Flow<DevicesEffect> = _effect.receiveAsFlow()

    init {
        // Observe endpoints from Settings and perform initial scan
        viewModelScope.launch {
            val settings = observeSettings().first()
            _state.update {
                it.copy(
                    laptopHost = settings.laptop.host,
                    laptopPort = settings.laptop.port,
                    espHost = settings.espRelay.host,
                    espPort = settings.espRelay.port,
                    ledHost = settings.ledStrip.host,
                    ledPort = settings.ledStrip.port
                )
            }
            refreshAllDevices(isManual = false)
        }

        // Observe unified Desk State (SSOT)
        viewModelScope.launch {
            deskUseCases.observeDeskState().collect { snapshot ->
                _state.update { current ->
                    val ledColor = snapshot.ledDeviceState?.let {
                        Color(it.red, it.green, it.blue)
                    } ?: current.ledColor

                    current.copy(
                        laptopStatus = snapshot.laptopStatus,
                        laptopPowerState = snapshot.derivedPowerState,
                        laptopTelemetry = snapshot.laptopTelemetry,
                        isLaptopLocked = snapshot.laptopTelemetry?.isLocked ?: false,
                        laptopUptime = snapshot.laptopTelemetry?.uptimeFormatted ?: if (snapshot.laptopStatus is DeviceConnectionStatus.Online) current.laptopUptime else "--",
                        espStatus = snapshot.espStatus,
                        ledStatus = snapshot.ledStatus,
                        isLedPoweredOn = snapshot.isLedPoweredOn,
                        ledBrightness = snapshot.ledBrightness,
                        ledColor = ledColor,
                        ledColorName = snapshot.ledColorName
                    )
                }
            }
        }
    }

    fun onIntent(intent: DevicesIntent) {
        when (intent) {
            is DevicesIntent.Refresh -> refreshAllDevices(isManual = true)
            is DevicesIntent.LaptopPowerClick -> handleLaptopPowerClick()
            is DevicesIntent.ConfirmLaptopShutdown -> handleConfirmLaptopShutdown()
            is DevicesIntent.DismissLaptopShutdownDialog -> {
                _state.update { it.copy(showLaptopShutdownDialog = false) }
            }
            is DevicesIntent.ToggleLedPower -> handleToggleLedPower(intent.targetOn)
        }
    }

    private fun refreshAllDevices(isManual: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRefreshing = true,
                    laptopStatus = DeviceConnectionStatus.Pinging,
                    espStatus = DeviceConnectionStatus.Pinging,
                    ledStatus = DeviceConnectionStatus.Pinging
                )
            }

            // Sync configured endpoints
            val settings = observeSettings().first()

            _state.update {
                it.copy(
                    laptopHost = settings.laptop.host,
                    laptopPort = settings.laptop.port,
                    espHost = settings.espRelay.host,
                    espPort = settings.espRelay.port,
                    ledHost = settings.ledStrip.host,
                    ledPort = settings.ledStrip.port
                )
            }

            val snapshot = deskUseCases.refreshDeskState()
            _state.update { it.copy(isRefreshing = false) }

            if (isManual) {
                _effect.send(DevicesEffect.ShowSnackbar("Scan complete - ${snapshot.onlineDevicesCount} endpoints reachable"))
            }
        }
    }

    private fun handleLaptopPowerClick() {
        when (_state.value.laptopPowerState) {
            PowerState.ON -> {
                _state.update { it.copy(showLaptopShutdownDialog = true) }
            }
            PowerState.OFF -> {
                _state.update {
                    it.copy(
                        laptopPowerState = PowerState.BOOTING,
                        laptopStatus = DeviceConnectionStatus.Pinging
                    )
                }
                viewModelScope.launch {
                    _effect.send(DevicesEffect.ShowSnackbar("Relay triggered - starting workstation..."))
                    val result = deskUseCases.powerOnDesk()
                    if (result.isFailure) {
                        _state.update {
                            it.copy(
                                laptopPowerState = PowerState.OFF,
                                laptopStatus = DeviceConnectionStatus.Offline("Relay unreachable")
                            )
                        }
                        _effect.send(DevicesEffect.ShowSnackbar("Failed to trigger relay: ${result.exceptionOrNull()?.message}"))
                    } else {
                        _effect.send(DevicesEffect.ShowSnackbar("Workstation online"))
                    }
                }
            }
            PowerState.BOOTING,
            PowerState.SHUTTING_DOWN -> {
                // Ignore clicks during transitions
            }
        }
    }

    private fun handleConfirmLaptopShutdown() {
        _state.update {
            it.copy(
                showLaptopShutdownDialog = false,
                laptopPowerState = PowerState.SHUTTING_DOWN
            )
        }
        viewModelScope.launch {
            _effect.send(DevicesEffect.ShowSnackbar("Shutting down workstation..."))
            deskUseCases.shutdownLaptop()
            _state.update {
                it.copy(
                    laptopPowerState = PowerState.OFF,
                    laptopStatus = DeviceConnectionStatus.Offline("Powered Down"),
                    laptopTelemetry = null,
                    laptopUptime = "--",
                    isLaptopLocked = false
                )
            }
            _effect.send(DevicesEffect.ShowSnackbar("Shutdown command sent & relay reset"))
        }
    }

    private fun handleToggleLedPower(targetOn: Boolean) {
        _state.update { it.copy(isLedPoweredOn = targetOn) }
        viewModelScope.launch {
            val result = deskUseCases.setLedPower(targetOn)
            if (result.isSuccess) {
                _effect.send(
                    DevicesEffect.ShowSnackbar(
                        if (targetOn) "Desk backlight turned ON" else "Desk backlight turned OFF"
                    )
                )
            } else {
                _effect.send(DevicesEffect.ShowSnackbar("Failed to switch desk backlight"))
            }
        }
    }
}
