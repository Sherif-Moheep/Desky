package org.example.desky.devices.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.example.desky.devices.laptop.presentation.LaptopDetailScreen
import org.example.desky.devices.led_strip.presentation.LedScreen
import org.example.desky.devices.list.presentation.DevicesScreen
import org.example.desky.navigation.TopRoutes

fun EntryProviderScope<NavKey>.devicesNavigation(
    devicesStack: NavBackStack<NavKey>
) {
    entry<TopRoutes.Devices>{
        DevicesScreen(
            onNavigateToLaptopDetail = {
                devicesStack.add(DevicesRoutes.LaptopDetail)
            },
            onNavigateToLedDetail = {
                devicesStack.add(DevicesRoutes.LedDetail)
            }
        )
    }

    entry<DevicesRoutes.LaptopDetail>{
        LaptopDetailScreen(
            onBackClick = { devicesStack.removeLastOrNull() }
        )
    }

    entry<DevicesRoutes.LedDetail>{
        LedScreen(
            onBackClick = { devicesStack.removeLastOrNull() }
        )
    }
}