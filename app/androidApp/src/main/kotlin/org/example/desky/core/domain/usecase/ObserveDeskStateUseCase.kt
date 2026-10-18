package org.example.desky.core.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeskSnapshot
import org.example.desky.core.domain.repository.DeskStateRepository

class ObserveDeskStateUseCase(
    private val repository: DeskStateRepository
) {
    operator fun invoke(): StateFlow<DeskSnapshot> = repository.deskState
}
