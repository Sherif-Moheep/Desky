package org.example.desky.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.usecase.DeskUseCases
import org.example.desky.core.domain.usecase.SettingsUseCases
import org.example.desky.core.domain.model.DeviceType
import org.example.desky.settings.presentation.contract.SettingsEffect
import org.example.desky.settings.presentation.contract.SettingsIntent
import org.example.desky.settings.presentation.contract.SettingsState
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel(
    private val settingsUseCases: SettingsUseCases,
    private val deskUseCases: DeskUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsEffect> = _effect.receiveAsFlow()

    private var debounceSaveJob: Job? = null

    init {
        viewModelScope.launch {
            settingsUseCases.observeSettings().collect { settings ->
                if (debounceSaveJob?.isActive == true) return@collect
                _state.update { current ->
                    current.copy(
                        espHost = settings.espRelay.host,
                        espPort = settings.espRelay.port.toString(),
                        laptopHost = settings.laptop.host,
                        laptopPort = settings.laptop.port.toString(),
                        ledHost = settings.ledStrip.host,
                        ledPort = settings.ledStrip.port.toString(),
                        themeMode = settings.themeMode
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateEspHost -> {
                _state.update { it.copy(espHost = intent.host) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.UpdateEspPort -> {
                _state.update { it.copy(espPort = intent.port) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.UpdateLaptopHost -> {
                _state.update { it.copy(laptopHost = intent.host) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.UpdateLaptopPort -> {
                _state.update { it.copy(laptopPort = intent.port) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.UpdateLedHost -> {
                _state.update { it.copy(ledHost = intent.host) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.UpdateLedPort -> {
                _state.update { it.copy(ledPort = intent.port) }
                scheduleDebouncedSave()
            }
            is SettingsIntent.SetThemeMode -> {
                _state.update { it.copy(themeMode = intent.mode) }
                viewModelScope.launch {
                    settingsUseCases.updateThemeMode(intent.mode)
                }
            }
            is SettingsIntent.PingDevice -> {
                pingDevice(intent.device)
            }
            is SettingsIntent.PingAllDevices -> {
                pingAllDevices()
            }
            is SettingsIntent.ShowResetDialog -> {
                _state.update { it.copy(showResetDialog = true) }
            }
            is SettingsIntent.DismissResetDialog -> {
                _state.update { it.copy(showResetDialog = false) }
            }
            is SettingsIntent.ConfirmReset -> {
                resetSettings()
            }
        }
    }

    private fun scheduleDebouncedSave() {
        debounceSaveJob?.cancel()
        debounceSaveJob = viewModelScope.launch {
            delay(500.milliseconds)
            commitCurrentEndpoints()
        }
    }

    private suspend fun commitCurrentEndpoints() {
        debounceSaveJob?.cancel()
        val currentState = _state.value
        val espPortInt = currentState.espPort.toIntOrNull() ?: 80
        val laptopPortInt = currentState.laptopPort.toIntOrNull() ?: 8080
        val ledPortInt = currentState.ledPort.toIntOrNull() ?: 5577

        settingsUseCases.updateEspEndpoint(currentState.espHost, espPortInt)
        settingsUseCases.updateLaptopEndpoint(currentState.laptopHost, laptopPortInt)
        settingsUseCases.updateLedEndpoint(currentState.ledHost, ledPortInt)
    }

    private fun updateDeviceStatus(
        device: DeviceType,
        status: DeviceConnectionStatus
    ) {
        _state.update { current ->
            when (device) {
                DeviceType.ESP_RELAY -> current.copy(espStatus = status)
                DeviceType.LAPTOP -> current.copy(laptopStatus = status)
                DeviceType.LED_STRIP -> current.copy(ledStatus = status)
            }
        }
    }

    private suspend fun checkDeviceStatus(
        device: DeviceType
    ): DeviceConnectionStatus = deskUseCases.pingDevice(device)

    private fun pingDevice(
        device: DeviceType
    ) {
        viewModelScope.launch {
            commitCurrentEndpoints()
            updateDeviceStatus(device, DeviceConnectionStatus.Pinging)
            val status = checkDeviceStatus(device)
            updateDeviceStatus(device, status)
        }
    }

    private fun pingAllDevices() {
        viewModelScope.launch {
            commitCurrentEndpoints()
            _state.update {
                it.copy(
                    isRefreshing = true,
                    espStatus = DeviceConnectionStatus.Pinging,
                    laptopStatus = DeviceConnectionStatus.Pinging,
                    ledStatus = DeviceConnectionStatus.Pinging
                )
            }

            val espDeferred = async { checkDeviceStatus(DeviceType.ESP_RELAY) }
            val laptopDeferred = async { checkDeviceStatus(DeviceType.LAPTOP) }
            val ledDeferred = async { checkDeviceStatus(DeviceType.LED_STRIP) }

            val espStatus = espDeferred.await()
            val laptopStatus = laptopDeferred.await()
            val ledStatus = ledDeferred.await()

            _state.update {
                it.copy(
                    espStatus = espStatus,
                    laptopStatus = laptopStatus,
                    ledStatus = ledStatus,
                    isRefreshing = false
                )
            }

            val onlineCount = listOf(espStatus, laptopStatus, ledStatus).count { it is DeviceConnectionStatus.Online }
            _effect.send(SettingsEffect.ShowSnackbar("Pinged all devices: $onlineCount/3 reachable"))
        }
    }

    private fun resetSettings() {
        viewModelScope.launch {
            settingsUseCases.resetToDefaults()
            val defaults = AppSettings()
            _state.update {
                it.copy(
                    espHost = defaults.espRelay.host,
                    espPort = defaults.espRelay.port.toString(),
                    espStatus = DeviceConnectionStatus.Idle,
                    laptopHost = defaults.laptop.host,
                    laptopPort = defaults.laptop.port.toString(),
                    laptopStatus = DeviceConnectionStatus.Idle,
                    ledHost = defaults.ledStrip.host,
                    ledPort = defaults.ledStrip.port.toString(),
                    ledStatus = DeviceConnectionStatus.Idle,
                    themeMode = defaults.themeMode,
                    showResetDialog = false,
                    isRefreshing = false
                )
            }
            _effect.send(SettingsEffect.ShowSnackbar("Settings restored to defaults"))
        }
    }
}
