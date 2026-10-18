package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.model.DeskSnapshot
import org.example.desky.core.domain.repository.DeskStateRepository

class RefreshDeskStateUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(): DeskSnapshot = repository.refreshAll()
}
