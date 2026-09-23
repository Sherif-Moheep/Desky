package org.example.desky.laptop.di

import org.example.desky.laptop.presentation.LaptopViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val laptopModule = module {
    viewModelOf(::LaptopViewModel)
}
