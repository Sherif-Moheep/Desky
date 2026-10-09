package org.example.desky.devices.esp.domain.usecase

import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class PowerOnEspUseCase(
    private val repository: EspRelayRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.powerOn()
}
