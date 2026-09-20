package org.example.desky.led_strip.presentation.model

import androidx.compose.ui.graphics.Color
import org.example.desky.led_strip.domain.model.LedPreset
import kotlin.math.roundToInt

fun LedPreset.toUiItem(): LedPresetItem = LedPresetItem(
    id = id,
    name = name,
    color = Color(red, green, blue),
    brightness = brightness
)

fun LedPresetItem.toDomain(): LedPreset {
    val r = (color.red * 255).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255).roundToInt().coerceIn(0, 255)
    return LedPreset(
        id = id,
        name = name,
        red = r,
        green = g,
        blue = b,
        brightness = brightness
    )
}
