package org.example.desky.devices.esp.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class ObserveEspConnectionStatusUseCase(
    private val repository: EspRelayRepository
) {
    operator fun invoke(): StateFlow<DeviceConnectionStatus> = repository.connectionStatus
}
