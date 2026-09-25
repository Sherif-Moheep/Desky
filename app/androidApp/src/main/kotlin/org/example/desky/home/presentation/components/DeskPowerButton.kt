package org.example.desky.home.presentation.components

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
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.core.presentation.util.playTactileClick
import org.example.desky.core.presentation.util.rememberVibrator
import org.example.desky.home.presentation.model.DeskPowerState

@Composable
fun DeskPowerButton(
    powerState: DeskPowerState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vibrator = rememberVibrator()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isInteractive = powerState == DeskPowerState.OFF || powerState == DeskPowerState.ON

    val activeColor = MaterialTheme.colorScheme.primary
    val buttonSize = 190.dp

    // Tactile press spring animation (active only when interactive)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isInteractive) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )

    // Animated colors and glow transitions
    val animatedActiveColor by animateColorAsState(
        targetValue = when (powerState) {
            DeskPowerState.ON -> activeColor
            DeskPowerState.BOOTING -> StatusAmber
            DeskPowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            DeskPowerState.OFF -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 350),
        label = "ring_color"
    )

    val iconTint by animateColorAsState(
        targetValue = when (powerState) {
            DeskPowerState.ON -> activeColor
            DeskPowerState.BOOTING -> StatusAmber
            DeskPowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            DeskPowerState.OFF -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 350),
        label = "icon_tint"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when (powerState) {
            DeskPowerState.ON -> 0.35f
            DeskPowerState.BOOTING -> 0.25f
            DeskPowerState.SHUTTING_DOWN -> 0.25f
            DeskPowerState.OFF -> 0f
        },
        animationSpec = tween(durationMillis = 400),
        label = "glow_alpha"
    )

    // Infinite rotation animation for BOOTING state
    val infiniteTransition = rememberInfiniteTransition(label = "desk_spin_transition")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "desk_spin_rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(buttonSize + 40.dp)
                .scale(scale)
        ) {
            // 1. Ambient Glow behind the ring
            if (glowAlpha > 0f) {
                Canvas(modifier = Modifier.size(buttonSize + 40.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedActiveColor.copy(alpha = glowAlpha),
                                animatedActiveColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2f
                        )
                    )
                }
            }

            // 2. Outer Glowing Bezel Ring (Spinning arc if BOOTING, solid circle otherwise)
            if (powerState == DeskPowerState.BOOTING) {
                Canvas(
                    modifier = Modifier
                        .size(buttonSize)
                        .rotate(spinRotation)
                ) {
                    val strokeWidth = 5.dp.toPx()
                    drawArc(
                        color = animatedActiveColor,
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            } else {
                Canvas(modifier = Modifier.size(buttonSize)) {
                    val strokeWidth = 5.dp.toPx()
                    drawCircle(
                        color = animatedActiveColor,
                        style = Stroke(width = strokeWidth),
                        radius = (size.minDimension - strokeWidth) / 2f
                    )
                }
            }

            // 3. Inner Tactile Hardware Cap
            Surface(
                modifier = Modifier
                    .size(buttonSize - 20.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = isInteractive,
                        onClick = {
                            vibrator.playTactileClick()
                            onToggle()
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
                    // Center Power Icon
                    Icon(
                        painter = painterResource(id = R.drawable.power),
                        contentDescription = when (powerState) {
                            DeskPowerState.ON -> "Turn Desk Power Off"
                            DeskPowerState.OFF -> "Turn Desk Power On"
                            DeskPowerState.BOOTING -> "Desk Power Starting..."
                            DeskPowerState.SHUTTING_DOWN -> "Desk Power Shutting Down..."
                        },
                        tint = iconTint,
                        modifier = Modifier.size(buttonSize * 0.28f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Status Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Desk Power: ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val (statusText, statusColor) = when (powerState) {
                DeskPowerState.ON -> "ON" to animatedActiveColor
                DeskPowerState.BOOTING -> "STARTING..." to StatusAmber
                DeskPowerState.SHUTTING_DOWN -> "SHUTTING DOWN..." to MaterialTheme.colorScheme.error
                DeskPowerState.OFF -> "OFF" to MaterialTheme.colorScheme.outline
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}



@PreviewLightDark
@Composable
fun PreviewDeskPowerButtonOn() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DeskPowerButton(
                    powerState = DeskPowerState.ON,
                    onToggle = {}
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewDeskPowerButtonBooting() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DeskPowerButton(
                    powerState = DeskPowerState.BOOTING,
                    onToggle = {}
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewDeskPowerButtonOff() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DeskPowerButton(
                    powerState = DeskPowerState.OFF,
                    onToggle = {}
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewDeskPowerButtonShuttingDown() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DeskPowerButton(
                    powerState = DeskPowerState.SHUTTING_DOWN,
                    onToggle = {}
                )
            }
        }
    }
}
