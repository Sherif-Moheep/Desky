package org.example.desky.devices.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface DevicesRoutes: NavKey {

    @Serializable
    data object LaptopDetail: DevicesRoutes

    @Serializable
    data object LedDetail: DevicesRoutes
}