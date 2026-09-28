package org.example.desky.settings.presentation.contract

import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.model.DeviceConnectionStatus

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
    val showResetDialog: Boolean = false,
    val isRefreshing: Boolean = false
)
