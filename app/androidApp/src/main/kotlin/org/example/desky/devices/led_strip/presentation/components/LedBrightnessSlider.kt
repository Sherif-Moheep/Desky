package org.example.desky.devices.led_strip.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun LedBrightnessSlider(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        label = "brightnessAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        Text(
            text = "Brightness",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.brightness),
                contentDescription = "Brightness",
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            val sliderState = remember { SliderState(brightness.coerceIn(0.01f, 1f)) }

            LaunchedEffect(brightness) {
                if (sliderState.value != brightness) {
                    sliderState.value = brightness.coerceIn(0.01f, 1f)
                }
            }

            Slider(
                state = sliderState,
                enabled = enabled,
                onValueChange = {
                    onBrightnessChange(it)
                },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            val percentage = (brightness * 100).roundToInt().coerceIn(1, 100)
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun PreviewLedBrightnessSlider() {
    var brightness by remember { mutableFloatStateOf(0.65f) }

    DeskyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LedBrightnessSlider(
                brightness = brightness,
                onBrightnessChange = { brightness = it },
                enabled = true
            )
        }
    }
}
