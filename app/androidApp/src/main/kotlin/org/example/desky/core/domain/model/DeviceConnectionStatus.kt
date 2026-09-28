package org.example.desky.core.domain.model

sealed interface DeviceConnectionStatus {
    data object Idle : DeviceConnectionStatus
    data object Pinging : DeviceConnectionStatus
    data class Online(val latencyMs: Long) : DeviceConnectionStatus
    data class Offline(val message: String = "Offline") : DeviceConnectionStatus
}
