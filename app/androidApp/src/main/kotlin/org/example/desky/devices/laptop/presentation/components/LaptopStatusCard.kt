package org.example.desky.devices.laptop.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.model.LaptopTelemetryDto

@Composable
fun LaptopStatusCard(
    telemetry: LaptopTelemetryDto?,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isOnline) 1f else 0.45f,
        label = "statusCardAlpha"
    )

    val sessionTint = when {
        !isOnline || telemetry == null -> MaterialTheme.colorScheme.outline
        telemetry.isLocked -> MaterialTheme.colorScheme.primary
        else -> StatusAmber
    }

    val powerTint = when {
        !isOnline || telemetry == null -> MaterialTheme.colorScheme.outline
        telemetry.isAcConnected -> StatusAmber
        telemetry.batteryPercent <= 20 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val batteryIconRes = when {
        telemetry == null || !isOnline -> R.drawable.battery_full
        telemetry.isAcConnected -> R.drawable.electricity
        telemetry.batteryPercent >= 90 -> R.drawable.battery_full
        telemetry.batteryPercent >= 70 -> R.drawable.battery_5
        telemetry.batteryPercent >= 50 -> R.drawable.battery_4
        telemetry.batteryPercent > 20  -> R.drawable.battery_2
        else -> R.drawable.battery_alert
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. SESSION
                StatusMetricColumn(
                    label = "SESSION",
                    value = if (isOnline && telemetry != null) (if (telemetry.isLocked) "Locked" else "Unlocked") else "--",
                    iconRes = if (telemetry?.isLocked == true) R.drawable.lock else R.drawable.lock_open,
                    iconTint = sessionTint,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // 2. POWER
                StatusMetricColumn(
                    label = "POWER",
                    value = if (isOnline && telemetry != null) "${telemetry.batteryPercent}% ${if (telemetry.isAcConnected) "AC" else "Bat"}" else "--",
                    iconRes = batteryIconRes,
                    iconTint = powerTint,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // 3. UPTIME
                StatusMetricColumn(
                    label = "UPTIME",
                    value = if (isOnline && telemetry != null) telemetry.uptimeFormatted else "--",
                    iconRes = R.drawable.timer,
                    iconTint = if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMetricColumn(
    label: String,
    value: String,
    @DrawableRes iconRes: Int,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopStatusCardOnline() {
    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LaptopStatusCard(
                telemetry = LaptopTelemetryDto(
                    isLocked = true,
                    isAcConnected = true,
                    batteryPercent = 100,
                    uptimeFormatted = "3h 42m"
                ),
                isOnline = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            LaptopStatusCard(
                telemetry = LaptopTelemetryDto(
                    isLocked = false,
                    isAcConnected = false,
                    batteryPercent = 40,
                    uptimeFormatted = "3h 42m"
                ),
                isOnline = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            LaptopStatusCard(
                telemetry = null,
                isOnline = false
            )
        }
    }
}
