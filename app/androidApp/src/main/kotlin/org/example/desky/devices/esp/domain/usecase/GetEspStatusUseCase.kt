package org.example.desky.devices.esp.domain.usecase

import org.example.desky.devices.esp.domain.model.EspRelayStatus
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class GetEspStatusUseCase(
    private val repository: EspRelayRepository
) {
    suspend operator fun invoke(): Result<EspRelayStatus> = repository.getStatus()
}
