package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.SettingsRepository

class UpdateEspEndpointUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(host: String, port: Int) = repository.updateEspEndpoint(host, port)
}
