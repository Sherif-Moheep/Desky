package org.example.desky.devices.led.presentation.contract

import androidx.compose.ui.graphics.Color
import org.example.desky.devices.led.presentation.model.LedPresetItem
import org.example.desky.devices.led.presentation.model.PresetSheetMode

sealed interface LedIntent {
    data object Connect : LedIntent
    data object RetryConnection : LedIntent
    data object Refresh : LedIntent
    data class TogglePower(val isOn: Boolean) : LedIntent
    data class ChangeColor(val color: Color) : LedIntent
    data class ChangeBrightness(val brightness: Float) : LedIntent
    data class SelectPreset(val preset: LedPresetItem) : LedIntent
    data class OpenSheet(val mode: PresetSheetMode) : LedIntent
    data object DismissSheet : LedIntent
    data class SavePreset(val preset: LedPresetItem) : LedIntent
    data class DeletePreset(val presetId: String) : LedIntent
}
