package org.example.desky.home.presentation.contract

sealed interface HomeEffect {
    data class ShowSnackbar(val message: String) : HomeEffect
}
