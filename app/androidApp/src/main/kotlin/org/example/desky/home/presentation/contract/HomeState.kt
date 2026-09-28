package org.example.desky.home.presentation.contract

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.home.presentation.model.HomeTelemetryUiState

data class HomeState(
    val powerState: PowerState = PowerState.ON,
    val showPowerOffDialog: Boolean = false,
    val isRefreshing: Boolean = false,
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.Online(12),
    val telemetry: HomeTelemetryUiState = HomeTelemetryUiState()
) {
    val isDeskOn: Boolean
        get() = powerState == PowerState.ON

    val isAllOnline: Boolean
        get() = isDeskOn && telemetry.laptop.isOnline && telemetry.light.isOn && telemetry.network.isConnected
}
