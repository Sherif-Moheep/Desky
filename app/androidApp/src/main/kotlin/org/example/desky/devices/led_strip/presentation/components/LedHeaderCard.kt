package org.example.desky.devices.led_strip.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.components.StatusBadge
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun LedHeaderCard(
    isOn: Boolean,
    onTogglePower: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    name: String,
    status: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    onRetryConnection: (() -> Unit)? = null,
    @DrawableRes iconRes: Int
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    StatusBadge(status = status)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (status is DeviceConnectionStatus.Offline && onRetryConnection != null) {
                    FilledTonalIconButton(
                        onClick = onRetryConnection,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.refresh),
                            contentDescription = "Retry connection",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Switch(
                    checked = isOn,
                    onCheckedChange = onTogglePower
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLedHeaderCard() {
    var isOn by remember { mutableStateOf(true) }

    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LedHeaderCard(
                isOn = isOn,
                onTogglePower = { isOn = it },
                status = DeviceConnectionStatus.Online(14),
                iconRes = R.drawable.led_strip,
                name = "Desk Backlight",
            )
            Spacer(modifier = Modifier.size(8.dp))
            LedHeaderCard(
                isOn = isOn,
                onTogglePower = { isOn = it },
                status = DeviceConnectionStatus.Offline("Offline"),
                onRetryConnection = {},
                iconRes = R.drawable.led_strip,
                name = "Desk Backlight",
            )
        }
    }
}
