package org.example.desky.devices.esp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.desky.devices.esp.domain.model.EspRelayStatus

@Serializable
data class EspRelayDto(
    val status: String,
    @SerialName("is_on")
    val isOn: Boolean,
    @SerialName("uptime_seconds")
    val uptimeSeconds: Long,
    @SerialName("wifi_rssi")
    val wifiRssi: Int,
    val ip: String
) {
    fun toDomain(): EspRelayStatus = EspRelayStatus(
        isOnline = status.equals("online", ignoreCase = true),
        isOn = isOn,
        uptimeSeconds = uptimeSeconds,
        wifiRssi = wifiRssi,
        ip = ip
    )
}
