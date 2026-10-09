package org.example.desky.devices.esp.domain.usecase

import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class DiscoverEspUseCase(
    private val repository: EspRelayRepository
) {
    suspend operator fun invoke(): Result<DeviceEndpoint> = repository.discover()
}
