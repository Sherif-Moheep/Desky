package org.example.desky.devices.led.domain.model

data class LedPreset(
    val id: String,
    val name: String,
    val red: Int,
    val green: Int,
    val blue: Int,
    val brightness: Float? = null
)