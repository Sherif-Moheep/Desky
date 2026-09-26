package org.example.desky.devices.laptop.presentation.contract

sealed interface LaptopEffect {
    data class ShowSnackbar(val message: String) : LaptopEffect
}
