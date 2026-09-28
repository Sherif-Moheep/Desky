package org.example.desky.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.example.desky.devices.navigation.devicesSerializerModule

class NavigationState(
    val startRoute: NavKey,
    val topLevelRoutes: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute by topLevelRoutes

    val currentScreen: NavKey
        get() = backStacks[topLevelRoute]?.lastOrNull() ?: topLevelRoute

    val isTopLevelRoute: Boolean
        get() = currentScreen in backStacks.keys

    val stackInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

val serializersConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(TopRoutes.Home::class, TopRoutes.Home.serializer())
            subclass(TopRoutes.Devices::class, TopRoutes.Devices.serializer())
            subclass(TopRoutes.Settings::class, TopRoutes.Settings.serializer())
        }
        include(devicesSerializerModule)
    }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute,
        topLevelRoutes,
        configuration = serializersConfig,
        serializer = MutableStateSerializer(PolymorphicSerializer(NavKey::class))
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith {
        rememberNavBackStack(
            configuration = serializersConfig,
            it
        )
    }

    return remember(startRoute, topLevelRoute) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoutes = topLevelRoute,
            backStacks = backStacks
        )
    }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->

        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator()
        )

        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    return stackInUse
        .flatMap {
            decoratedEntries[it] ?: emptyList()
        }
        .toMutableStateList()
}