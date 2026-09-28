package org.example.desky.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    val navigationState: NavigationState
) {
    fun navigate(targetRoute: NavKey) {
        if (targetRoute in navigationState.backStacks.keys) {
            navigationState.topLevelRoute = targetRoute
        } else {
            navigationState.backStacks[navigationState.topLevelRoute]?.add(targetRoute)
        }
    }

    fun goBack() {
        val currentStack = navigationState.backStacks[navigationState.topLevelRoute]
            ?: error("No back stack for ${navigationState.topLevelRoute}")

        val currentRoute = currentStack.last()

        if (currentRoute == navigationState.topLevelRoute) {
            navigationState.topLevelRoute = navigationState.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}