package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.DeskStateRepository

class PowerOnDeskUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.powerOnDesk()
}
