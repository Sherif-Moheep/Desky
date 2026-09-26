package org.example.desky.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface TopRoutes: NavKey {

    @Serializable
    data object Home: TopRoutes

    @Serializable
    data object Devices: TopRoutes

    @Serializable
    data object Settings: TopRoutes
}