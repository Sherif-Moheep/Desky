package org.example.desky.devices.laptop.presentation.contract

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.devices.laptop.presentation.model.LaptopTelemetryState


data class LaptopState(
    val powerState: PowerState = PowerState.ON,
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
        get() = connectionStatus is DeviceConnectionStatus.Online && powerState == PowerState.ON
}
