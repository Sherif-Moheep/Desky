package org.example.desky.devices.laptop.domain.usecase

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.laptop.domain.repository.LaptopRepository

class CheckLaptopStatusUseCase(
    private val repository: LaptopRepository
) {
    suspend operator fun invoke(): DeviceConnectionStatus = repository.checkStatus()
}
