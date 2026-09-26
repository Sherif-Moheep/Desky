package org.example.desky.devices.presentation.contract

sealed interface DevicesEffect {
    data class ShowSnackbar(val message: String) : DevicesEffect
}
