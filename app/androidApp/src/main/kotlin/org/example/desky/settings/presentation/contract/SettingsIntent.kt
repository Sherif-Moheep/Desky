package org.example.desky.settings.presentation.contract

import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.model.DeviceType

sealed interface SettingsIntent {
    data class UpdateEspHost(val host: String) : SettingsIntent
    data class UpdateEspPort(val port: String) : SettingsIntent
    data class UpdateLaptopHost(val host: String) : SettingsIntent
    data class UpdateLaptopPort(val port: String) : SettingsIntent
    data class UpdateLedHost(val host: String) : SettingsIntent
    data class UpdateLedPort(val port: String) : SettingsIntent
    data class SetThemeMode(val mode: AppThemeMode) : SettingsIntent
    data class PingDevice(val device: DeviceType) : SettingsIntent
    data object PingAllDevices : SettingsIntent
    data object ShowResetDialog : SettingsIntent
    data object DismissResetDialog : SettingsIntent
    data object ConfirmReset : SettingsIntent
}
