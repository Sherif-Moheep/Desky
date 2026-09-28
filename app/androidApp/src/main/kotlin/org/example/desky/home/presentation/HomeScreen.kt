package org.example.desky.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.components.TopLevelAppBar
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.home.presentation.components.DeskPowerButton
import org.example.desky.home.presentation.components.HomeGreeting
import org.example.desky.home.presentation.components.HomeTelemetryCard
import org.example.desky.home.presentation.components.PowerOffConfirmationDialog
import org.example.desky.home.presentation.contract.HomeEffect
import org.example.desky.home.presentation.contract.HomeIntent
import org.example.desky.home.presentation.contract.HomeState
import org.example.desky.home.presentation.model.DeskPowerState
import org.example.desky.home.presentation.model.HomeTelemetryUiState
import org.example.desky.home.presentation.model.LaptopTelemetry
import org.example.desky.home.presentation.model.LightTelemetry
import org.example.desky.home.presentation.model.RelayTelemetry
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeScreenContent(
        state = state,
        onIntent = viewModel::sendIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopLevelAppBar(
                "Desky"
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
                .verticalScroll(scrollState)
        ) {
                Spacer(modifier = Modifier.height(12.dp))

                HomeGreeting(powerState = state.powerState)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DeskPowerButton(
                        powerState = state.powerState,
                        onToggle = { onIntent(HomeIntent.ToggleDeskPower) }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                HomeTelemetryCard(
                    state = state.telemetry
                )

                Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.showPowerOffDialog) {
            PowerOffConfirmationDialog(
                onConfirm = { onIntent(HomeIntent.ConfirmPowerOff) },
                onDismiss = { onIntent(HomeIntent.DismissPowerOffDialog) }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Previews
// ----------------------------------------------------------------------------

@PreviewLightDark
@Composable
fun PreviewHomeScreenOnline() {
    DeskyTheme {
        HomeScreenContent(
            state = HomeState(
                powerState = DeskPowerState.ON,
                connectionStatus = DeviceConnectionStatus.Online(12),
                telemetry = HomeTelemetryUiState()
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewHomeScreenBooting() {
    DeskyTheme {
        HomeScreenContent(
            state = HomeState(
                powerState = DeskPowerState.BOOTING,
                connectionStatus = DeviceConnectionStatus.Pinging,
                telemetry = HomeTelemetryUiState(
                    laptop = LaptopTelemetry(isOnline = false),
                    light = LightTelemetry(isOnline = true, isOn = false),
                    relay = RelayTelemetry(isOnline = true, isAcActive = true)
                )
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewHomeScreenDeskOff() {
    DeskyTheme {
        HomeScreenContent(
            state = HomeState(
                powerState = DeskPowerState.OFF,
                connectionStatus = DeviceConnectionStatus.Online(12),
                telemetry = HomeTelemetryUiState(
                    laptop = LaptopTelemetry(isOnline = false),
                    light = LightTelemetry(isOnline = true, isOn = false),
                    relay = RelayTelemetry(isOnline = true, isAcActive = false)
                )
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewHomeScreenWithDialog() {
    DeskyTheme {
        HomeScreenContent(
            state = HomeState(
                powerState = DeskPowerState.ON,
                showPowerOffDialog = true
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
