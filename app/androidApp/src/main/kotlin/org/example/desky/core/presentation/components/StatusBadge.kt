package org.example.desky.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.theme.StatusAmberBg
import org.example.desky.core.presentation.theme.StatusGreen
import org.example.desky.core.presentation.theme.StatusGreenBg

sealed interface DeviceConnectionStatus {
    data object Idle : DeviceConnectionStatus
    data object Pinging : DeviceConnectionStatus
    data class Online(val latencyMs: Long) : DeviceConnectionStatus
    data class Offline(val message: String = "Offline") : DeviceConnectionStatus
}

/**
 * Badge indicating connection status and latency.
 * Used in the Settings screen and device detail header cards.
 */
@Composable
fun StatusBadge(
    status: DeviceConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        is DeviceConnectionStatus.Online -> Triple(
            StatusGreenBg,
            StatusGreen,
            "Online · ${status.latencyMs}ms"
        )
        is DeviceConnectionStatus.Pinging -> Triple(
            StatusAmberBg,
            StatusAmber,
            "Connecting..."
        )
        is DeviceConnectionStatus.Offline -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            status.message
        )
        is DeviceConnectionStatus.Idle -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Untested"
        )
    }

    BadgePill(
        label = label,
        bgColor = bgColor,
        textColor = textColor,
        modifier = modifier
    )
}

@Composable
private fun BadgePill(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = textColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
