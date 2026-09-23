package org.example.desky.laptop.presentation.contract

sealed interface LaptopEffect {
    data class ShowSnackbar(val message: String) : LaptopEffect
}
