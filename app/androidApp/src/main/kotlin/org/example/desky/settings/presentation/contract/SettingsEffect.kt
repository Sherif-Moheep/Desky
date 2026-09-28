package org.example.desky.settings.presentation.contract

sealed interface SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect
}
