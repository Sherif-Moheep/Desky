package org.example.desky.devices.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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

        Surface(
            shape = RoundedCornerShape(50),
            color = bgColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color = textColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
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
