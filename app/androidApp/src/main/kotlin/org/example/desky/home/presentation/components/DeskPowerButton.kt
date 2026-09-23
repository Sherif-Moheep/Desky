package org.example.desky.home.presentation.components

import org.example.desky.core.presentation.util.playTactileClick
import org.example.desky.core.presentation.util.rememberVibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun DeskPowerButton(
    isDeskOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vibrator = rememberVibrator()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val activeColor = MaterialTheme.colorScheme.primary
    val buttonSize = 190.dp

    // Tactile press spring animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )

    // Animated colors and glow transitions
    val animatedActiveColor by animateColorAsState(
        targetValue = if (isDeskOn) activeColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 350),
        label = "ring_color"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isDeskOn) activeColor else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 350),
        label = "icon_tint"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isDeskOn) 0.35f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "glow_alpha"
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
                                activeColor.copy(alpha = glowAlpha),
                                activeColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2f
                        )
                    )
                }
            }

            // 2. Outer Glowing Bezel Ring
            Canvas(modifier = Modifier.size(buttonSize)) {
                val strokeWidth = 5.dp.toPx()
                drawCircle(
                    color = animatedActiveColor,
                    style = Stroke(width = strokeWidth),
                    radius = (size.minDimension - strokeWidth) / 2f
                )
            }

            // 3. Inner Tactile Hardware Cap
            Surface(
                modifier = Modifier
                    .size(buttonSize - 20.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
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
                        contentDescription = if (isDeskOn) "Turn Desk Power Off" else "Turn Desk Power On",
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
            Text(
                text = if (isDeskOn) "ON" else "OFF",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDeskOn) animatedActiveColor else MaterialTheme.colorScheme.outline
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
                    isDeskOn = true,
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
                    isDeskOn = false,
                    onToggle = {}
                )
            }
        }
    }
}
