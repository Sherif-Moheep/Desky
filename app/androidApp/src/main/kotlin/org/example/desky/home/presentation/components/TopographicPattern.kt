package org.example.desky.home.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

@Composable
fun TopographicPattern(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val spacing = 30f
        val numLines = (size.height / spacing).toInt()

        for (i in 0..numLines) {
            val yOffset = i * spacing
            val path = Path().apply {
                moveTo(0f, yOffset)
                val step = 10f
                var x = 0f
                while (x <= size.width) {
                    // Combine sine waves to simulate terrain contours
                    val wave1 = sin((x + i * 50f) * 0.01f) * 20f
                    val wave2 = sin(x * 0.005f - i * 0.2f) * 15f
                    lineTo(x, yOffset + wave1 + wave2)
                    x += step
                }
            }
            drawPath(
                path = path,
                color = Color.Red.copy(alpha = 0.3f),
                style = Stroke(width = 2f)
            )
        }
    }
}