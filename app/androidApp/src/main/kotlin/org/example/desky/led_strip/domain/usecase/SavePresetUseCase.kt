package org.example.desky.led_strip.domain.usecase

import org.example.desky.led_strip.domain.model.LedPreset
import org.example.desky.led_strip.domain.repository.LedPresetRepository

class SavePresetUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        preset: LedPreset
    ): Result<Unit> = repository.savePreset(preset)
}
