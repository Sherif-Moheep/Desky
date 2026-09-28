package org.example.desky.devices.led.presentation.model

sealed interface PresetSheetMode {
    data object Create : PresetSheetMode
    data class Edit(val preset: LedPresetItem) : PresetSheetMode
}
