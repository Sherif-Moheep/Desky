package org.example.desky.devices.laptop.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.laptop.domain.repository.LaptopRepository

class ObserveLaptopConnectionStatusUseCase(
    private val repository: LaptopRepository
) {
    operator fun invoke(): StateFlow<DeviceConnectionStatus> = repository.connectionStatus
}
