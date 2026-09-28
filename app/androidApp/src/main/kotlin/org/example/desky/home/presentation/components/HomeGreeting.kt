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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.home.presentation.model.getGreetingContent
import java.util.Calendar

@Composable
fun HomeGreeting(
    powerState: PowerState,
    modifier: Modifier = Modifier,
    hour: Int = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
) {
    val greeting = remember(powerState, hour) {
        getGreetingContent(powerState = powerState, hour = hour)
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



@PreviewLightDark
@Composable
fun PreviewAllGreetingStates() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            HomeGreeting(powerState = PowerState.ON, hour = 8)
            Spacer(modifier = Modifier.height(24.dp))
            HomeGreeting(powerState = PowerState.BOOTING)
            Spacer(modifier = Modifier.height(24.dp))
            HomeGreeting(powerState = PowerState.SHUTTING_DOWN)
            Spacer(modifier = Modifier.height(24.dp))
            HomeGreeting(powerState = PowerState.OFF)
        }
    }
}
