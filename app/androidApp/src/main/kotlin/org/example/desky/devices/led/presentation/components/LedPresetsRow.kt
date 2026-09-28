package org.example.desky.devices.led.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.devices.led.presentation.model.PreviewLedPresets
import org.example.desky.devices.led.presentation.model.LedPresetItem
import kotlin.math.abs

@Composable
fun LedPresetsRow(
    presets: List<LedPresetItem>,
    selectedColor: Color,
    onPresetSelect: (LedPresetItem) -> Unit,
    onAddPresetClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPresetLongClick: (LedPresetItem) -> Unit,
    enabled: Boolean = true
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        label = "presetsAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        Text(
            text = "Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                presets,
                key = { it.id }
            ) { preset ->
                val isSelected = isColorMatch(preset.color, selectedColor)

                PresetCard(
                    preset = preset,
                    isSelected = isSelected,
                    enabled = enabled,
                    onClick = { onPresetSelect(preset) },
                    onLongClick = { onPresetLongClick(preset) }
                )
            }

            item(key = "add_preset") {
                AddPresetCard(
                    enabled = enabled,
                    onClick = onAddPresetClick
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: LedPresetItem,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val borderWidth = if (isSelected) 2.dp else 1.dp

    OutlinedCard(
        modifier = modifier
            .width(86.dp)
            .height(96.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Preset Color Orb
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = preset.color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Subtle white ring highlight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.Transparent,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddPresetCard(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .width(86.dp)
            .height(96.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Add Preset",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Checks if two colors are visually close enough to be considered matching.
 */
private fun isColorMatch(
    c1: Color,
    c2: Color
): Boolean {
    val deltaR = abs(c1.red - c2.red)
    val deltaG = abs(c1.green - c2.green)
    val deltaB = abs(c1.blue - c2.blue)
    return deltaR < 0.05f && deltaG < 0.05f && deltaB < 0.05f
}

@PreviewLightDark
@Composable
fun PreviewLedPresetsRow() {
    var selectedColor by remember { mutableStateOf(Color(0xFFFF9E3B)) }

    DeskyTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            LedPresetsRow(
                presets = PreviewLedPresets,
                selectedColor = selectedColor,
                onPresetSelect = { selectedColor = it.color },
                onPresetLongClick = {},
                onAddPresetClick = {},
                enabled = true
            )
        }
    }
}
