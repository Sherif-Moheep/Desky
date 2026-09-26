package org.example.desky.devices.laptop.presentation.model

data class LaptopTelemetryState(
    val isLocked: Boolean = true,
    val isAcConnected: Boolean = true,
    val batteryPercent: Int = 100,
    val uptimeFormatted: String = "3h 42m"
)
