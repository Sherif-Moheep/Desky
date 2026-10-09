package org.example.desky.devices.esp.di

import org.example.desky.devices.esp.data.datasource.EspDiscoveryDataSource
import org.example.desky.devices.esp.data.datasource.EspRemoteDataSource
import org.example.desky.devices.esp.data.repository.EspRelayRepositoryImpl
import org.example.desky.devices.esp.domain.repository.EspRelayRepository
import org.example.desky.devices.esp.domain.usecase.CheckEspStatusUseCase
import org.example.desky.devices.esp.domain.usecase.DiscoverEspUseCase
import org.example.desky.devices.esp.domain.usecase.EspRelayUseCases
import org.example.desky.devices.esp.domain.usecase.GetEspStatusUseCase
import org.example.desky.devices.esp.domain.usecase.ObserveEspConnectionStatusUseCase
import org.example.desky.devices.esp.domain.usecase.PowerOffEspUseCase
import org.example.desky.devices.esp.domain.usecase.PowerOnEspUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val espModule = module {
    // Data sources
    singleOf(::EspRemoteDataSource)
    singleOf(::EspDiscoveryDataSource)

    // Repositories
    singleOf(::EspRelayRepositoryImpl) { bind<EspRelayRepository>() }

    // Use cases
    factoryOf(::CheckEspStatusUseCase)
    factoryOf(::ObserveEspConnectionStatusUseCase)
    factoryOf(::DiscoverEspUseCase)
    factoryOf(::GetEspStatusUseCase)
    factoryOf(::PowerOnEspUseCase)
    factoryOf(::PowerOffEspUseCase)
    factoryOf(::EspRelayUseCases)
}
