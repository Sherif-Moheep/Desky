package org.example.desky.devices.laptop.di

import org.example.desky.devices.laptop.data.datasource.LaptopRemoteDataSource
import org.example.desky.devices.laptop.data.repository.LaptopRepositoryImpl
import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.devices.laptop.domain.usecase.CheckLaptopStatusUseCase
import org.example.desky.devices.laptop.domain.usecase.GetLaptopTelemetryUseCase
import org.example.desky.devices.laptop.domain.usecase.LaptopUseCases
import org.example.desky.devices.laptop.domain.usecase.LockLaptopUseCase
import org.example.desky.devices.laptop.domain.usecase.ObserveLaptopConnectionStatusUseCase
import org.example.desky.devices.laptop.domain.usecase.RestartLaptopUseCase
import org.example.desky.devices.laptop.domain.usecase.ShutdownLaptopUseCase
import org.example.desky.devices.laptop.domain.usecase.SleepLaptopUseCase
import org.example.desky.devices.laptop.presentation.LaptopViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val laptopModule = module {
    // Data sources
    singleOf(::LaptopRemoteDataSource)

    // Repositories
    singleOf(::LaptopRepositoryImpl) { bind<LaptopRepository>() }

    // Use cases
    factoryOf(::CheckLaptopStatusUseCase)
    factoryOf(::ObserveLaptopConnectionStatusUseCase)
    factoryOf(::GetLaptopTelemetryUseCase)
    factoryOf(::LockLaptopUseCase)
    factoryOf(::SleepLaptopUseCase)
    factoryOf(::RestartLaptopUseCase)
    factoryOf(::ShutdownLaptopUseCase)
    factoryOf(::LaptopUseCases)

    // ViewModel
    viewModelOf(::LaptopViewModel)
}
