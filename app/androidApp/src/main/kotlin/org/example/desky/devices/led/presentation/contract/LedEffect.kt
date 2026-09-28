package org.example.desky.devices.led.presentation.contract

sealed interface LedEffect {
    data class ShowSnackbar(val message: String) : LedEffect
}
