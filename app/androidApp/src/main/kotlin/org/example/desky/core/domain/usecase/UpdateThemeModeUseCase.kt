package org.example.desky.core.domain.usecase

import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.repository.SettingsRepository

class UpdateThemeModeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: AppThemeMode) = repository.updateThemeMode(mode)
}
