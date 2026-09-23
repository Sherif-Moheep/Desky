package org.example.desky.laptop.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber

@Composable
fun LaptopActionsRow(
    enabled: Boolean,
    onLockClick: () -> Unit,
    onSleepClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.45f,
        label = "actionsAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LaptopActionCard(
                title = "Lock",
                subtitle = "Win + L",
                iconRes = R.drawable.lock,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onLockClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            LaptopActionCard(
                title = "Sleep",
                subtitle = "Standby",
                iconRes = R.drawable.moon,
                iconTint = MaterialTheme.colorScheme.secondary,
                onClick = onSleepClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            LaptopActionCard(
                title = "Restart",
                subtitle = "Reboot OS",
                iconRes = R.drawable.refresh,
                iconTint = StatusAmber,
                onClick = onRestartClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopActionsRowOnline() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LaptopActionsRow(
                onLockClick = {},
                onSleepClick = {},
                onRestartClick = {},
                enabled = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            LaptopActionsRow(
                onLockClick = {},
                onSleepClick = {},
                onRestartClick = {},
                enabled = false
            )
        }
    }
}
