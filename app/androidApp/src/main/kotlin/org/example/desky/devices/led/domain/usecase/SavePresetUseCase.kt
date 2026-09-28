package org.example.desky.devices.led.domain.usecase

import org.example.desky.devices.led.domain.model.LedPreset
import org.example.desky.devices.led.domain.repository.LedPresetRepository

class SavePresetUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        preset: LedPreset
    ): Result<Unit> = repository.savePreset(preset)
}
