package org.example.desky.devices.led_strip.presentation.model

sealed interface PresetSheetMode {
    data object Create : PresetSheetMode
    data class Edit(val preset: LedPresetItem) : PresetSheetMode
}
