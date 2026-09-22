package org.example.desky.core.di

import org.example.desky.core.data.datasource.SettingsDataStore
import org.example.desky.core.data.repository.SettingsRepositoryImpl
import org.example.desky.core.domain.repository.SettingsRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::SettingsDataStore)
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
}
