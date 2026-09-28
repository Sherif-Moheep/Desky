package org.example.desky.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.desky.R
import org.example.desky.core.presentation.components.TopLevelAppBar
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.settings.components.AppInfoCard
import org.example.desky.settings.components.DeviceConfigCard
import org.example.desky.settings.components.ResetSettingsDialog
import org.example.desky.settings.components.ThemeSelectorCard
import org.example.desky.settings.presentation.contract.DeviceType
import org.example.desky.settings.presentation.contract.SettingsEffect
import org.example.desky.settings.presentation.contract.SettingsIntent
import org.example.desky.settings.presentation.contract.SettingsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopLevelAppBar("Settings")
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
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Devices & Networking
            SectionHeader(title = "Devices & Networking")

            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = state.espHost,
                onHostChange = { onIntent(SettingsIntent.UpdateEspHost(it)) },
                port = state.espPort,
                onPortChange = { onIntent(SettingsIntent.UpdateEspPort(it)) },
                status = state.espStatus,
                onActionClick = { onIntent(SettingsIntent.PingDevice(DeviceType.ESP_RELAY)) }
            )

            DeviceConfigCard(
                name = "Laptop",
                subtitle = "Windows Ktor server",
                iconRes = R.drawable.laptop,
                host = state.laptopHost,
                onHostChange = { onIntent(SettingsIntent.UpdateLaptopHost(it)) },
                port = state.laptopPort,
                onPortChange = { onIntent(SettingsIntent.UpdateLaptopPort(it)) },
                status = state.laptopStatus,
                onActionClick = { onIntent(SettingsIntent.PingDevice(DeviceType.LAPTOP)) }
            )

            DeviceConfigCard(
                name = "LED Strip Controller",
                subtitle = "Magic Home Controller",
                iconRes = R.drawable.led_strip,
                host = state.ledHost,
                onHostChange = { onIntent(SettingsIntent.UpdateLedHost(it)) },
                port = state.ledPort,
                onPortChange = { onIntent(SettingsIntent.UpdateLedPort(it)) },
                status = state.ledStatus,
                onActionClick = { onIntent(SettingsIntent.PingDevice(DeviceType.LED_STRIP)) }
            )

            // Section: Appearance
            SectionHeader(title = "Appearance")

            ThemeSelectorCard(
                currentTheme = state.themeMode,
                onThemeChange = { onIntent(SettingsIntent.SetThemeMode(it)) }
            )

            // Section: About & Maintenance
            SectionHeader(title = "About & Maintenance")

            AppInfoCard(
                onResetDefaults = { onIntent(SettingsIntent.ShowResetDialog) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (state.showResetDialog) {
        ResetSettingsDialog(
            onConfirm = { onIntent(SettingsIntent.ConfirmReset) },
            onDismiss = { onIntent(SettingsIntent.DismissResetDialog) }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@PreviewLightDark
@Composable
fun PreviewSettingsScreen() {
    DeskyTheme {
        SettingsContent(
            state = SettingsState(
                espHost = "desky-relay.local",
                espPort = "80",
                laptopHost = "desky-laptop.local",
                laptopPort = "8080",
                ledHost = "192.168.1.150",
                ledPort = "5577"
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}