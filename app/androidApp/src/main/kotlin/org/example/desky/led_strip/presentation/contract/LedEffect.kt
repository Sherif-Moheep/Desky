package org.example.desky.led_strip.presentation.contract

sealed interface LedEffect {
    data class ShowSnackbar(val message: String) : LedEffect
}
