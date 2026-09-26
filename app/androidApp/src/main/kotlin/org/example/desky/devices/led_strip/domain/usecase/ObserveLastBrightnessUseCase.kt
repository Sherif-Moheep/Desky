package org.example.desky.devices.led_strip.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.desky.devices.led_strip.domain.repository.LedPresetRepository

class ObserveLastBrightnessUseCase(
    private val repository: LedPresetRepository
) {
    operator fun invoke(): Flow<Float> = repository.getLastBrightness()
}
