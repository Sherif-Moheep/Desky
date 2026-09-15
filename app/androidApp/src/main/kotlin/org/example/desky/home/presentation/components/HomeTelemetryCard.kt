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
import org.example.desky.theme.DeskyTheme

// ----------------------------------------------------------------------------
// Telemetry UI Models
// ----------------------------------------------------------------------------

data class HomeTelemetryUiState(
    val laptop: LaptopTelemetry = LaptopTelemetry(),
    val light: LightTelemetry = LightTelemetry(),
    val relay: RelayTelemetry = RelayTelemetry(),
    val network: NetworkTelemetry = NetworkTelemetry()
)

data class LaptopTelemetry(
    val isOnline: Boolean = true,
    val isLocked: Boolean = true,
    val deviceName: String = "Laptop Workstation"
) {
    val statusText: String
        get() = when {
            !isOnline -> "Offline"
            isLocked -> "Online · Locked"
            else -> "Online · Unlocked"
        }
}

data class LightTelemetry(
    val isOn: Boolean = true,
    val brightnessPercent: Int = 80,
    val colorName: String = "Crimson",
    val label: String = "Ambient Light"
) {
    val statusText: String
        get() = if (isOn) "$brightnessPercent% · $colorName" else "Off"
}

data class RelayTelemetry(
    val isAcActive: Boolean = true,
    val label: String = "Laptop Charger Relay"
) {
    val statusText: String
        get() = if (isAcActive) "AC Active" else "AC Cut"
}

data class NetworkTelemetry(
    val isConnected: Boolean = true,
    val latencyMs: Int = 12
) {
    val statusText: String
        get() = if (isConnected) "LAN (${latencyMs}ms)" else "No LAN"
    val label: String
        get() = if (isConnected) "Connected" else "Disconnected"
}

// ----------------------------------------------------------------------------
// Telemetry Composable
// ----------------------------------------------------------------------------

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
                    iconTint = if (state.light.isOn) activeColor else MaterialTheme.colorScheme.outline,
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
                    iconTint = if (state.relay.isAcActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
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

// ----------------------------------------------------------------------------
// Previews
// ----------------------------------------------------------------------------

@PreviewLightDark
@Composable
fun PreviewHomeTelemetryCardActive() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            HomeTelemetryCard(
                state = HomeTelemetryUiState()
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
                    light = LightTelemetry(isOn = false),
                    relay = RelayTelemetry(isAcActive = false)
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
                    network = NetworkTelemetry(isConnected = false)
                )
            )
        }
    }
}
