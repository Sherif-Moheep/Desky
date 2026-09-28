package org.example.desky.devices.led.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.example.desky.core.presentation.theme.DeskyTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.devices.led.presentation.model.LedPresetItem
import org.example.desky.devices.led.presentation.model.PresetSheetMode
import java.util.UUID
import kotlin.math.roundToInt

private val PresetNameSuggestions = listOf(
    "Focus", "Cozy", "Gaming", "Reading", "Night Shift", "Cyberpunk", "Movie", "Coding"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetBottomSheet(
    mode: PresetSheetMode,
    onDismiss: () -> Unit,
    onSavePreset: (LedPresetItem) -> Unit,
    onDeletePreset: (String) -> Unit,
    currentColor: Color,
    currentBrightness: Float,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        PresetBottomSheetContent(
            mode = mode,
            onDismiss = onDismiss,
            onSavePreset = onSavePreset,
            onDeletePreset = onDeletePreset,
            currentColor = currentColor,
            currentBrightness = currentBrightness,
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
fun PresetBottomSheetContent(
    mode: PresetSheetMode,
    onDismiss: () -> Unit,
    onSavePreset: (LedPresetItem) -> Unit,
    onDeletePreset: (String) -> Unit,
    currentColor: Color,
    currentBrightness: Float,
    modifier: Modifier = Modifier
) {
    val isEditMode = mode is PresetSheetMode.Edit
    val existingPreset = (mode as? PresetSheetMode.Edit)?.preset

    var name by remember { mutableStateOf(existingPreset?.name ?: "") }
    var presetColor by remember { mutableStateOf(existingPreset?.color ?: currentColor) }
    var presetBrightness by remember {
        mutableFloatStateOf(existingPreset?.brightness ?: currentBrightness)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
            // Header Title
            Text(
                text = if (isEditMode) "Edit Preset" else "New Preset",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snapshot Preview Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color = presetColor, shape = CircleShape)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = formatColorHex(presetColor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Brightness: ${(presetBrightness * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isEditMode && (presetColor != currentColor || presetBrightness != currentBrightness)) {
                        OutlinedButton(
                            onClick = {
                                presetColor = currentColor
                                presetBrightness = currentBrightness
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Use Current",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Preset Name") },
                placeholder = { Text("e.g. Late Night Coding") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Suggestion Chips (Quick 1-tap naming)
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PresetNameSuggestions) { suggestion ->
                    val isChipSelected = name.equals(suggestion, ignoreCase = true)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChipSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isChipSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier.clickable { name = suggestion }
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isChipSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            existingPreset?.id?.let { onDeletePreset(it) }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val updated = existingPreset?.copy(
                                name = name.trim(),
                                color = presetColor,
                                brightness = presetBrightness
                            ) ?: LedPresetItem(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                color = presetColor,
                                brightness = presetBrightness
                            )
                            onSavePreset(updated)
                            onDismiss()
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        val newPreset = LedPresetItem(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            color = presetColor,
                            brightness = presetBrightness
                        )
                        onSavePreset(newPreset)
                        onDismiss()
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Save Preset",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
}

private fun formatColorHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

@PreviewLightDark
@Composable
fun PreviewPresetBottomSheetCreate() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            PresetBottomSheetContent(
                mode = PresetSheetMode.Create,
                onDismiss = {},
                onSavePreset = {},
                onDeletePreset = {},
                currentColor = Color(0xFFFF9E3B),
                currentBrightness = 0.8f
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewPresetBottomSheetEdit() {
    DeskyTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            PresetBottomSheetContent(
                mode = PresetSheetMode.Edit(
                    preset = LedPresetItem(
                        id = "1",
                        name = "Focus Amber",
                        color = Color(0xFFFF9E3B),
                        brightness = 0.85f
                    )
                ),
                onDismiss = {},
                onSavePreset = {},
                onDeletePreset = {},
                currentColor = Color(0xFF00E5FF),
                currentBrightness = 0.5f
            )
        }
    }
}
