package org.example.desky.settings.presentation.contract

import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.domain.model.AppThemeMode

enum class DeviceType {
    ESP_RELAY,
    LAPTOP,
    LED_STRIP
}

data class SettingsState(
    val espHost: String = "",
    val espPort: String = "",
    val espStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val laptopHost: String = "",
    val laptopPort: String = "",
    val laptopStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val ledHost: String = "",
    val ledPort: String = "",
    val ledStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val showResetDialog: Boolean = false
)

sealed interface SettingsIntent {
    data class UpdateEspHost(val host: String) : SettingsIntent
    data class UpdateEspPort(val port: String) : SettingsIntent
    data class UpdateLaptopHost(val host: String) : SettingsIntent
    data class UpdateLaptopPort(val port: String) : SettingsIntent
    data class UpdateLedHost(val host: String) : SettingsIntent
    data class UpdateLedPort(val port: String) : SettingsIntent
    data class SetThemeMode(val mode: AppThemeMode) : SettingsIntent
    data class PingDevice(val device: DeviceType) : SettingsIntent
    data object ShowResetDialog : SettingsIntent
    data object DismissResetDialog : SettingsIntent
    data object ConfirmReset : SettingsIntent
}

sealed interface SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect
}
