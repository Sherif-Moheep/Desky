package org.example.desky.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.navigation3.runtime.NavKey
import org.example.desky.core.presentation.components.DeviceConnectionStatus
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.devices.presentation.components.DeviceSummaryHeader
import org.example.desky.devices.presentation.components.DevicesTopBar
import org.example.desky.devices.presentation.components.LaptopDeviceCard
import org.example.desky.devices.presentation.components.LedDeviceCard
import org.example.desky.devices.presentation.contract.DevicesEffect
import org.example.desky.devices.presentation.contract.DevicesIntent
import org.example.desky.devices.presentation.contract.DevicesState
import org.example.desky.laptop.presentation.components.LaptopShutdownDialog
import org.example.desky.navigation.BottomNavBar
import org.example.desky.navigation.Route
import org.koin.androidx.compose.koinViewModel

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    onNavigateToLaptopDetail: () -> Unit = {},
    onNavigateToLedDetail: () -> Unit = {},
    onNavigateToTab: (NavKey) -> Unit = {},
    viewModel: DevicesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DevicesEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    DevicesScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        onNavigateToLaptopDetail = onNavigateToLaptopDetail,
        onNavigateToLedDetail = onNavigateToLedDetail,
        onNavigateToTab = onNavigateToTab,
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
    onNavigateToTab: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            DevicesTopBar()
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
                Spacer(modifier = Modifier.height(4.dp))

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
            LaptopShutdownDialog(
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
                laptopHost = "desky-laptop.local",
                laptopPort = 8080,
                laptopStatus = DeviceConnectionStatus.Online(12),
                espStatus = DeviceConnectionStatus.Online(8),
                laptopPowerState = org.example.desky.laptop.presentation.model.LaptopPowerState.ON,
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
            onNavigateToTab = {}
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewDevicesScreenOneOffline() {
    DeskyTheme {
        DevicesScreenContent(
            state = DevicesState(
                laptopHost = "desky-laptop.local",
                laptopPort = 8080,
                laptopStatus = DeviceConnectionStatus.Offline("Offline"),
                espStatus = DeviceConnectionStatus.Online(8),
                laptopPowerState = org.example.desky.laptop.presentation.model.LaptopPowerState.OFF,
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
            onNavigateToTab = {}
        )
    }
}
