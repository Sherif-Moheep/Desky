package org.example.desky.home.presentation.contract

import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.home.presentation.model.DeskPowerState
import org.example.desky.home.presentation.model.HomeTelemetryUiState

data class HomeState(
    val powerState: DeskPowerState = DeskPowerState.ON,
    val showPowerOffDialog: Boolean = false,
    val isRefreshing: Boolean = false,
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.Online(12),
    val telemetry: HomeTelemetryUiState = HomeTelemetryUiState()
) {
    val isDeskOn: Boolean
        get() = powerState == DeskPowerState.ON

    val isAllOnline: Boolean
        get() = isDeskOn && telemetry.laptop.isOnline && telemetry.light.isOn && telemetry.network.isConnected
}
