package org.example.desky.led_strip.presentation.contract

import androidx.compose.ui.graphics.Color
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.led_strip.presentation.model.LedPresetItem
import org.example.desky.led_strip.presentation.model.PresetSheetMode

data class LedState(
    val isOn: Boolean = false,
    val selectedColor: Color = Color(0xFFFF9E3B),
    val brightness: Float = 0.85f,
    val presets: List<LedPresetItem> = emptyList(),
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val activeSheetMode: PresetSheetMode? = null,
    val isBusy: Boolean = false
)
