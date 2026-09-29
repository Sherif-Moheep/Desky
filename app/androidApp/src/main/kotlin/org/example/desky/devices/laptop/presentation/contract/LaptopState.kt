package org.example.desky.devices.laptop.presentation.contract

import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.model.LaptopTelemetryDto


data class LaptopState(
    val powerState: PowerState = PowerState.ON,
    val connectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.Online(12),
    val telemetry: LaptopTelemetryDto? = null,
    val showShutdownDialog: Boolean = false,
    val showRestartDialog: Boolean = false,
    val isBusy: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val isOnline: Boolean
        get() = connectionStatus is DeviceConnectionStatus.Online && powerState == PowerState.ON
}
