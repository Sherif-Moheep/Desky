package org.example.desky.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import org.example.desky.devices.navigation.DevicesRoute
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

    val topLevelRoutesList = remember {
        TOP_LEVEL_DESTINATIONS.keys.toList()
    }

    val initialIndex = remember {
        topLevelRoutesList.indexOf(navigationState.topLevelRoute).coerceAtLeast(0)
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { topLevelRoutesList.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Sync pager swipe -> navigation state
    LaunchedEffect(pagerState.currentPage) {
        val currentRoute = topLevelRoutesList[pagerState.currentPage]
        if (navigationState.topLevelRoute != currentRoute) {
            navigationState.topLevelRoute = currentRoute
        }
    }

    // Sync navigation state -> pager
    LaunchedEffect(navigationState.topLevelRoute) {
        val targetIndex = topLevelRoutesList.indexOf(navigationState.topLevelRoute)
        if (targetIndex != -1 && pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        bottomBar = {
            if (navigationState.isTopLevelRoute) {
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
        AnimatedContent(
            targetState = navigationState.isTopLevelRoute,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
                } else {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                }
            },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
        ) { isTopLevel ->
            if (isTopLevel) {
                BackHandler(enabled = navigationState.topLevelRoute != navigationState.startRoute) {
                    navigator.goBack()
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) { page ->
                    when (topLevelRoutesList[page]) {
                        TopRoutes.Home -> HomeScreen()
                        TopRoutes.Devices -> DevicesRoute(
                            onNavigate = { navigator.navigate(it) }
                        )
                        TopRoutes.Settings -> SettingsScreen()
                    }
                }
            } else {
                NavDisplay(
                    modifier = Modifier
                        .fillMaxSize(),
                    onBack = navigator::goBack,
                    entries = navigationState.toEntries(
                        entryProvider {
                            entry<TopRoutes.Home> {
                                HomeScreen()
                            }

                            devicesNavigation(
                                onNavigate = { navigator.navigate(it) },
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
    }
}