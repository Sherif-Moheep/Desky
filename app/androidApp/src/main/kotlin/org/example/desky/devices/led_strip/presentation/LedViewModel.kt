package org.example.desky.devices.led_strip.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.led_strip.domain.usecase.LedUseCases
import org.example.desky.devices.led_strip.presentation.contract.LedEffect
import org.example.desky.devices.led_strip.presentation.contract.LedIntent
import org.example.desky.devices.led_strip.presentation.contract.LedState
import org.example.desky.devices.led_strip.presentation.model.LedPresetItem
import org.example.desky.devices.led_strip.presentation.model.toDomain
import org.example.desky.devices.led_strip.presentation.model.toUiItem
import kotlin.math.roundToInt

class LedViewModel(
    private val useCases: LedUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(LedState())
    val state: StateFlow<LedState> = _state.asStateFlow()

    private val _effect = Channel<LedEffect>(Channel.BUFFERED)
    val effect: Flow<LedEffect> = _effect.receiveAsFlow()

    // High-frequency throttler for color wheel and brightness slider
    private val colorUpdateChannel = MutableSharedFlow<Pair<Color, Float>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        // Load initial brightness from DataStore fallback before live sync completes
        viewModelScope.launch {
            val savedBrightness = useCases.observeLastBrightness().first()
            _state.update { currentState ->
                if (currentState.connectionStatus !is DeviceConnectionStatus.Online) {
                    currentState.copy(brightness = savedBrightness)
                } else {
                    currentState
                }
            }
        }

        // Observe presets from DataStore
        viewModelScope.launch {
            useCases.observePresets().collect { presets ->
                _state.update {
                    it.copy(
                        presets = presets.map { p ->
                            p.toUiItem()
                        }
                    )
                }
            }
        }

        // Observe physical connection status
        viewModelScope.launch {
            useCases.observeConnectionStatus().collect { status ->
                _state.update { it.copy(connectionStatus = status) }
            }
        }

        // Dedicated single-flight coroutine for socket transmissions
        viewModelScope.launch {
            colorUpdateChannel
                .conflate()
                .collect { (color, brightness) ->
                    val r = (color.red * 255).roundToInt().coerceIn(0, 255)
                    val g = (color.green * 255).roundToInt().coerceIn(0, 255)
                    val b = (color.blue * 255).roundToInt().coerceIn(0, 255)
                    useCases.setColor(r, g, b, brightness)
                    useCases.saveLastBrightness(brightness)
                }
        }

        // Connect on startup
        sendIntent(LedIntent.Connect)
    }

    fun sendIntent(intent: LedIntent) {
        when (intent) {
            is LedIntent.Connect -> handleConnect(forceRediscovery = false)
            is LedIntent.RetryConnection -> handleConnect(forceRediscovery = true)
            is LedIntent.TogglePower -> handleTogglePower(intent.isOn)
            is LedIntent.ChangeColor -> handleChangeColor(intent.color)
            is LedIntent.ChangeBrightness -> handleChangeBrightness(intent.brightness)
            is LedIntent.SelectPreset -> handleSelectPreset(intent.preset)
            is LedIntent.OpenSheet -> _state.update { it.copy(activeSheetMode = intent.mode) }
            is LedIntent.DismissSheet -> _state.update { it.copy(activeSheetMode = null) }
            is LedIntent.SavePreset -> handleSavePreset(intent.preset)
            is LedIntent.DeletePreset -> handleDeletePreset(intent.presetId)
        }
    }

    private fun handleConnect(
        forceRediscovery: Boolean
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val result = useCases.connect(forceRediscovery)
            result.onSuccess { deviceState ->
                val maxChannel = maxOf(
                    deviceState.red,
                    deviceState.green,
                    deviceState.blue
                )
                if (maxChannel > 0) {
                    val liveBrightness = maxChannel / 255f
                    val normalizedColor = Color(
                        red = (deviceState.red / 255f / liveBrightness).coerceIn(0f, 1f),
                        green = (deviceState.green / 255f / liveBrightness).coerceIn(0f, 1f),
                        blue = (deviceState.blue / 255f / liveBrightness).coerceIn(0f, 1f)
                    )
                    _state.update {
                        it.copy(
                            isOn = deviceState.isOn,
                            selectedColor = normalizedColor,
                            brightness = liveBrightness,
                            isBusy = false
                        )
                    }
                    viewModelScope.launch {
                        useCases.saveLastBrightness(liveBrightness)
                    }
                } else {
                    _state.update {
                        it.copy(
                            isOn = deviceState.isOn,
                            isBusy = false
                        )
                    }
                }
            }.onFailure { error ->
                _state.update { it.copy(isBusy = false) }
                _effect.send(LedEffect.ShowSnackbar("Could not connect to LED strip: ${error.message ?: "Offline"}"))
            }
        }
    }

    private fun handleTogglePower(
        isOn: Boolean
    ) {
        _state.update { it.copy(isOn = isOn) }
        viewModelScope.launch {
            val result = useCases.setPower(isOn)
            result.onFailure {
                _effect.send(LedEffect.ShowSnackbar("Failed to send power command"))
            }
        }
    }

    private fun handleChangeColor(
        color: Color
    ) {
        _state.update {
            it.copy(selectedColor = color)
        }
        colorUpdateChannel.tryEmit(Pair(color, _state.value.brightness))
    }

    private fun handleChangeBrightness(
        brightness: Float
    ) {
        _state.update {
            it.copy(brightness = brightness)
        }
        colorUpdateChannel.tryEmit(Pair(_state.value.selectedColor, brightness))
    }

    private fun handleSelectPreset(
        preset: LedPresetItem
    ) {
        val newBrightness = preset.brightness ?: _state.value.brightness
        _state.update {
            it.copy(
                selectedColor = preset.color,
                brightness = newBrightness
            )
        }
        colorUpdateChannel.tryEmit(Pair(preset.color, newBrightness))
    }

    private fun handleSavePreset(
        preset: LedPresetItem
    ) {
        viewModelScope.launch {
            useCases.savePreset(preset.toDomain())
        }
    }

    private fun handleDeletePreset(
        presetId: String
    ) {
        viewModelScope.launch {
            useCases.deletePreset(presetId)
        }
    }
}
