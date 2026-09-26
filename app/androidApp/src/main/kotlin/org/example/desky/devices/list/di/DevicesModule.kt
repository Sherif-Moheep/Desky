package org.example.desky.devices.list.di

import org.example.desky.devices.list.presentation.DevicesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val devicesModule = module {
    viewModelOf(::DevicesViewModel,)
}
