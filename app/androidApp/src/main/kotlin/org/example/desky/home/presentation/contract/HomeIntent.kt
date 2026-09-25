package org.example.desky.home.presentation.contract

sealed interface HomeIntent {
    data object ToggleDeskPower : HomeIntent
    data object ConfirmPowerOff : HomeIntent
    data object DismissPowerOffDialog : HomeIntent
    data object RefreshTelemetry : HomeIntent
    data object RetryConnection : HomeIntent
}
