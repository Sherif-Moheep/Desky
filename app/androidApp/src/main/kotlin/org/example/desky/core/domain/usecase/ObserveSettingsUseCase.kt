package org.example.desky.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.repository.SettingsRepository

class ObserveSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.settingsFlow
}
