package org.example.desky.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.repository.SettingsRepository

class ObserveThemeModeUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppThemeMode> = repository.themeModeFlow
}
