package org.example.desky.devices.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.example.desky.devices.laptop.presentation.LaptopDetailScreen
import org.example.desky.devices.led_strip.presentation.LedScreen
import org.example.desky.devices.list.presentation.DevicesScreen
import org.example.desky.navigation.TopRoutes

fun EntryProviderScope<NavKey>.devicesNavigation(
    navigate: (NavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    entry<TopRoutes.Devices>{
        DevicesScreen(
            onNavigateToLaptopDetail = {
                navigate(DevicesRoutes.LaptopDetail)
            },
            onNavigateToLedDetail = {
                navigate(DevicesRoutes.LedDetail)
            }
        )
    }

    entry<DevicesRoutes.LaptopDetail>{
        LaptopDetailScreen(
            onBackClick = {
                onBackClick()
            }
        )
    }

    entry<DevicesRoutes.LedDetail>{
        LedScreen(
            onBackClick = {
                onBackClick()
            }
        )
    }
}

val devicesSerializerModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(DevicesRoutes.LaptopDetail::class, DevicesRoutes.LaptopDetail.serializer())
        subclass(DevicesRoutes.LedDetail::class, DevicesRoutes.LedDetail.serializer())
    }
}