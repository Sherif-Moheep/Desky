package org.example.desky.devices.list.presentation.contract

sealed interface DevicesIntent {
    data object Refresh : DevicesIntent
    data object LaptopPowerClick : DevicesIntent
    data object ConfirmLaptopShutdown : DevicesIntent
    data object DismissLaptopShutdownDialog : DevicesIntent
    data class ToggleLedPower(val targetOn: Boolean) : DevicesIntent
}
