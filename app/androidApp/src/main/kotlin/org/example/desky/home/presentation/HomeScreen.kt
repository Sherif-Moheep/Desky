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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.presentation.components.ConfirmationDialog
import org.example.desky.core.presentation.components.PowerButton
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.home.presentation.components.HomeGreeting
import org.example.desky.R
import org.example.desky.home.presentation.components.HomeTelemetryCard
import org.example.desky.home.presentation.contract.HomeEffect
import org.example.desky.home.presentation.contract.HomeIntent
import org.example.desky.home.presentation.contract.HomeState
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
        var snackbarJob: Job? = null
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> {
                    snackbarJob?.cancel()
                    snackbarJob = this.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(HomeIntent.RefreshTelemetry) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    PowerButton(
                        powerState = state.powerState,
                        onClick = { onIntent(HomeIntent.ToggleDeskPower) },
                        size = 190.dp,
                        statusLabelPrefix = "Desk Power: "
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                HomeTelemetryCard(
                    state = state.telemetry
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (state.showPowerOffDialog) {
            ConfirmationDialog(
                title = "Power Off Desk?",
                message = "Are you sure you want to turn off the desk? All connected systems will be powered down.",
                confirmText = "Power Off",
                iconRes = R.drawable.power,
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
                powerState = PowerState.ON,
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
                powerState = PowerState.BOOTING,
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
                powerState = PowerState.OFF,
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
                powerState = PowerState.ON,
                showPowerOffDialog = true
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
