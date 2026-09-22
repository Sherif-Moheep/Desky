package org.example.desky.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.withContext
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.settings.presentation.contract.DeviceType
import org.example.desky.settings.presentation.contract.SettingsEffect
import org.example.desky.settings.presentation.contract.SettingsIntent
import org.example.desky.settings.presentation.contract.SettingsState
import java.net.InetSocketAddress
import java.net.Socket
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

            val status = checkConnection(host, port)
            _state.update {
                when (device) {
                    DeviceType.ESP_RELAY -> it.copy(espStatus = status)
                    DeviceType.LAPTOP -> it.copy(laptopStatus = status)
                    DeviceType.LED_STRIP -> it.copy(ledStatus = status)
                }
            }
        }
    }

    private suspend fun checkConnection(
        host: String,
        port: Int
    )
    : DeviceConnectionStatus {
        if (host.isBlank()) return DeviceConnectionStatus.Offline("Host empty")
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), 2000)
                }
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                DeviceConnectionStatus.Online(latency)
            } catch (e: Exception) {
                DeviceConnectionStatus.Offline(e.localizedMessage?.take(25) ?: "Unreachable")
            }
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
                    showResetDialog = false
                )
            }
            _effect.send(SettingsEffect.ShowSnackbar("Settings restored to defaults"))
        }
    }
}
