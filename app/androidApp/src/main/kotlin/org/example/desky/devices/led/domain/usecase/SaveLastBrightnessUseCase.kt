package org.example.desky.devices.led.domain.usecase

import org.example.desky.devices.led.domain.repository.LedPresetRepository

class SaveLastBrightnessUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        brightness: Float
    ): Result<Unit> = repository.saveLastBrightness(brightness)
}
