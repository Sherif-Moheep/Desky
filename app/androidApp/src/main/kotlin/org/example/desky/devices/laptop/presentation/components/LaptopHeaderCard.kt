package org.example.desky.devices.laptop.presentation.components

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
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.presentation.components.PowerButton
import org.example.desky.core.presentation.components.StatusBadge
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun LaptopHeaderCard(
    powerState: PowerState,
    status: DeviceConnectionStatus,
    onPowerClick: () -> Unit,
    onRetryConnection: () -> Unit,
    modifier: Modifier = Modifier,
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
                        painter = painterResource(R.drawable.laptop),
                        contentDescription = "Laptop",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Laptop",
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
                if (status is DeviceConnectionStatus.Offline) {
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

                PowerButton(
                    powerState = powerState,
                    onClick = onPowerClick,
                    contentDescription = when (powerState) {
                        PowerState.ON -> "Shut Down Laptop"
                        PowerState.OFF -> "Turn ON Laptop"
                        PowerState.BOOTING -> "Booting..."
                        PowerState.SHUTTING_DOWN -> "Shutting Down..."
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopHeaderCard() {
    var powerState by remember { mutableStateOf(PowerState.ON) }

    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LaptopHeaderCard(
                powerState = powerState,
                onPowerClick = {
                    powerState = if (powerState == PowerState.ON) PowerState.OFF else PowerState.ON
                },
                status = DeviceConnectionStatus.Online(12),
                onRetryConnection = {},
            )
            Spacer(modifier = Modifier.size(12.dp))
            LaptopHeaderCard(
                powerState = PowerState.BOOTING,
                onPowerClick = {},
                status = DeviceConnectionStatus.Pinging,
                onRetryConnection = {},
            )
            Spacer(modifier = Modifier.size(12.dp))
            LaptopHeaderCard(
                powerState = PowerState.OFF,
                onPowerClick = { powerState = PowerState.BOOTING },
                status = DeviceConnectionStatus.Offline("Offline"),
                onRetryConnection = {},
            )
        }
    }
}
