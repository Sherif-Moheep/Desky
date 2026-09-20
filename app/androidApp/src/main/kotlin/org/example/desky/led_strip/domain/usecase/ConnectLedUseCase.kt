package org.example.desky.led_strip.domain.usecase

import org.example.desky.led_strip.domain.model.LedDeviceState
import org.example.desky.led_strip.domain.repository.LedControllerRepository

class ConnectLedUseCase(
    private val repository: LedControllerRepository
) {
    suspend operator fun invoke(
        forceRediscovery: Boolean = false
    ): Result<LedDeviceState> {
        return repository.connect(forceRediscovery)
    }
}
