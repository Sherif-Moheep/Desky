package org.example.desky.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.model.DeviceEndpoint

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    val themeModeFlow: Flow<AppThemeMode>
    val espEndpointFlow: Flow<DeviceEndpoint>
    val laptopEndpointFlow: Flow<DeviceEndpoint>
    val ledEndpointFlow: Flow<DeviceEndpoint>

    suspend fun updateEspEndpoint(host: String, port: Int)
    suspend fun updateLaptopEndpoint(host: String, port: Int)
    suspend fun updateLedEndpoint(host: String, port: Int)
    suspend fun updateThemeMode(mode: AppThemeMode)
    suspend fun resetToDefaults()
}
