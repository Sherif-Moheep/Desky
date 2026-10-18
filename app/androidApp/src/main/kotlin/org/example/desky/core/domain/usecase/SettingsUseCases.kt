package org.example.desky.core.domain.usecase

data class SettingsUseCases(
    val observeSettings: ObserveSettingsUseCase,
    val observeThemeMode: ObserveThemeModeUseCase,
    val updateEspEndpoint: UpdateEspEndpointUseCase,
    val updateLaptopEndpoint: UpdateLaptopEndpointUseCase,
    val updateLedEndpoint: UpdateLedEndpointUseCase,
    val updateThemeMode: UpdateThemeModeUseCase,
    val resetToDefaults: ResetSettingsToDefaultsUseCase
)
