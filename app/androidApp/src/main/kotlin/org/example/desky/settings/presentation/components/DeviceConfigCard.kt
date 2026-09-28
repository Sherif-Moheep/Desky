package org.example.desky.settings.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.presentation.components.StatusBadge
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun DeviceConfigCard(
    name: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    host: String,
    onHostChange: (String) -> Unit,
    port: String,
    onPortChange: (String) -> Unit,
    status: DeviceConnectionStatus,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    hostLabel: String = "Host / IP",
    actionLabel: String = "Ping"
) {
    OutlinedCard(
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
                .padding(16.dp)
        ) {
            // Header Row: Icon, Title/Subtitle, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs Row: Host (flexible) & Port (fixed width) side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InputField(
                    value = host,
                    onValueChange = onHostChange,
                    label = hostLabel,
                    keyboardType = KeyboardType.Uri,
                    modifier = Modifier.weight(1f)
                )

                InputField(
                    value = port,
                    onValueChange = onPortChange,
                    label = "Port",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.width(96.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    enabled = status !is DeviceConnectionStatus.Pinging
                ) {
                    if (status is DeviceConnectionStatus.Pinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Ping",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}



@PreviewLightDark
@Composable
fun PreviewDeviceConfigCard() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = "desky-relay.local",
                onHostChange = {},
                port = "80",
                onPortChange = {},
                status = DeviceConnectionStatus.Online(8),
                onActionClick = {}
            )

            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = "desky-relay.local",
                onHostChange = {},
                port = "80",
                onPortChange = {},
                status = DeviceConnectionStatus.Offline(),
                onActionClick = {}
            )

            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = "desky-relay.local",
                onHostChange = {},
                port = "80",
                onPortChange = {},
                status = DeviceConnectionStatus.Idle,
                onActionClick = {}
            )

            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = "desky-relay.local",
                onHostChange = {},
                port = "80",
                onPortChange = {},
                status = DeviceConnectionStatus.Pinging,
                onActionClick = {}
            )
        }
    }
}
