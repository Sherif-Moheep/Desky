package org.example.desky.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.theme.StatusAmberBg
import org.example.desky.core.presentation.theme.StatusGreen
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.presentation.theme.StatusGreenBg

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

    ChipPill(
        label = label,
        bgColor = bgColor,
        textColor = textColor,
        dotColor = textColor,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun PreviewStatusBadge() {
    DeskyTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(status = DeviceConnectionStatus.Online(12))
            StatusBadge(status = DeviceConnectionStatus.Pinging)
            StatusBadge(status = DeviceConnectionStatus.Offline())
        }
    }
}
