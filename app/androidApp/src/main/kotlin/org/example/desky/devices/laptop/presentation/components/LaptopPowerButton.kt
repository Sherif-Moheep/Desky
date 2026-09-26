package org.example.desky.devices.laptop.presentation.components

import org.example.desky.core.presentation.util.playTactileClick
import org.example.desky.core.presentation.util.rememberVibrator
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.presentation.theme.StatusAmber
import org.example.desky.devices.laptop.presentation.model.LaptopPowerState

@Composable
fun LaptopPowerButton(
    powerState: LaptopPowerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val vibrator = rememberVibrator()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isInteractive = powerState == LaptopPowerState.OFF || powerState == LaptopPowerState.ON

    // Press spring scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isInteractive) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "power_button_scale"
    )

    // Animated colors based on state
    val activeColor = MaterialTheme.colorScheme.primary
    val ringColor by animateColorAsState(
        targetValue = when (powerState) {
            LaptopPowerState.ON -> activeColor
            LaptopPowerState.BOOTING -> StatusAmber
            LaptopPowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            LaptopPowerState.OFF -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(300),
        label = "power_ring_color"
    )

    val iconTint by animateColorAsState(
        targetValue = when (powerState) {
            LaptopPowerState.ON -> activeColor
            LaptopPowerState.BOOTING -> StatusAmber
            LaptopPowerState.SHUTTING_DOWN -> MaterialTheme.colorScheme.error
            LaptopPowerState.OFF -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "power_icon_tint"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when (powerState) {
            LaptopPowerState.ON -> 0.35f
            LaptopPowerState.BOOTING -> 0.25f
            LaptopPowerState.SHUTTING_DOWN -> 0.25f
            LaptopPowerState.OFF -> 0f
        },
        animationSpec = tween(350),
        label = "power_glow_alpha"
    )

    // Rotation animation for BOOTING state
    val infiniteTransition = rememberInfiniteTransition(label = "power_spin_transition")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 12.dp)
            .scale(scale)
    ) {
        // 1. Ambient Glow behind the ring
        if (glowAlpha > 0f) {
            Canvas(modifier = Modifier.size(size + 12.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = glowAlpha),
                            ringColor.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = this.size.minDimension / 2f
                    )
                )
            }
        }

        // 2. Bezel Ring (Spinning arc if BOOTING, solid circle otherwise)
        if (powerState == LaptopPowerState.BOOTING) {
            Canvas(
                modifier = Modifier
                    .size(size)
                    .rotate(spinRotation)
            ) {
                val strokeWidth = 3.dp.toPx()
                drawArc(
                    color = ringColor,
                    startAngle = 0f,
                    sweepAngle = 280f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        } else {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 3.dp.toPx()
                drawCircle(
                    color = ringColor,
                    style = Stroke(width = strokeWidth),
                    radius = (this.size.minDimension - strokeWidth) / 2f
                )
            }
        }

        // 3. Inner Tactile Hardware Cap
        Surface(
            modifier = Modifier
                .size(size - 6.dp)
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
                    contentDescription = when (powerState) {
                        LaptopPowerState.ON -> "Shut Down Laptop"
                        LaptopPowerState.OFF -> "Turn ON Laptop"
                        LaptopPowerState.BOOTING -> "Booting..."
                        LaptopPowerState.SHUTTING_DOWN -> "Shutting Down..."
                    },
                    tint = iconTint,
                    modifier = Modifier.size(size * 0.44f)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopPowerButtonStates() {
    DeskyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                LaptopPowerButton(powerState = LaptopPowerState.ON, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                LaptopPowerButton(powerState = LaptopPowerState.BOOTING, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                LaptopPowerButton(powerState = LaptopPowerState.OFF, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                LaptopPowerButton(powerState = LaptopPowerState.SHUTTING_DOWN, onClick = {})
            }
        }
    }
}
