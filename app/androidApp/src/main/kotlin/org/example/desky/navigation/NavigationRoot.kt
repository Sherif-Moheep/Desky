package org.example.desky.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.example.desky.devices.navigation.devicesNavigation
import org.example.desky.home.presentation.HomeScreen
import org.example.desky.settings.presentation.SettingsScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = TopRoutes.Home,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember {
        Navigator(navigationState)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        bottomBar = {
            if (navigationState.isTopLevelRoute) {
                BottomNavBar(
                    selectedKey = navigationState.topLevelRoute,
                    onSelectedKey = {
                        navigator.navigate(it)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = navigator::goBack,
            entries = navigationState.toEntries(
                entryProvider {

                    entry<TopRoutes.Home> {
                        HomeScreen()
                    }

                    devicesNavigation(
                        navigate = { navigator.navigate(it) },
                        onBackClick = navigator::goBack
                    )

                    entry<TopRoutes.Settings> {
                        SettingsScreen()
                    }
                }
            )
        )
    }
}