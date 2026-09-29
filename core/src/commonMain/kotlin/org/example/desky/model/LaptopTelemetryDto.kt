package org.example.desky.model

import kotlinx.serialization.Serializable

@Serializable
data class LaptopTelemetryDto(
    val isLocked: Boolean,
    val isAcConnected: Boolean,
    val batteryPercent: Int,
    val uptimeFormatted: String
)
