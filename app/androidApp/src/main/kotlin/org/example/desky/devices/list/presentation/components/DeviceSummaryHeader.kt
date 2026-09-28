package org.example.desky.devices.list.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.desky.core.presentation.components.ChipPill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.theme.StatusAmberBg
import org.example.desky.core.presentation.theme.StatusGreen
import org.example.desky.core.presentation.theme.StatusGreenBg

@Composable
fun DeviceSummaryHeader(
    onlineCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        onlineCount == totalCount && totalCount > 0 -> Triple(
            StatusGreenBg,
            StatusGreen,
            "$onlineCount/$totalCount Online"
        )
        onlineCount > 0 -> Triple(
            StatusAmberBg,
            StatusAmber,
            "$onlineCount/$totalCount Online"
        )
        else -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            "0/$totalCount Online"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Primary Peripherals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Devices connected over LAN",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ChipPill(
            label = label,
            bgColor = bgColor,
            textColor = textColor,
            dotColor = textColor
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewDeviceSummaryHeader() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DeviceSummaryHeader(onlineCount = 2, totalCount = 2)
            DeviceSummaryHeader(onlineCount = 1, totalCount = 2)
            DeviceSummaryHeader(onlineCount = 0, totalCount = 2)
        }
    }
}
