package org.example.desky.devices.led_strip.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.led_strip.domain.repository.LedControllerRepository

class ObserveConnectionStatusUseCase(
    private val repository: LedControllerRepository
) {
    operator fun invoke(): StateFlow<DeviceConnectionStatus> = repository.connectionStatus
}
