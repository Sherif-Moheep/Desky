package org.example.desky.devices.led.domain.model

data class LedDeviceState(
    val isOn: Boolean,
    val red: Int,
    val green: Int,
    val blue: Int,
    val warmWhite: Int = 0
)