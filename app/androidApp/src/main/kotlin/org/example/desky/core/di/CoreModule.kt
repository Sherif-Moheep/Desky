package org.example.desky.core.di

import org.example.desky.core.data.datasource.SettingsDataStore
import org.example.desky.core.data.repository.DeskStateRepositoryImpl
import org.example.desky.core.data.repository.SettingsRepositoryImpl
import org.example.desky.core.domain.repository.DeskStateRepository
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.domain.usecase.DeskUseCases
import org.example.desky.core.domain.usecase.ObserveDeskStateUseCase
import org.example.desky.core.domain.usecase.ObserveSettingsUseCase
import org.example.desky.core.domain.usecase.ObserveThemeModeUseCase
import org.example.desky.core.domain.usecase.PingDeskDeviceUseCase
import org.example.desky.core.domain.usecase.PowerOffDeskUseCase
import org.example.desky.core.domain.usecase.PowerOnDeskUseCase
import org.example.desky.core.domain.usecase.RefreshDeskStateUseCase
import org.example.desky.core.domain.usecase.ResetSettingsToDefaultsUseCase
import org.example.desky.core.domain.usecase.SetDeskLedPowerUseCase
import org.example.desky.core.domain.usecase.SettingsUseCases
import org.example.desky.core.domain.usecase.ShutdownDeskLaptopUseCase
import org.example.desky.core.domain.usecase.UpdateEspEndpointUseCase
import org.example.desky.core.domain.usecase.UpdateLaptopEndpointUseCase
import org.example.desky.core.domain.usecase.UpdateLedEndpointUseCase
import org.example.desky.core.domain.usecase.UpdateThemeModeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::SettingsDataStore)
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class

    singleOf(::DeskStateRepositoryImpl) bind DeskStateRepository::class

    // Desk use cases
    factoryOf(::ObserveDeskStateUseCase)
    factoryOf(::RefreshDeskStateUseCase)
    factoryOf(::PingDeskDeviceUseCase)
    factoryOf(::PowerOnDeskUseCase)
    factoryOf(::PowerOffDeskUseCase)
    factoryOf(::SetDeskLedPowerUseCase)
    factoryOf(::ShutdownDeskLaptopUseCase)
    factoryOf(::DeskUseCases)

    // Settings use cases
    factoryOf(::ObserveSettingsUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::UpdateEspEndpointUseCase)
    factoryOf(::UpdateLaptopEndpointUseCase)
    factoryOf(::UpdateLedEndpointUseCase)
    factoryOf(::UpdateThemeModeUseCase)
    factoryOf(::ResetSettingsToDefaultsUseCase)
    factoryOf(::SettingsUseCases)
}
