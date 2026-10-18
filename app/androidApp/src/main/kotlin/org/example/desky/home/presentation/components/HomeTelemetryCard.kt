package org.example.desky.home.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.home.presentation.model.HomeTelemetryUiState
import org.example.desky.home.presentation.model.LaptopTelemetry
import org.example.desky.home.presentation.model.LightTelemetry
import org.example.desky.home.presentation.model.NetworkTelemetry
import org.example.desky.home.presentation.model.RelayTelemetry

@Composable
fun HomeTelemetryCard(
    state: HomeTelemetryUiState,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Row 1: Laptop & Ambient Light
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryCell(
                    icon = R.drawable.laptop,
                    title = state.laptop.statusText,
                    subtitle = state.laptop.deviceName,
                    iconTint = if (state.laptop.isOnline) activeColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )

                TelemetryCell(
                    icon = R.drawable.led_strip,
                    title = state.light.statusText,
                    subtitle = state.light.label,
                    iconTint = when {
                        !state.light.isOnline -> MaterialTheme.colorScheme.outline
                        state.light.isOn -> activeColor
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Desk Power Relay & LAN Connection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryCell(
                    icon = R.drawable.electricity,
                    title = state.relay.statusText,
                    subtitle = state.relay.label,
                    iconTint = when {
                        !state.relay.isOnline -> MaterialTheme.colorScheme.outline
                        state.relay.isAcActive -> activeColor
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )

                TelemetryCell(
                    icon = if (state.network.isConnected) R.drawable.wifi else R.drawable.wifi_off,
                    title = state.network.statusText,
                    subtitle = state.network.label,
                    iconTint = if (state.network.isConnected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryCell(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = subtitle,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@PreviewLightDark
@Composable
fun PreviewHomeTelemetryCardActive() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            HomeTelemetryCard(
                state = HomeTelemetryUiState(
                    laptop = LaptopTelemetry(isOnline = true, isLocked = true),
                    light = LightTelemetry(isOnline = true, isOn = true, brightnessPercent = 80, colorName = "Crimson"),
                    relay = RelayTelemetry(isOnline = true, isAcActive = true),
                    network = NetworkTelemetry(isConnected = true, latencyMs = 12)
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewHomeTelemetryCardDeskOff() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            HomeTelemetryCard(
                state = HomeTelemetryUiState(
                    laptop = LaptopTelemetry(isOnline = false),
                    light = LightTelemetry(isOnline = true, isOn = false),
                    relay = RelayTelemetry(isOnline = true, isAcActive = false)
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewHomeTelemetryCardDisconnected() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            HomeTelemetryCard(
                state = HomeTelemetryUiState(
                    laptop = LaptopTelemetry(isOnline = false),
                    light = LightTelemetry(isOnline = false, isOn = false),
                    relay = RelayTelemetry(isOnline = false, isAcActive = false),
                    network = NetworkTelemetry(isConnected = false)
                )
            )
        }
    }
}
