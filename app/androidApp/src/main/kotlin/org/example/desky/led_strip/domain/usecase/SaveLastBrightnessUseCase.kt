package org.example.desky.led_strip.domain.usecase

import org.example.desky.led_strip.domain.repository.LedPresetRepository

class SaveLastBrightnessUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        brightness: Float
    ): Result<Unit> = repository.saveLastBrightness(brightness)
}
