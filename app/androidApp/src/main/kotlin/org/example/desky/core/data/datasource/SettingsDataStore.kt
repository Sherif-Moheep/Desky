package org.example.desky.core.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.desky.core.domain.model.AppSettings
import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.model.DeviceEndpoint

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "desky_settings_prefs")

class SettingsDataStore(
    private val context: Context
) {
    companion object {
        val KEY_ESP_HOST = stringPreferencesKey("esp_host")
        val KEY_ESP_PORT = intPreferencesKey("esp_port")
        val KEY_LAPTOP_HOST = stringPreferencesKey("laptop_host")
        val KEY_LAPTOP_PORT = intPreferencesKey("laptop_port")
        val KEY_LED_HOST = stringPreferencesKey("led_host")
        val KEY_LED_PORT = intPreferencesKey("led_port")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            espRelay = DeviceEndpoint(
                host = prefs[KEY_ESP_HOST]?.takeIf { it.isNotBlank() } ?: defaults.espRelay.host,
                port = prefs[KEY_ESP_PORT] ?: defaults.espRelay.port
            ),
            laptop = DeviceEndpoint(
                host = prefs[KEY_LAPTOP_HOST]?.takeIf { it.isNotBlank() } ?: defaults.laptop.host,
                port = prefs[KEY_LAPTOP_PORT] ?: defaults.laptop.port
            ),
            ledStrip = DeviceEndpoint(
                host = prefs[KEY_LED_HOST]?.takeIf { it.isNotBlank() } ?: defaults.ledStrip.host,
                port = prefs[KEY_LED_PORT] ?: defaults.ledStrip.port
            ),
            themeMode = prefs[KEY_THEME_MODE]?.let { name ->
                runCatching { AppThemeMode.valueOf(name) }.getOrNull()
            } ?: defaults.themeMode
        )
    }

    suspend fun saveEspEndpoint(
        host: String,
        port: Int
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_ESP_HOST] = host
            prefs[KEY_ESP_PORT] = port
        }
    }

    suspend fun saveLaptopEndpoint(
        host: String,
        port: Int
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_LAPTOP_HOST] = host
            prefs[KEY_LAPTOP_PORT] = port
        }
    }

    suspend fun saveLedEndpoint(
        host: String,
        port: Int
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_LED_HOST] = host
            prefs[KEY_LED_PORT] = port
        }
    }

    suspend fun saveThemeMode(
        mode: AppThemeMode
    ) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun clear() {
        context.settingsDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
