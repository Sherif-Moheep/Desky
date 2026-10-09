package org.example.desky.devices.esp.domain.usecase

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class CheckEspStatusUseCase(
    private val repository: EspRelayRepository
) {
    suspend operator fun invoke(): DeviceConnectionStatus = repository.checkStatus()
}
