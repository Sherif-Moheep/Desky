package org.example.desky.home.presentation.model

data class HomeTelemetryUiState(
    val laptop: LaptopTelemetry = LaptopTelemetry(),
    val light: LightTelemetry = LightTelemetry(),
    val relay: RelayTelemetry = RelayTelemetry(),
    val network: NetworkTelemetry = NetworkTelemetry()
)

data class LaptopTelemetry(
    val isOnline: Boolean = false,
    val isLocked: Boolean = false,
    val deviceName: String = "Laptop Workstation"
) {
    val statusText: String
        get() = when {
            !isOnline -> "Offline"
            isLocked -> "Online · Locked"
            else -> "Online · Unlocked"
        }
}

data class LightTelemetry(
    val isOnline: Boolean = false,
    val isOn: Boolean = false,
    val brightnessPercent: Int = 0,
    val colorName: String = "--",
    val label: String = "Ambient Light"
) {
    val statusText: String
        get() = when {
            !isOnline -> "Offline"
            !isOn -> "Online · Off"
            colorName.isNotBlank() && colorName != "--" -> "$brightnessPercent% · $colorName"
            else -> "$brightnessPercent%"
        }
}

data class RelayTelemetry(
    val isOnline: Boolean = false,
    val isAcActive: Boolean = false,
    val label: String = "Laptop Charger Relay"
) {
    val statusText: String
        get() = when {
            !isOnline -> "Offline"
            !isAcActive -> "Online · AC Cut"
            else -> "Online · AC Active"
        }
}

data class NetworkTelemetry(
    val isConnected: Boolean = false,
    val latencyMs: Int = 0
) {
    val statusText: String
        get() = if (isConnected) "LAN (${latencyMs}ms)" else "No LAN"
    val label: String
        get() = if (isConnected) "Connected" else "Disconnected"
}
