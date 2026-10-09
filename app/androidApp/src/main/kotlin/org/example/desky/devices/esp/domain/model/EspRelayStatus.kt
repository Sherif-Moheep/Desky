package org.example.desky.devices.esp.domain.model

data class EspRelayStatus(
    val isOnline: Boolean,
    val isOn: Boolean,
    val uptimeSeconds: Long,
    val wifiRssi: Int,
    val ip: String
)
