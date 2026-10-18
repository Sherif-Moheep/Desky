package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.DeskStateRepository

class ShutdownDeskLaptopUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.shutdownLaptop()
}
