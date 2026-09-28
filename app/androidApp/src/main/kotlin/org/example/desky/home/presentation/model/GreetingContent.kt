package org.example.desky.home.presentation.model

import org.example.desky.core.domain.model.PowerState

data class GreetingContent(
    val headline: String,
    val subtitle: String
)

fun getGreetingContent(
    powerState: PowerState,
    hour: Int
): GreetingContent {
    return when (powerState) {
        PowerState.BOOTING -> GreetingContent(
            headline = "Waking up workspace...",
            subtitle = "Starting workstation, light, and peripherals."
        )
        PowerState.SHUTTING_DOWN -> GreetingContent(
            headline = "Powering down...",
            subtitle = "Safely shutting down all systems."
        )
        PowerState.OFF -> GreetingContent(
            headline = "Desk is asleep.",
            subtitle = "Rest well. Workspace is powered down."
        )
        PowerState.ON -> when (hour) {
            in 5..11 -> GreetingContent(
                headline = "Ready to work.",
                subtitle = "Good morning. Have a productive session."
            )
            in 12..17 -> GreetingContent(
                headline = "Ready to work.",
                subtitle = "Good afternoon. Workspace is ready for focus."
            )
            in 18..23 -> GreetingContent(
                headline = "Ready to work.",
                subtitle = "Good evening. Ready for the night session."
            )
            else -> GreetingContent(
                headline = "Ready to work.",
                subtitle = "The world is asleep. Time to build."
            )
        }
    }
}
