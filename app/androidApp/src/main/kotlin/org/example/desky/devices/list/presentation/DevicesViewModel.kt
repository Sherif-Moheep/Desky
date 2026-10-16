package org.example.desky.devices.list.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.devices.esp.domain.usecase.EspRelayUseCases
import org.example.desky.devices.laptop.domain.usecase.LaptopUseCases
import org.example.desky.devices.led.domain.usecase.LedUseCases
import org.example.desky.devices.list.presentation.contract.DevicesEffect
import org.example.desky.devices.list.presentation.contract.DevicesIntent
import org.example.desky.devices.list.presentation.contract.DevicesState
import org.example.desky.model.LaptopTelemetryDto
import kotlin.time.Duration.Companion.milliseconds

class DevicesViewModel(
    private val settingsRepository: SettingsRepository,
    private val ledUseCases: LedUseCases,
    private val laptopUseCases: LaptopUseCases,
    private val espUseCases: EspRelayUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()

    private val _effect = Channel<DevicesEffect>(Channel.BUFFERED)
    val effect: Flow<DevicesEffect> = _effect.receiveAsFlow()

    init {
        // Observe endpoints from Settings and perform initial auto-ping
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
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

        // Observe LED Connection Status
        viewModelScope.launch {
            ledUseCases.observeConnectionStatus().collect { status ->
                _state.update { it.copy(ledStatus = status) }
            }
        }

        // Observe LED Brightness
        viewModelScope.launch {
            ledUseCases.observeLastBrightness().collect { brightness ->
                _state.update { it.copy(ledBrightness = brightness) }
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

            val laptopDef = async {
                val endpoint = settingsRepository.laptopEndpointFlow.first()
                _state.update { it.copy(laptopHost = endpoint.host, laptopPort = endpoint.port) }
                laptopUseCases.checkStatus()
            }

            val espDef = async {
                val endpoint = settingsRepository.espEndpointFlow.first()
                _state.update { it.copy(espHost = endpoint.host, espPort = endpoint.port) }
                espUseCases.checkStatus()
            }

            val ledDef = async {
                val endpoint = settingsRepository.ledEndpointFlow.first()
                _state.update { it.copy(ledHost = endpoint.host, ledPort = endpoint.port) }
                checkLedConnection()
            }

            val laptopStatus = laptopDef.await()
            val espStatus = espDef.await()
            val ledStatus = ledDef.await()

            // Fetch live laptop telemetry if online
            val laptopTelemetry = if (laptopStatus is DeviceConnectionStatus.Online) {
                laptopUseCases.getTelemetry().getOrNull()
            } else null

            _state.update {
                it.copy(
                    laptopStatus = laptopStatus,
                    laptopPowerState = if (laptopStatus is DeviceConnectionStatus.Online) PowerState.ON else PowerState.OFF,
                    laptopTelemetry = laptopTelemetry,
                    laptopUptime = laptopTelemetry?.uptimeFormatted ?: if (laptopStatus is DeviceConnectionStatus.Online) it.laptopUptime else "--",
                    isLaptopLocked = laptopTelemetry?.isLocked ?: false,
                    espStatus = espStatus,
                    ledStatus = ledStatus,
                    isRefreshing = false
                )
            }

            if (isManual) {
                val onlineCount = (if (laptopStatus is DeviceConnectionStatus.Online) 1 else 0) +
                        (if (espStatus is DeviceConnectionStatus.Online) 1 else 0) +
                        (if (ledStatus is DeviceConnectionStatus.Online) 1 else 0)
                _effect.send(DevicesEffect.ShowSnackbar("Scan complete - $onlineCount endpoints reachable"))
            }
        }
    }

    private suspend fun checkLedConnection(): DeviceConnectionStatus {
        val result = ledUseCases.connect(forceRediscovery = false)
        return if (result.isSuccess) {
            val deviceState = result.getOrNull()
            if (deviceState != null) {
                _state.update {
                    it.copy(
                        isLedPoweredOn = deviceState.isOn,
                        ledColor = Color(deviceState.red, deviceState.green, deviceState.blue)
                    )
                }
            }
            ledUseCases.observeConnectionStatus().value
        } else {
            DeviceConnectionStatus.Offline("Unreachable")
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
                    val relayResult = espUseCases.powerOn()
                    if (relayResult.isFailure) {
                        _state.update {
                            it.copy(
                                laptopPowerState = PowerState.OFF,
                                laptopStatus = DeviceConnectionStatus.Offline("Relay unreachable")
                            )
                        }
                        _effect.send(DevicesEffect.ShowSnackbar("Failed to trigger relay: ${relayResult.exceptionOrNull()?.message}"))
                    } else {
                        var attempts = 0
                        while (attempts < 15) {
                            delay(2000.milliseconds)
                            val status = laptopUseCases.checkStatus()
                            if (status is DeviceConnectionStatus.Online) {
                                val telemetry = laptopUseCases.getTelemetry().getOrNull()
                                _state.update {
                                    it.copy(
                                        laptopPowerState = PowerState.ON,
                                        laptopStatus = status,
                                        laptopTelemetry = telemetry,
                                        laptopUptime = telemetry?.uptimeFormatted ?: it.laptopUptime,
                                        isLaptopLocked = telemetry?.isLocked ?: false
                                    )
                                }
                                _effect.send(DevicesEffect.ShowSnackbar("Workstation online"))
                                break
                            }
                            attempts++
                        }
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
            laptopUseCases.shutdown()
            espUseCases.powerOff()
            delay(1200.milliseconds)
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
            val result = ledUseCases.setPower(targetOn)
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
