package org.example.desky.devices.laptop.domain.usecase

import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.devices.laptop.domain.repository.LaptopRepository

class DiscoverLaptopUseCase(
    private val repository: LaptopRepository
) {
    suspend operator fun invoke(): Result<DeviceEndpoint> = repository.discover()
}
