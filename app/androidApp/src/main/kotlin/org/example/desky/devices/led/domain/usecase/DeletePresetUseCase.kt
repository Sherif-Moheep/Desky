package org.example.desky.devices.led.domain.usecase

import org.example.desky.devices.led.domain.repository.LedPresetRepository

class DeletePresetUseCase(
    private val repository: LedPresetRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Unit> = repository.deletePreset(id)
}
