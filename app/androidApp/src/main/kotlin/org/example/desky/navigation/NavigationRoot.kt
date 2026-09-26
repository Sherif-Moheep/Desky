package org.example.desky.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.example.desky.devices.navigation.devicesNavigation
import org.example.desky.home.presentation.HomeScreen
import org.example.desky.settings.presentation.SettingsScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val homeStack = rememberNavBackStack(TopRoutes.Home)
    val devicesStack = rememberNavBackStack(TopRoutes.Devices)
    val settingsStack = rememberNavBackStack(TopRoutes.Settings)

    var currentTab by remember { mutableStateOf<TopRoutes>(TopRoutes.Home) }
    val tabHistory = remember { mutableStateListOf<TopRoutes>(TopRoutes.Home) }

    val activeStack = when (currentTab) {
        TopRoutes.Home -> homeStack
        TopRoutes.Devices -> devicesStack
        TopRoutes.Settings -> settingsStack
    }
    val currentScreen = activeStack.lastOrNull() ?: currentTab

    val isTopLevelRoute = currentScreen is TopRoutes

    fun onTabSelected(
        targetRoute: TopRoutes
    ) {
        if (currentTab == targetRoute) {
            while (activeStack.size > 1) {
                activeStack.removeLastOrNull()
            }
            return
        }

        tabHistory.remove(targetRoute)
        tabHistory.add(targetRoute)
        currentTab = targetRoute
    }

    BackHandler(
        enabled = activeStack.size > 1 || currentTab != TopRoutes.Home
    ) {
        if (activeStack.size > 1) {
            activeStack.removeLastOrNull()
        } else if (tabHistory.size > 1) {
            // check it
            tabHistory.removeLastOrNull()
            currentTab = tabHistory.last()
        } else {
            currentTab = TopRoutes.Home
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        bottomBar = {
            if (isTopLevelRoute) {
                BottomNavBar(
                    selectedKey = currentTab,
                    onSelectedKey = { key ->
                        (key as? TopRoutes)?.let { onTabSelected(it) }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = activeStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            entryProvider = entryProvider {

                entry<TopRoutes.Home> {
                    HomeScreen()
                }

                devicesNavigation(devicesStack)

                entry<TopRoutes.Settings> {
                    SettingsScreen()
                }
            }
        )
    }
}