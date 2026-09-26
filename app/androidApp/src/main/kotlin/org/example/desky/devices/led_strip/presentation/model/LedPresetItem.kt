package org.example.desky.devices.led_strip.presentation.model

import androidx.compose.ui.graphics.Color

data class LedPresetItem(
    val id: String,
    val name: String,
    val color: Color,
    val brightness: Float? = null
)

val PreviewLedPresets = listOf(
    LedPresetItem(id = "amber", name = "Warm Amber", color = Color(0xFFFF9E3B), brightness = 0.65f),
    LedPresetItem(id = "focus", name = "Cool White", color = Color(0xFFEBF5FF), brightness = 1.0f),
    LedPresetItem(id = "cyber", name = "Cyber Blue", color = Color(0xFF00E5FF), brightness = 0.85f),
    LedPresetItem(id = "night", name = "Night Shift", color = Color(0xFFFF453A), brightness = 0.25f)
)
