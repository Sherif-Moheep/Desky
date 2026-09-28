package org.example.desky.devices.laptop.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.devices.laptop.presentation.components.LaptopActionsRow
import org.example.desky.devices.laptop.presentation.components.LaptopHeaderCard
import org.example.desky.devices.laptop.presentation.components.LaptopRestartDialog
import org.example.desky.devices.laptop.presentation.components.LaptopShutdownDialog
import org.example.desky.core.presentation.components.DetailTopAppBar
import org.example.desky.devices.laptop.presentation.components.LaptopStatusCard
import org.example.desky.devices.laptop.presentation.contract.LaptopEffect
import org.example.desky.devices.laptop.presentation.contract.LaptopIntent
import org.example.desky.devices.laptop.presentation.contract.LaptopState
import org.example.desky.devices.laptop.presentation.model.LaptopPowerState
import org.example.desky.devices.laptop.presentation.model.LaptopTelemetryState
import org.koin.androidx.compose.koinViewModel

@Composable
fun LaptopDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: LaptopViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LaptopEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LaptopDetailScreenContent(
        state = state,
        onIntent = viewModel::sendIntent,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun LaptopDetailScreenContent(
    state: LaptopState,
    onIntent: (LaptopIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DetailTopAppBar(
                title = "Laptop",
                onBackClick = onBackClick
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

            // 1. Device Header Card
            LaptopHeaderCard(
                powerState = state.powerState,
                status = state.connectionStatus,
                onPowerClick = {
                    if (state.powerState == LaptopPowerState.ON) {
                        onIntent(LaptopIntent.RequestShutdown)
                    } else {
                        onIntent(LaptopIntent.PowerClick)
                    }
                },
                onRetryConnection = { onIntent(LaptopIntent.RetryConnection) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. System Status Card (The 3 Essentials: Session, Power, Uptime)
            LaptopStatusCard(
                telemetry = state.telemetry,
                isOnline = state.isOnline
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Actions Row (Lock, Sleep, Restart)
            LaptopActionsRow(
                onLockClick = { onIntent(LaptopIntent.Lock) },
                onSleepClick = { onIntent(LaptopIntent.Sleep) },
                onRestartClick = { onIntent(LaptopIntent.RequestRestart) },
                enabled = state.isOnline
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Confirmation Dialogs
        if (state.showShutdownDialog) {
            LaptopShutdownDialog(
                onConfirm = { onIntent(LaptopIntent.ConfirmShutdown) },
                onDismiss = { onIntent(LaptopIntent.DismissShutdownDialog) }
            )
        }

        if (state.showRestartDialog) {
            LaptopRestartDialog(
                onConfirm = { onIntent(LaptopIntent.ConfirmRestart) },
                onDismiss = { onIntent(LaptopIntent.DismissRestartDialog) }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopDetailScreenOnline() {
    DeskyTheme {
        LaptopDetailScreenContent(
            state = LaptopState(
                powerState = LaptopPowerState.ON,
                connectionStatus = DeviceConnectionStatus.Online(12),
                telemetry = LaptopTelemetryState(
                    isLocked = true,
                    isAcConnected = true,
                    batteryPercent = 100,
                    uptimeFormatted = "3h 42m"
                )
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewLaptopDetailScreenOffline() {
    DeskyTheme {
        LaptopDetailScreenContent(
            state = LaptopState(
                powerState = LaptopPowerState.OFF,
                connectionStatus = DeviceConnectionStatus.Offline("Offline"),
                telemetry = LaptopTelemetryState()
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
        )
    }
}
