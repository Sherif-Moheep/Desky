package org.example.desky.devices.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.theme.StatusAmberBg
import org.example.desky.core.presentation.theme.StatusGreen
import org.example.desky.core.presentation.theme.StatusGreenBg
import org.example.desky.core.presentation.util.playTactileClick
import org.example.desky.core.presentation.util.rememberVibrator
import kotlin.math.roundToInt

@Composable
fun LedDeviceCard(
    host: String,
    port: Int,
    status: DeviceConnectionStatus,
    isPoweredOn: Boolean,
    brightness: Float,
    color: Color,
    colorName: String,
    onPowerToggle: (Boolean) -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vibrator = rememberVibrator()
    val isOnline = status is DeviceConnectionStatus.Online
    val brightnessPercent = (brightness * 100).roundToInt()

    OutlinedCard(
        onClick = onNavigateToDetail,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: LED Icon + Title & Host + Power Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (isOnline && isPoweredOn) {
                                    color.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.led_strip),
                            contentDescription = "LED Strip",
                            tint = if (isOnline && isPoweredOn) {
                                color
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Desk Backlight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$host:$port",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isPoweredOn,
                    onCheckedChange = { targetOn ->
                        vibrator?.playTactileClick()
                        onPowerToggle(targetOn)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated Status Chips (FlowRow matching Laptop card)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Connection Status Chip
                when (status) {
                    is DeviceConnectionStatus.Online -> {
                        LedChipPill(
                            label = "Online · ${status.latencyMs}ms",
                            dotColor = StatusGreen,
                            bgColor = StatusGreenBg,
                            textColor = StatusGreen
                        )
                    }
                    is DeviceConnectionStatus.Pinging -> {
                        LedChipPill(
                            label = "Connecting...",
                            dotColor = StatusAmber,
                            bgColor = StatusAmberBg,
                            textColor = StatusAmber
                        )
                    }
                    is DeviceConnectionStatus.Offline -> {
                        LedChipPill(
                            label = status.message.ifBlank { "Offline" },
                            dotColor = MaterialTheme.colorScheme.error,
                            bgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                    DeviceConnectionStatus.Idle -> {
                        LedChipPill(
                            label = "Idle",
                            dotColor = MaterialTheme.colorScheme.outline,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Color Chip (with live swatch preview circle)
                if (isPoweredOn && isOnline) {
                    LedChipPill(
                        label = colorName,
                        dotColor = color,
                        dotBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 3. Brightness Chip
                    LedChipPill(
                        label = "$brightnessPercent% Brightness",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!isPoweredOn) {
                    LedChipPill(
                        label = "Turned Off",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Navigation Affordance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Light Controls",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "Open LED Controls",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LedChipPill(
    label: String,
    bgColor: Color,
    textColor: Color,
    dotColor: Color? = null,
    dotBorderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color = dotColor, shape = CircleShape)
                        .then(
                            if (dotBorderColor != null) {
                                Modifier.border(0.5.dp, dotBorderColor, CircleShape)
                            } else Modifier
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLedDeviceCard() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LedDeviceCard(
                host = "192.168.1.150",
                port = 5577,
                status = DeviceConnectionStatus.Online(15),
                isPoweredOn = true,
                brightness = 0.8f,
                color = Color(0xFFE53935),
                colorName = "Crimson",
                onPowerToggle = {},
                onNavigateToDetail = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            LedDeviceCard(
                host = "192.168.1.150",
                port = 5577,
                status = DeviceConnectionStatus.Offline("Unreachable"),
                isPoweredOn = false,
                brightness = 0.8f,
                color = Color(0xFFE53935),
                colorName = "Crimson",
                onPowerToggle = {},
                onNavigateToDetail = {}
            )
        }
    }
}
