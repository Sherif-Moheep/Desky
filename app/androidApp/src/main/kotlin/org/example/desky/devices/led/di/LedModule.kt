package org.example.desky.devices.led.di

import org.example.desky.devices.led.data.datasource.LedPresetDataStore
import org.example.desky.devices.led.data.datasource.MagicHomeDiscoveryDataSource
import org.example.desky.devices.led.data.datasource.MagicHomeSocketDataSource
import org.example.desky.devices.led.data.repository.LedControllerRepositoryImpl
import org.example.desky.devices.led.data.repository.LedPresetRepositoryImpl
import org.example.desky.devices.led.domain.repository.LedControllerRepository
import org.example.desky.devices.led.domain.repository.LedPresetRepository
import org.example.desky.devices.led.domain.usecase.ConnectLedUseCase
import org.example.desky.devices.led.domain.usecase.DeletePresetUseCase
import org.example.desky.devices.led.domain.usecase.LedUseCases
import org.example.desky.devices.led.domain.usecase.ObserveConnectionStatusUseCase
import org.example.desky.devices.led.domain.usecase.ObserveLastBrightnessUseCase
import org.example.desky.devices.led.domain.usecase.ObservePresetsUseCase
import org.example.desky.devices.led.domain.usecase.SaveLastBrightnessUseCase
import org.example.desky.devices.led.domain.usecase.SavePresetUseCase
import org.example.desky.devices.led.domain.usecase.SetLedColorUseCase
import org.example.desky.devices.led.domain.usecase.SetLedPowerUseCase
import org.example.desky.devices.led.presentation.LedViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ledModule = module {
    // Data sources
    singleOf(::MagicHomeSocketDataSource)
    singleOf(::MagicHomeDiscoveryDataSource)
    singleOf(::LedPresetDataStore)

    // Repositories
    singleOf(::LedPresetRepositoryImpl) { bind<LedPresetRepository>() }
    singleOf(::LedControllerRepositoryImpl) { bind<LedControllerRepository>() }

    // Use cases
    factoryOf(::ObservePresetsUseCase)
    factoryOf(::SavePresetUseCase)
    factoryOf(::DeletePresetUseCase)
    factoryOf(::ObserveLastBrightnessUseCase)
    factoryOf(::SaveLastBrightnessUseCase)
    factoryOf(::SetLedPowerUseCase)
    factoryOf(::SetLedColorUseCase)
    factoryOf(::ConnectLedUseCase)
    factoryOf(::ObserveConnectionStatusUseCase)
    factoryOf(::LedUseCases)

    // ViewModel
    viewModelOf(::LedViewModel)
}
