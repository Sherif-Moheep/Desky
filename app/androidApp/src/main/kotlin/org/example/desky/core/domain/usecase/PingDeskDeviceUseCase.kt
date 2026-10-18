package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceType
import org.example.desky.core.domain.repository.DeskStateRepository

class PingDeskDeviceUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(device: DeviceType): DeviceConnectionStatus = repository.pingDevice(device)
}
