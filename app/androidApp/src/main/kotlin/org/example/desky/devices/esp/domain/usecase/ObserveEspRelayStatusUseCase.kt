package org.example.desky.devices.esp.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.devices.esp.domain.model.EspRelayStatus
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class ObserveEspRelayStatusUseCase(
    private val repository: EspRelayRepository
) {
    operator fun invoke(): StateFlow<EspRelayStatus?> = repository.relayStatus
}
