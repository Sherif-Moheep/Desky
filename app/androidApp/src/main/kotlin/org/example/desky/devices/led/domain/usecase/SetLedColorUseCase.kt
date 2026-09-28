package org.example.desky.devices.led.domain.usecase

import kotlin.math.roundToInt

class SetLedColorUseCase(
    private val repository: org.example.desky.devices.led.domain.repository.LedControllerRepository
) {
    /**
     * Scales RGB channels by brightness factor (0.0f..1.0f) for 4-pin analog strips.
     */
    suspend operator fun invoke(
        r: Int,
        g: Int,
        b: Int,
        brightness: Float = 1.0f
    ): Result<Unit> {
        val factor = brightness.coerceIn(0f, 1f)
        val scaledR = (r * factor).roundToInt().coerceIn(0, 255)
        val scaledG = (g * factor).roundToInt().coerceIn(0, 255)
        val scaledB = (b * factor).roundToInt().coerceIn(0, 255)
        return repository.setRgb(scaledR, scaledG, scaledB)
    }
}
