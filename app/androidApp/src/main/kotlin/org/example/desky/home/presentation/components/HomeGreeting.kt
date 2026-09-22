package org.example.desky.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.desky.core.presentation.theme.DeskyTheme
import java.util.Calendar

data class GreetingContent(
    val headline: String,
    val subtitle: String
)

fun getGreetingContent(
    isDeskOn: Boolean,
    hour: Int
): GreetingContent {
    if (!isDeskOn) {
        return GreetingContent(
            headline = "Desk is asleep.",
            subtitle = "Rest well. Workspace is powered down."
        )
    }

    return when (hour) {
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

@Composable
fun HomeGreeting(
    isDeskOn: Boolean,
    modifier: Modifier = Modifier,
    hour: Int = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
) {
    val greeting = remember(isDeskOn, hour) {
        getGreetingContent(isDeskOn = isDeskOn, hour = hour)
    }

    Column(modifier = modifier) {
        Text(
            text = greeting.headline,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = greeting.subtitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewAllGreetingStates() {
    DeskyTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Morning
            HomeGreeting(isDeskOn = true, hour = 8)
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Afternoon
            HomeGreeting(isDeskOn = true, hour = 14)
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Evening
            HomeGreeting(isDeskOn = true, hour = 20)
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Night Owl
            HomeGreeting(isDeskOn = true, hour = 2)
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Desk OFF
            HomeGreeting(isDeskOn = false, hour = 14)
        }
    }
}
