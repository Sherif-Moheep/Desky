package org.example.desky.led_strip.domain.usecase

import org.example.desky.led_strip.domain.repository.LedPresetRepository

class DeletePresetUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Unit> = repository.deletePreset(id)
}
