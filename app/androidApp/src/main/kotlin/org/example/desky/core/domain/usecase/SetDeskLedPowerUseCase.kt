package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.DeskStateRepository

class SetDeskLedPowerUseCase(
    private val repository: DeskStateRepository
) {
    suspend operator fun invoke(isOn: Boolean): Result<Unit> = repository.setLedPower(isOn)
}
