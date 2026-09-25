package org.example.desky.home.presentation.model

data class HomeTelemetryUiState(
    val laptop: LaptopTelemetry = LaptopTelemetry(),
    val light: LightTelemetry = LightTelemetry(),
    val relay: RelayTelemetry = RelayTelemetry(),
    val network: NetworkTelemetry = NetworkTelemetry()
)

data class LaptopTelemetry(
    val isOnline: Boolean = true,
    val isLocked: Boolean = true,
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
    val isOnline: Boolean = true,
    val isOn: Boolean = true,
    val brightnessPercent: Int = 80,
    val colorName: String = "Crimson",
    val label: String = "Ambient Light"
) {
    val statusText: String
        get() = when {
            !isOnline -> "Offline"
            !isOn -> "Online · Off"
            else -> "$brightnessPercent% · $colorName"
        }
}

data class RelayTelemetry(
    val isOnline: Boolean = true,
    val isAcActive: Boolean = true,
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
    val isConnected: Boolean = true,
    val latencyMs: Int = 12
) {
    val statusText: String
        get() = if (isConnected) "LAN (${latencyMs}ms)" else "No LAN"
    val label: String
        get() = if (isConnected) "Connected" else "Disconnected"
}
