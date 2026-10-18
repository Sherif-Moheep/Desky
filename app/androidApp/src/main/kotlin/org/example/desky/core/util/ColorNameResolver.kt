package org.example.desky.core.util

object ColorNameResolver {
    fun resolve(
        red: Int,
        green: Int,
        blue: Int
    ): String {
        val maxChannel = maxOf(red, green, blue)
        if (maxChannel == 0) return "Off"

        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f

        val minChannel = minOf(r, g, b)
        val max = maxOf(r, g, b)
        val delta = max - minChannel

        val saturation = if (max == 0f) 0f else delta / max
        if (saturation < 0.15f) {
            return if (r > b) "Warm White" else "Cool White"
        }

        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0) it + 360f else it }

        return when (hue) {
            in 0f..15f -> "Red"
            in 15f..45f -> "Amber"
            in 45f..70f -> "Yellow"
            in 70f..165f -> "Green"
            in 165f..200f -> "Teal"
            in 200f..260f -> "Blue"
            in 260f..300f -> "Purple"
            in 300f..345f -> "Pink"
            else -> "Crimson"
        }
    }
}
