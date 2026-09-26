package org.example.desky.devices.laptop.presentation.contract

sealed interface LaptopIntent {
    data object PowerClick : LaptopIntent
    data object RequestShutdown : LaptopIntent
    data object ConfirmShutdown : LaptopIntent
    data object DismissShutdownDialog : LaptopIntent

    data object RequestRestart : LaptopIntent
    data object ConfirmRestart : LaptopIntent
    data object DismissRestartDialog : LaptopIntent

    data object Lock : LaptopIntent
    data object Sleep : LaptopIntent
    data object RetryConnection : LaptopIntent
    data object RefreshStatus : LaptopIntent
}
