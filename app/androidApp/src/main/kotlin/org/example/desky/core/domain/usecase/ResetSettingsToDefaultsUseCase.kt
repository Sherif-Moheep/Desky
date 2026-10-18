package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.repository.SettingsRepository

class ResetSettingsToDefaultsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.resetToDefaults()
}
