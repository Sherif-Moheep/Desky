package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.DeskStateRepository

class PowerOffDeskUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.powerOffDesk()
}
