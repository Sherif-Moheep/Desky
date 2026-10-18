package org.example.desky.devices.led.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.devices.led.domain.model.LedDeviceState
import org.example.desky.devices.led.domain.repository.LedControllerRepository

class ObserveLedDeviceStateUseCase(
    private val repository: LedControllerRepository
) {
    operator fun invoke(): StateFlow<LedDeviceState?> = repository.deviceState
}
