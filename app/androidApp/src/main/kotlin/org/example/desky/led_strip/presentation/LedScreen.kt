package org.example.desky.led_strip.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import org.example.desky.R
import org.example.desky.core.theme.DeskyTheme
import org.example.desky.led_strip.presentation.components.DonutColorWheel
import org.example.desky.led_strip.presentation.components.LedBrightnessSlider
import org.example.desky.led_strip.presentation.components.LedHeaderCard
import org.example.desky.led_strip.presentation.components.LedPresetsRow
import org.example.desky.led_strip.presentation.components.LedTopBar
import org.example.desky.led_strip.presentation.components.PresetBottomSheet
import org.example.desky.led_strip.presentation.contract.LedEffect
import org.example.desky.led_strip.presentation.contract.LedIntent
import org.example.desky.led_strip.presentation.contract.LedState
import org.example.desky.led_strip.presentation.model.PreviewLedPresets
import org.example.desky.led_strip.presentation.model.PresetSheetMode
import org.example.desky.navigation.BottomNavBar
import org.example.desky.navigation.Route
import org.koin.androidx.compose.koinViewModel

@Composable
fun LedScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onNavigateToTab: (NavKey) -> Unit,
    viewModel: LedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LedEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LedScreenContent(
        state = state,
        onIntent = viewModel::sendIntent,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onNavigateToTab = onNavigateToTab,
        modifier = modifier
    )
}

@Composable
fun LedScreenContent(
    state: LedState,
    onIntent: (LedIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onNavigateToTab: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            LedTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            BottomNavBar(
                selectedKey = Route.Devices,
                onSelectedKey = onNavigateToTab
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Device Header Card (Identity, Status Badge & Power Switch)
            LedHeaderCard(
                isOn = state.isOn,
                onTogglePower = { onIntent(LedIntent.TogglePower(it)) },
                status = state.connectionStatus,
                onRetryConnection = { onIntent(LedIntent.RetryConnection) },
                name = "Desk Backlight",
                iconRes = R.drawable.led_strip
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Interactive Donut Color Wheel
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DonutColorWheel(
                    selectedColor = state.selectedColor,
                    onColorChange = { onIntent(LedIntent.ChangeColor(it)) },
                    enabled = state.isOn,
                    modifier = Modifier.size(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Brightness Slider
            LedBrightnessSlider(
                brightness = state.brightness,
                onBrightnessChange = { onIntent(LedIntent.ChangeBrightness(it)) },
                enabled = state.isOn
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Presets Row (Tap to apply, Long-press to edit/delete, "+" to create)
            LedPresetsRow(
                presets = state.presets,
                selectedColor = state.selectedColor,
                onPresetSelect = { preset ->
                    onIntent(LedIntent.SelectPreset(preset))
                },
                onAddPresetClick = {
                    onIntent(LedIntent.OpenSheet(PresetSheetMode.Create))
                },
                onPresetLongClick = { preset ->
                    onIntent(LedIntent.OpenSheet(PresetSheetMode.Edit(preset)))
                },
                enabled = state.isOn
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Customization Modal Bottom Sheet
        state.activeSheetMode?.let { mode ->
            PresetBottomSheet(
                mode = mode,
                onDismiss = { onIntent(LedIntent.DismissSheet) },
                onSavePreset = { onIntent(LedIntent.SavePreset(it)) },
                onDeletePreset = { onIntent(LedIntent.DeletePreset(it)) },
                currentColor = state.selectedColor,
                currentBrightness = state.brightness
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLedScreen() {
    DeskyTheme {
        LedScreenContent(
            state = LedState(
                isOn = true,
                presets = PreviewLedPresets,
                brightness = .85f
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onNavigateToTab = {}
        )
    }
}
