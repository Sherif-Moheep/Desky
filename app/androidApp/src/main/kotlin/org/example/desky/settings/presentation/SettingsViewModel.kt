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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.util.checkSocketConnection
import org.example.desky.settings.presentation.model.DeviceType
import org.example.desky.settings.presentation.contract.SettingsEffect
import org.example.desky.settings.presentation.contract.SettingsIntent
import org.example.desky.settings.presentation.contract.SettingsState
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsEffect> = _effect.receiveAsFlow()

    private var debounceSaveJob: Job? = null

    init {
        viewModelScope.launch {
            val initial = settingsRepository.settingsFlow.first()
            _state.update {
                it.copy(
                    espHost = initial.espRelay.host,
                    espPort = initial.espRelay.port.toString(),
                    laptopHost = initial.laptop.host,
                    laptopPort = initial.laptop.port.toString(),
                    ledHost = initial.ledStrip.host,
                    ledPort = initial.ledStrip.port.toString(),
                    themeMode = initial.themeMode
                )
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
                    settingsRepository.updateThemeMode(intent.mode)
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
            val currentState = _state.value
            val espPortInt = currentState.espPort.toIntOrNull() ?: 80
            val laptopPortInt = currentState.laptopPort.toIntOrNull() ?: 8080
            val ledPortInt = currentState.ledPort.toIntOrNull() ?: 5577

            settingsRepository.updateEspEndpoint(currentState.espHost, espPortInt)
            settingsRepository.updateLaptopEndpoint(currentState.laptopHost, laptopPortInt)
            settingsRepository.updateLedEndpoint(currentState.ledHost, ledPortInt)
        }
    }

    private fun pingDevice(device: DeviceType) {
        viewModelScope.launch {
            val (host, port) = when (device) {
                DeviceType.ESP_RELAY -> {
                    _state.update { it.copy(espStatus = DeviceConnectionStatus.Pinging) }
                    _state.value.espHost to (_state.value.espPort.toIntOrNull() ?: 80)
                }
                DeviceType.LAPTOP -> {
                    _state.update { it.copy(laptopStatus = DeviceConnectionStatus.Pinging) }
                    _state.value.laptopHost to (_state.value.laptopPort.toIntOrNull() ?: 8080)
                }
                DeviceType.LED_STRIP -> {
                    _state.update { it.copy(ledStatus = DeviceConnectionStatus.Pinging) }
                    _state.value.ledHost to (_state.value.ledPort.toIntOrNull() ?: 5577)
                }
            }

            val status = checkSocketConnection(host, port)
            _state.update {
                when (device) {
                    DeviceType.ESP_RELAY -> it.copy(espStatus = status)
                    DeviceType.LAPTOP -> it.copy(laptopStatus = status)
                    DeviceType.LED_STRIP -> it.copy(ledStatus = status)
                }
            }
        }
    }

    private fun pingAllDevices() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRefreshing = true,
                    espStatus = DeviceConnectionStatus.Pinging,
                    laptopStatus = DeviceConnectionStatus.Pinging,
                    ledStatus = DeviceConnectionStatus.Pinging
                )
            }

            val current = _state.value
            val espHost = current.espHost
            val espPort = current.espPort.toIntOrNull() ?: 80
            val laptopHost = current.laptopHost
            val laptopPort = current.laptopPort.toIntOrNull() ?: 8080
            val ledHost = current.ledHost
            val ledPort = current.ledPort.toIntOrNull() ?: 5577

            val espDeferred = async { checkSocketConnection(espHost, espPort) }
            val laptopDeferred = async { checkSocketConnection(laptopHost, laptopPort) }
            val ledDeferred = async { checkSocketConnection(ledHost, ledPort) }

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
            settingsRepository.resetToDefaults()
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
