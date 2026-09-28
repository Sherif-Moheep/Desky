package org.example.desky.devices.led.domain.usecase

import org.example.desky.devices.led.domain.model.LedDeviceState
import org.example.desky.devices.led.domain.repository.LedControllerRepository

class ConnectLedUseCase(
    private val repository: LedControllerRepository
) {
    suspend operator fun invoke(
        forceRediscovery: Boolean = false
    ): Result<LedDeviceState> {
        return repository.connect(forceRediscovery)
    }
}
