package org.example.desky.devices.led_strip.presentation.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.core.presentation.theme.DeskyTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val RainbowColors = listOf(
    Color(0xFFFF0000), // 0° Red
    Color(0xFFFFFF00), // 60° Yellow
    Color(0xFF00FF00), // 120° Green
    Color(0xFF00FFFF), // 180° Cyan
    Color(0xFF0000FF), // 240° Blue
    Color(0xFFFF00FF), // 300° Magenta
    Color(0xFFFF0000)  // 360° Red
)

/**
 * Interactive Donut Color Wheel matching Desky's wireframe.
 * - Circular Hue/Saturation sweep gradient ring
 * - Center glowing circular preview disc
 * - Touch-draggable selector ring thumb
 * - Disabled state with smooth alpha dimming
 */
@Composable
fun DonutColorWheel(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
) {
    // Track hue (0..360) and saturation (0..1)
    var currentHue by remember { mutableFloatStateOf(0f) }
    var currentSat by remember { mutableFloatStateOf(1f) }

    // Sync from selectedColor when updated externally
    LaunchedEffect(selectedColor) {
        val hsv = FloatArray(3)
        AndroidColor.RGBToHSV(
            (selectedColor.red * 255).toInt(),
            (selectedColor.green * 255).toInt(),
            (selectedColor.blue * 255).toInt(),
            hsv
        )
        currentHue = hsv[0]
        currentSat = hsv[1]
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        label = "colorWheelAlpha"
    )

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .alpha(animatedAlpha),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val (hue, sat) = calculateHueSat(offset, size.width.toFloat(), size.height.toFloat())
                                currentHue = hue
                                currentSat = sat
                                onColorChange(hsvToColor(hue, sat))
                            }
                        }.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val (hue, sat) = calculateHueSat(offset, size.width.toFloat(), size.height.toFloat())
                                    currentHue = hue
                                    currentSat = sat
                                    onColorChange(hsvToColor(hue, sat))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val (hue, sat) = calculateHueSat(change.position, size.width.toFloat(), size.height.toFloat())
                                    currentHue = hue
                                    currentSat = sat
                                    onColorChange(hsvToColor(hue, sat))
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2f - 12.dp.toPx()
            val innerRadius = outerRadius * 0.52f
            val ringWidth = outerRadius - innerRadius
            val ringCenterRadius = innerRadius + ringWidth / 2f

            // 1. Rainbow Hue Sweep Ring
            drawCircle(
                brush = Brush.sweepGradient(RainbowColors, center = center),
                radius = ringCenterRadius,
                style = Stroke(width = ringWidth)
            )

            // 2. White Desaturation Radial Gradient (Pastel inner to Vibrant outer)
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White.copy(alpha = 0.75f),
                        0.5f to Color.White.copy(alpha = 0.45f),
                        0.85f to Color.White.copy(alpha = 0.1f),
                        1.0f to Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius
                ),
                radius = ringCenterRadius,
                style = Stroke(width = ringWidth)
            )

            // 3. Subtle dark inner/outer ring borders for crisp definition
            drawCircle(
                color = outlineColor,
                radius = outerRadius,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = outlineColor,
                radius = innerRadius,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 4. Center Circular Preview Disc
            val previewRadius = innerRadius * 0.72f

            // Ambient background glow under center preview
            drawCircle(
                color = selectedColor.copy(alpha = 0.25f),
                radius = previewRadius + 6.dp.toPx()
            )
            // Solid center color
            drawCircle(
                color = selectedColor,
                radius = previewRadius
            )
            // Center preview border
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = previewRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // 5. Draggable Selector Thumb on the Donut Ring
            val angleRad = Math.toRadians(currentHue.toDouble())
            val thumbDistance = innerRadius + (currentSat * ringWidth)
            val thumbX = center.x + (thumbDistance * cos(angleRad)).toFloat()
            val thumbY = center.y + (thumbDistance * sin(angleRad)).toFloat()
            val thumbCenter = Offset(thumbX, thumbY)
            val thumbRadius = 14.dp.toPx()

            // Shadow / contrast ring
            drawCircle(
                color = Color.Black.copy(alpha = 0.4f),
                radius = thumbRadius + 2.dp.toPx(),
                center = thumbCenter
            )
            // White ring border
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = thumbCenter,
                style = Stroke(width = 3.5.dp.toPx())
            )
            // Thumb inner fill with active color
            drawCircle(
                color = selectedColor,
                radius = thumbRadius - 3.dp.toPx(),
                center = thumbCenter
            )
        }
    }
}

/**
 * Calculates (Hue, Saturation) from a touch offset relative to canvas dimensions.
 */
private fun calculateHueSat(offset: Offset, width: Float, height: Float): Pair<Float, Float> {
    val centerX = width / 2f
    val centerY = height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY

    // Angle in degrees [0..360)
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0f) angle += 360f

    val outerRadius = (minOf(width, height) / 2f) - 12f
    val innerRadius = outerRadius * 0.52f
    val distance = sqrt(dx * dx + dy * dy)

    // Normalize saturation [0..1] between inner and outer ring
    val sat = ((distance - innerRadius) / (outerRadius - innerRadius)).coerceIn(0.05f, 1f)

    return Pair(angle, sat)
}

/**
 * Converts Hue, Saturation, Value to Compose Color.
 */
private fun hsvToColor(hue: Float, sat: Float, value: Float = 1f): Color {
    val hsv = floatArrayOf(hue.coerceIn(0f, 360f), sat.coerceIn(0f, 1f), value.coerceIn(0f, 1f))
    return Color(AndroidColor.HSVToColor(hsv))
}

@PreviewLightDark
@Composable
fun PreviewDonutColorWheel() {
    var color by remember { mutableFloatStateOf(0f) }
    val amberColor = Color(0xFFFF9E3B)

    DeskyTheme {
        Box(
            modifier = Modifier
                .size(320.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DonutColorWheel(
                enabled = true,
                selectedColor = amberColor,
                onColorChange = {}
            )
        }
    }
}
