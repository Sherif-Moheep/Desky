package org.example.desky.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.util.playTactileClick
import org.example.desky.core.presentation.util.rememberVibrator

@Composable
fun PowerButton(
    powerState: PowerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescription: String? = null,
    statusLabelPrefix: String? = null,
) {
    val vibrator = rememberVibrator()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isInteractive = powerState == PowerState.OFF || powerState == PowerState.ON
    val isLarge = size >= 100.dp

    // Tactile press spring animation (active only when interactive)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isInteractive) {
            if (isLarge) 0.94f else 0.92f
        } else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "power_button_scale"
    )

    val activeColor = MaterialTheme.colorScheme.primary

    // Animated colors and glow transitions
    val animatedActiveColor by animateColorAsState(
        targetValue = when (powerState) {
            PowerState.ON -> activeColor
            PowerState.BOOTING -> StatusAmber
            PowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            PowerState.OFF -> if (isLarge) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        },
        animationSpec = tween(durationMillis = 350),
        label = "power_ring_color"
    )

    val iconTint by animateColorAsState(
        targetValue = when (powerState) {
            PowerState.ON -> activeColor
            PowerState.BOOTING -> StatusAmber
            PowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            PowerState.OFF -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 350),
        label = "power_icon_tint"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when (powerState) {
            PowerState.ON -> 0.35f
            PowerState.BOOTING -> 0.25f
            PowerState.SHUTTING_DOWN -> 0.25f
            PowerState.OFF -> 0f
        },
        animationSpec = tween(durationMillis = 350),
        label = "power_glow_alpha"
    )

    // Infinite rotation animation for BOOTING state
    val infiniteTransition = rememberInfiniteTransition(label = "power_spin_transition")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "power_spin_rotation"
    )

    val haloPadding = if (isLarge) 40.dp else 12.dp
    val strokeWidth = if (isLarge) 5.dp else 3.dp
    val capSize = if (isLarge) size - 20.dp else size - 6.dp
    val iconSize = if (isLarge) size * 0.28f else size * 0.44f

    val buttonBox = @Composable {
        Box(
            contentAlignment = Alignment.Center,
            modifier = (if (statusLabelPrefix == null) modifier else Modifier)
                .size(size + haloPadding)
                .scale(scale)
        ) {
            // 1. Ambient Glow behind the ring
            if (glowAlpha > 0f) {
                Canvas(modifier = Modifier.size(size + haloPadding)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedActiveColor.copy(alpha = glowAlpha),
                                animatedActiveColor.copy(alpha = glowAlpha * 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = this.size.minDimension / 2f
                        )
                    )
                }
            }

            // 2. Bezel Ring (Spinning arc if BOOTING, solid circle otherwise)
            if (powerState == PowerState.BOOTING) {
                Canvas(
                    modifier = Modifier
                        .size(size)
                        .rotate(spinRotation)
                ) {
                    val strokeWidthPx = strokeWidth.toPx()
                    drawArc(
                        color = animatedActiveColor,
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            } else {
                Canvas(modifier = Modifier.size(size)) {
                    val strokeWidthPx = strokeWidth.toPx()
                    drawCircle(
                        color = animatedActiveColor,
                        style = Stroke(width = strokeWidthPx),
                        radius = (this.size.minDimension - strokeWidthPx) / 2f
                    )
                }
            }

            // 3. Inner Tactile Hardware Cap
            Surface(
                modifier = Modifier
                    .size(capSize)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = isInteractive,
                        onClick = {
                            vibrator.playTactileClick()
                            onClick()
                        }
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.power),
                        contentDescription = contentDescription ?: when (powerState) {
                            PowerState.ON -> "Turn Power Off"
                            PowerState.OFF -> "Turn Power On"
                            PowerState.BOOTING -> "Starting..."
                            PowerState.SHUTTING_DOWN -> "Shutting Down..."
                        },
                        tint = iconTint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }

    if (statusLabelPrefix != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            buttonBox()

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Status Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = statusLabelPrefix,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val (statusText, statusColor) = when (powerState) {
                    PowerState.ON -> "ON" to animatedActiveColor
                    PowerState.BOOTING -> "STARTING..." to StatusAmber
                    PowerState.SHUTTING_DOWN -> "SHUTTING DOWN..." to MaterialTheme.colorScheme.error
                    PowerState.OFF -> "OFF" to MaterialTheme.colorScheme.outline
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    } else {
        buttonBox()
    }
}

@PreviewLightDark
@Composable
fun PreviewPowerButtonCompact() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                PowerButton(powerState = PowerState.ON, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                PowerButton(powerState = PowerState.BOOTING, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                PowerButton(powerState = PowerState.OFF, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                PowerButton(powerState = PowerState.SHUTTING_DOWN, onClick = {})
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewPowerButtonLarge() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                PowerButton(
                    powerState = PowerState.ON,
                    onClick = {},
                    size = 190.dp,
                    statusLabelPrefix = "Desk Power: "
                )
            }
        }
    }
}
