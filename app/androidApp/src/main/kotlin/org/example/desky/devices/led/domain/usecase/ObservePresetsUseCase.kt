package org.example.desky.devices.led.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.desky.devices.led.domain.model.LedPreset
import org.example.desky.devices.led.domain.repository.LedPresetRepository

class ObservePresetsUseCase(
    private val repository: LedPresetRepository
) {
    operator fun invoke(): Flow<List<LedPreset>> = repository.getPresets()
}
