package org.example.desky.devices.list.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.presentation.components.TopLevelAppBar
import org.example.desky.R
import org.example.desky.core.presentation.components.ConfirmationDialog
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.core.domain.model.PowerState
import org.example.desky.devices.list.presentation.components.DeviceSummaryHeader
import org.example.desky.devices.list.presentation.components.LaptopDeviceCard
import org.example.desky.devices.list.presentation.components.LedDeviceCard
import org.example.desky.devices.list.presentation.contract.DevicesEffect
import org.example.desky.devices.list.presentation.contract.DevicesIntent
import org.example.desky.devices.list.presentation.contract.DevicesState
import org.koin.androidx.compose.koinViewModel

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    onNavigateToLaptopDetail: () -> Unit,
    onNavigateToLedDetail: () -> Unit ,
    viewModel: DevicesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        var snackbarJob: Job? = null
        viewModel.effect.collect { effect ->
            when (effect) {
                is DevicesEffect.ShowSnackbar -> {
                    snackbarJob?.cancel()
                    snackbarJob = this@LaunchedEffect.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    DevicesScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        onNavigateToLaptopDetail = onNavigateToLaptopDetail,
        onNavigateToLedDetail = onNavigateToLedDetail,
        modifier = modifier
    )
}

@Composable
fun DevicesScreenContent(
    state: DevicesState,
    onIntent: (DevicesIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateToLaptopDetail: () -> Unit,
    onNavigateToLedDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopLevelAppBar("Devices")
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(DevicesIntent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Network & devices summary header
                DeviceSummaryHeader(
                    onlineCount = state.onlineCount,
                    totalCount = state.totalDevicesCount
                )

                // Laptop Workstation Card
                LaptopDeviceCard(
                    host = state.laptopHost,
                    port = state.laptopPort,
                    serverStatus = state.laptopStatus,
                    espStatus = state.espStatus,
                    powerState = state.laptopPowerState,
                    isLocked = state.isLaptopLocked,
                    uptime = state.laptopUptime,
                    onPowerClick = {
                        onIntent(DevicesIntent.LaptopPowerClick)
                    },
                    onNavigateToDetail = onNavigateToLaptopDetail
                )

                // Desk Backlight (Magic Home LED Strip) Card
                LedDeviceCard(
                    host = state.ledHost,
                    port = state.ledPort,
                    status = state.ledStatus,
                    isPoweredOn = state.isLedPoweredOn,
                    brightness = state.ledBrightness,
                    color = state.ledColor,
                    colorName = state.ledColorName,
                    onPowerToggle = { targetOn ->
                        onIntent(DevicesIntent.ToggleLedPower(targetOn))
                    },
                    onNavigateToDetail = onNavigateToLedDetail
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (state.showLaptopShutdownDialog) {
            ConfirmationDialog(
                title = "Shut Down Workstation?",
                message = "Are you sure you want to turn off the laptop? Any unsaved work will be lost.",
                confirmText = "Shut Down",
                iconRes = R.drawable.power,
                onConfirm = { onIntent(DevicesIntent.ConfirmLaptopShutdown) },
                onDismiss = { onIntent(DevicesIntent.DismissLaptopShutdownDialog) }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Previews
// ----------------------------------------------------------------------------

@PreviewLightDark
@Composable
fun PreviewDevicesScreenAllOnline() {
    DeskyTheme {
        DevicesScreenContent(
            state = DevicesState(
                laptopHost = "Sherif-Laptop.local",
                laptopPort = 8080,
                laptopStatus = DeviceConnectionStatus.Online(12),
                espStatus = DeviceConnectionStatus.Online(8),
                laptopPowerState = PowerState.ON,
                isLaptopLocked = true,
                laptopUptime = "3h 42m",
                ledHost = "192.168.1.150",
                ledPort = 5577,
                ledStatus = DeviceConnectionStatus.Online(15),
                isLedPoweredOn = true,
                ledBrightness = 0.8f,
                ledColor = Color(0xFFE53935),
                ledColorName = "Crimson"
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateToLaptopDetail = {},
            onNavigateToLedDetail = {},
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewDevicesScreenOneOffline() {
    DeskyTheme {
        DevicesScreenContent(
            state = DevicesState(
                laptopHost = "Sherif-Laptop.local",
                laptopPort = 8080,
                laptopStatus = DeviceConnectionStatus.Offline("Offline"),
                espStatus = DeviceConnectionStatus.Online(8),
                laptopPowerState = PowerState.OFF,
                isLaptopLocked = false,
                laptopUptime = "0m",
                ledHost = "192.168.1.150",
                ledPort = 5577,
                ledStatus = DeviceConnectionStatus.Online(15),
                isLedPoweredOn = true,
                ledBrightness = 0.8f,
                ledColor = Color(0xFFE53935),
                ledColorName = "Crimson"
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateToLaptopDetail = {},
            onNavigateToLedDetail = {},
        )
    }
}
