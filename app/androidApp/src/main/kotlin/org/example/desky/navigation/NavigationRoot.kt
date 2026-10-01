package org.example.desky.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import org.example.desky.devices.navigation.devicesNavigation
import org.example.desky.home.presentation.HomeScreen
import org.example.desky.settings.presentation.SettingsScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val topLevelRoutesList = remember {
        TOP_LEVEL_DESTINATIONS.keys.toList()
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { topLevelRoutesList.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val devicesStack = rememberNavBackStack(TopRoutes.Devices)

    val focusManager = LocalFocusManager.current

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    val isDevicesTab = topLevelRoutesList.getOrNull(pagerState.currentPage) == TopRoutes.Devices
    val isTopLevel = !isDevicesTab || devicesStack.size == 1

    val devicesDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        rememberViewModelStoreNavEntryDecorator()
    )

    val devicesEntries = rememberDecoratedNavEntries(
        backStack = devicesStack,
        entryDecorators = devicesDecorators,
        entryProvider = entryProvider {
            devicesNavigation(
                onNavigate = { devicesStack.add(it) },
                onBackClick = { devicesStack.removeLastOrNull() }
            )
        }
    )

    // Top-level back handler: Return to Home if on another tab at root
    BackHandler(enabled = isTopLevel && pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevel,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                BottomNavBar(
                    selectedKey = topLevelRoutesList[pagerState.targetPage],
                    onSelectedKey = { targetRoute ->
                        val targetIndex = topLevelRoutesList.indexOf(targetRoute)
                        if (targetIndex != -1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = isTopLevel,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) { page ->
            when (topLevelRoutesList[page]) {
                TopRoutes.Home -> HomeScreen()
                TopRoutes.Devices -> {
                    NavDisplay(
                        modifier = Modifier.fillMaxSize(),
                        onBack = { devicesStack.removeLastOrNull() },
                        entries = devicesEntries
                    )
                }
                TopRoutes.Settings -> SettingsScreen()
            }
        }
    }
}