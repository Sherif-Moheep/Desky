package org.example.desky.devices.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.example.desky.devices.laptop.presentation.LaptopDetailScreen
import org.example.desky.devices.led.presentation.LedScreen
import org.example.desky.devices.list.presentation.DevicesScreen
import org.example.desky.navigation.TopRoutes

@Composable
fun DevicesRoute(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    DevicesScreen(
        modifier = modifier,
        onNavigateToLaptopDetail = { onNavigate(DevicesRoutes.LaptopDetail) },
        onNavigateToLedDetail = { onNavigate(DevicesRoutes.LedDetail) }
    )
}

fun EntryProviderScope<NavKey>.devicesNavigation(
    onNavigate: (NavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    entry<TopRoutes.Devices>{
        DevicesRoute(
            onNavigate = onNavigate
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