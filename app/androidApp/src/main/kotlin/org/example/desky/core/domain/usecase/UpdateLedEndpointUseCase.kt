package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.SettingsRepository

class UpdateLedEndpointUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(host: String, port: Int) = repository.updateLedEndpoint(host, port)
}
