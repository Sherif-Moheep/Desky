package org.example.desky.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.desky.R
import org.example.desky.navigation.BottomNavBar
import org.example.desky.navigation.Route
import org.example.desky.settings.components.AppInfoCard
import org.example.desky.settings.components.AppThemeMode
import org.example.desky.settings.components.DeviceConfigCard
import org.example.desky.settings.components.DeviceConnectionStatus
import org.example.desky.settings.components.ResetSettingsDialog
import org.example.desky.settings.components.SettingsTopBar
import org.example.desky.settings.components.ThemeSelectorCard
import org.example.desky.theme.DeskyTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }

    // Device Config State
    var espHost by remember { mutableStateOf("desky-relay.local") }
    var espPort by remember { mutableStateOf("80") }
    var espStatus by remember { mutableStateOf<DeviceConnectionStatus>(DeviceConnectionStatus.Online(12)) }

    var laptopHost by remember { mutableStateOf("desky-laptop.local") }
    var laptopPort by remember { mutableStateOf("8080") }
    var laptopStatus by remember { mutableStateOf<DeviceConnectionStatus>(DeviceConnectionStatus.Online(4)) }

    var ledHost by remember { mutableStateOf("192.168.1.150") }
    var ledPort by remember { mutableStateOf("5577") }
    var ledStatus by remember { mutableStateOf<DeviceConnectionStatus>(DeviceConnectionStatus.Idle) }

    // Appearance State
    var themeMode by remember { mutableStateOf(AppThemeMode.SYSTEM) }

    Scaffold(
        topBar = {
            SettingsTopBar()
        },
        bottomBar = {
            BottomNavBar(
                selectedKey = Route.Settings,
                onSelectedKey = { },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Devices & Networking
            SectionHeader(title = "Devices & Networking")

            DeviceConfigCard(
                name = "ESP32 Power Relay",
                subtitle = "Laptop charger AC relay",
                iconRes = R.drawable.electricity,
                host = espHost,
                onHostChange = { espHost = it },
                port = espPort,
                onPortChange = { espPort = it },
                status = espStatus,
                onActionClick = {
                    coroutineScope.launch {
                        espStatus = DeviceConnectionStatus.Pinging
                        delay(600)
                        espStatus = DeviceConnectionStatus.Online((8..20).random().toLong())
                    }
                }
            )

            DeviceConfigCard(
                name = "Laptop",
                subtitle = "Windows Ktor server",
                iconRes = R.drawable.laptop,
                host = laptopHost,
                onHostChange = { laptopHost = it },
                port = laptopPort,
                onPortChange = { laptopPort = it },
                status = laptopStatus,
                onActionClick = {
                    coroutineScope.launch {
                        laptopStatus = DeviceConnectionStatus.Pinging
                        delay(500)
                        laptopStatus = DeviceConnectionStatus.Online((3..10).random().toLong())
                    }
                }
            )

            DeviceConfigCard(
                name = "LED Strip Controller",
                subtitle = "Magic Home Controller",
                iconRes = R.drawable.led_strip,
                host = ledHost,
                onHostChange = { ledHost = it },
                port = ledPort,
                onPortChange = { ledPort = it },
                status = ledStatus,
                onActionClick = {
                    coroutineScope.launch {
                        ledStatus = DeviceConnectionStatus.Pinging
                        delay(700)
                        ledStatus = DeviceConnectionStatus.Online((15..45).random().toLong())
                    }
                }
            )

            // Section: Appearance
            SectionHeader(title = "Appearance")

            ThemeSelectorCard(
                currentTheme = themeMode,
                onThemeChange = { themeMode = it }
            )

            // Section: About & Maintenance
            SectionHeader(title = "About & Maintenance")

            AppInfoCard(
                onResetDefaults = { showResetDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showResetDialog) {
        ResetSettingsDialog(
            onConfirm = {
                showResetDialog = false
                espHost = "desky-relay.local"
                espPort = "80"
                espStatus = DeviceConnectionStatus.Idle

                laptopHost = "desky-laptop.local"
                laptopPort = "8080"
                laptopStatus = DeviceConnectionStatus.Idle

                ledHost = "192.168.1.150"
                ledPort = "5577"
                ledStatus = DeviceConnectionStatus.Idle

                themeMode = AppThemeMode.SYSTEM

                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Settings restored to defaults")
                }
            },
            onDismiss = { showResetDialog = false }
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
        SettingsScreen()
    }
}