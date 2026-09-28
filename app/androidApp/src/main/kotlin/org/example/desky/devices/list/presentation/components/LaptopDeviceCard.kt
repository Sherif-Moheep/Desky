package org.example.desky.devices.list.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.components.ChipPill
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.theme.StatusAmberBg
import org.example.desky.core.presentation.theme.StatusGreen
import org.example.desky.core.presentation.theme.StatusGreenBg
import org.example.desky.devices.laptop.presentation.model.LaptopPowerState
import org.example.desky.devices.laptop.presentation.components.LaptopPowerButton

@Composable
fun LaptopDeviceCard(
    host: String,
    port: Int,
    serverStatus: DeviceConnectionStatus,
    espStatus: DeviceConnectionStatus,
    powerState: LaptopPowerState,
    isLocked: Boolean,
    uptime: String,
    onPowerClick: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = serverStatus is DeviceConnectionStatus.Online

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
            // Header Row: Laptop Icon + Title & Subtitle + Tactile Power Button
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
                                color = if (isOnline && powerState == LaptopPowerState.ON) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.laptop),
                            contentDescription = "Laptop",
                            tint = if (isOnline && powerState == LaptopPowerState.ON) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Laptop Workstation",
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

                LaptopPowerButton(
                    powerState = powerState,
                    onClick = onPowerClick,
                    size = 42.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated Status Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Laptop Ktor Server Chip
                when (serverStatus) {
                    is DeviceConnectionStatus.Online -> {
                        ChipPill(
                            label = "Server: ${serverStatus.latencyMs}ms",
                            dotColor = StatusGreen,
                            bgColor = StatusGreenBg,
                            textColor = StatusGreen
                        )
                    }
                    is DeviceConnectionStatus.Pinging -> {
                        ChipPill(
                            label = "Server: Connecting...",
                            dotColor = StatusAmber,
                            bgColor = StatusAmberBg,
                            textColor = StatusAmber
                        )
                    }
                    is DeviceConnectionStatus.Offline -> {
                        ChipPill(
                            label = if (powerState == LaptopPowerState.OFF) "Server: Offline" else "Server: Unreachable",
                            dotColor = MaterialTheme.colorScheme.error,
                            bgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                    DeviceConnectionStatus.Idle -> {
                        ChipPill(
                            label = "Server: Idle",
                            dotColor = MaterialTheme.colorScheme.outline,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. ESP32 Relay Chip
                when (espStatus) {
                    is DeviceConnectionStatus.Online -> {
                        ChipPill(
                            label = "Relay: Ready (${espStatus.latencyMs}ms)",
                            dotColor = StatusGreen,
                            bgColor = StatusGreenBg,
                            textColor = StatusGreen
                        )
                    }
                    is DeviceConnectionStatus.Pinging -> {
                        ChipPill(
                            label = "Relay: Connecting...",
                            dotColor = StatusAmber,
                            bgColor = StatusAmberBg,
                            textColor = StatusAmber
                        )
                    }
                    is DeviceConnectionStatus.Offline -> {
                        ChipPill(
                            label = "Relay: Offline",
                            dotColor = MaterialTheme.colorScheme.error,
                            bgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                    DeviceConnectionStatus.Idle -> {
                        ChipPill(
                            label = "Relay: Idle",
                            dotColor = MaterialTheme.colorScheme.outline,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 3. Session Status Chip
                if (powerState == LaptopPowerState.ON) {
                    ChipPill(
                        label = if (isLocked) "Locked" else "Unlocked",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 4. Uptime Chip
                    ChipPill(
                        label = "Uptime: $uptime",
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
                    text = "Workstation Controls",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "Open Laptop Controls",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopDeviceCard() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LaptopDeviceCard(
                host = "desky-laptop.local",
                port = 8080,
                serverStatus = DeviceConnectionStatus.Online(12),
                espStatus = DeviceConnectionStatus.Online(8),
                powerState = LaptopPowerState.ON,
                isLocked = true,
                uptime = "3h 42m",
                onPowerClick = {},
                onNavigateToDetail = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            LaptopDeviceCard(
                host = "desky-laptop.local",
                port = 8080,
                serverStatus = DeviceConnectionStatus.Offline("Offline"),
                espStatus = DeviceConnectionStatus.Online(9),
                powerState = LaptopPowerState.OFF,
                isLocked = false,
                uptime = "0m",
                onPowerClick = {},
                onNavigateToDetail = {}
            )
        }
    }
}
