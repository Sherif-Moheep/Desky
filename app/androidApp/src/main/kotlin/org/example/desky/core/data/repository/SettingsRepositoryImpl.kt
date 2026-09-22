package org.example.desky.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.example.desky.core.data.datasource.SettingsDataStore
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.core.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val settingsFlow: Flow<AppSettings> = dataStore.settingsFlow

    override val themeModeFlow: Flow<AppThemeMode> = dataStore.settingsFlow
        .map { appSettings ->
            appSettings.themeMode
        }
        .distinctUntilChanged()

    override val espEndpointFlow: Flow<DeviceEndpoint> = dataStore.settingsFlow
        .map { appSettings ->
            appSettings.espRelay
        }
        .distinctUntilChanged()

    override val laptopEndpointFlow: Flow<DeviceEndpoint> = dataStore.settingsFlow
        .map { appSettings ->
            appSettings.laptop
        }
        .distinctUntilChanged()

    override val ledEndpointFlow: Flow<DeviceEndpoint> = dataStore.settingsFlow
        .map { appSettings ->
            appSettings.ledStrip
        }
        .distinctUntilChanged()

    override suspend fun updateEspEndpoint(
        host: String,
        port: Int
    ) {
        dataStore.saveEspEndpoint(
            host.trim(),
            port
        )
    }

    override suspend fun updateLaptopEndpoint(
        host: String,
        port: Int
    ) {
        dataStore.saveLaptopEndpoint(
            host.trim(),
            port
        )
    }

    override suspend fun updateLedEndpoint(
        host: String,
        port: Int
    ) {
        dataStore.saveLedEndpoint(
            host.trim(), port
        )
    }

    override suspend fun updateThemeMode(
        mode: AppThemeMode
    ) {
        dataStore.saveThemeMode(mode)
    }

    override suspend fun resetToDefaults() {
        dataStore.clear()
    }
}
