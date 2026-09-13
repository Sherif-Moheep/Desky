package org.example.desky.navigation

import androidx.annotation.DrawableRes
import org.example.desky.R

data class BottomNavBarItem (
    @DrawableRes val icon: Int,
    val label: String,
)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.Home to BottomNavBarItem(
        icon = R.drawable.home,
        label = "Home"
    ),
    Route.Devices to BottomNavBarItem(
        icon = R.drawable.devices,
        label = "Devices"
    ),
    Route.Settings to BottomNavBarItem(
        icon = R.drawable.settings,
        label = "Settings"
    )
)