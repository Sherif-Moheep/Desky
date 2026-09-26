package org.example.desky.devices.laptop.di

import org.example.desky.devices.laptop.presentation.LaptopViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val laptopModule = module {
    viewModelOf(::LaptopViewModel)
}
