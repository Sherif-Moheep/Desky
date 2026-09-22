package org.example.desky.core.domain.model

data class AppSettings(
    val espRelay: DeviceEndpoint = DeviceEndpoint(host = "desky-relay.local", port = 80),
    val laptop: DeviceEndpoint = DeviceEndpoint(host = "desky-laptop.local", port = 8080),
    val ledStrip: DeviceEndpoint = DeviceEndpoint(host = "192.168.1.150", port = 5577),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)
