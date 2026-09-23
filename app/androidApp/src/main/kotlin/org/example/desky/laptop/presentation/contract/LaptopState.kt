package org.example.desky.laptop.presentation.contract

import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.laptop.presentation.model.LaptopPowerState
import org.example.desky.laptop.presentation.model.LaptopTelemetryState

data class LaptopState(
    val powerState: LaptopPowerState = LaptopPowerState.ON,
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.Online(12),
    val telemetry: LaptopTelemetryState = LaptopTelemetryState(
        isLocked = false,
        isAcConnected = true,
        batteryPercent = 95,
        uptimeFormatted = "2h 45m"
    ),
    val showShutdownDialog: Boolean = false,
    val showRestartDialog: Boolean = false,
    val isBusy: Boolean = false
) {
    val isOnline: Boolean
        get() = connectionStatus is DeviceConnectionStatus.Online && powerState == LaptopPowerState.ON
}
