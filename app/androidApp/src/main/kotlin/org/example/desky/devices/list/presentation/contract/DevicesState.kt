package org.example.desky.devices.list.presentation.contract

import androidx.compose.ui.graphics.Color
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState

data class DevicesState(
    // Laptop state
    val laptopHost: String = "desky-laptop.local",
    val laptopPort: Int = 8080,
    val laptopStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val laptopPowerState: PowerState = PowerState.ON,
    val isLaptopLocked: Boolean = true,
    val laptopUptime: String = "3h 42m",
    val showLaptopShutdownDialog: Boolean = false,

    // ESP32 Relay state
    val espHost: String = "desky-relay.local",
    val espPort: Int = 80,
    val espStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,

    // LED Strip state
    val ledHost: String = "192.168.1.150",
    val ledPort: Int = 5577,
    val ledStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val isLedPoweredOn: Boolean = true,
    val ledBrightness: Float = 0.8f,
    val ledColor: Color = Color(0xFFE53935),
    val ledColorName: String = "Crimson",

    // Screen state
    val isRefreshing: Boolean = false
) {
    val onlineCount: Int
        get() = (if (laptopStatus is DeviceConnectionStatus.Online) 1 else 0) +
                (if (ledStatus is DeviceConnectionStatus.Online) 1 else 0)

    val totalDevicesCount: Int = 2
}
