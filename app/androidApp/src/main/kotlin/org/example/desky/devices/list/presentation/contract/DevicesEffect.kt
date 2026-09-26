package org.example.desky.devices.list.presentation.contract

sealed interface DevicesEffect {
    data class ShowSnackbar(val message: String) : DevicesEffect
}
