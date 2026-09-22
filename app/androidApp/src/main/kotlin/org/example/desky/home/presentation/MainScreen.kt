package org.example.desky.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.home.presentation.components.DeskPowerButton
import org.example.desky.home.presentation.components.HomeGreeting
import org.example.desky.home.presentation.components.HomeTelemetryCard
import org.example.desky.home.presentation.components.HomeTelemetryUiState
import org.example.desky.home.presentation.components.LaptopTelemetry
import org.example.desky.home.presentation.components.LightTelemetry
import org.example.desky.home.presentation.components.MainTopBar
import org.example.desky.home.presentation.components.PowerOffConfirmationDialog
import org.example.desky.home.presentation.components.RelayTelemetry
import org.example.desky.navigation.BottomNavBar
import org.example.desky.navigation.Route
import org.example.desky.core.presentation.theme.DeskyTheme

@PreviewLightDark
@Composable
fun MainScreen() {
//    Box(modifier = Modifier.fillMaxSize()) {
//        TopographicPattern()
//    }
    var isDeskOn by remember { mutableStateOf(true) }
    var showPowerOffDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val telemetryState = remember(isDeskOn) {
        if (isDeskOn) {
            HomeTelemetryUiState()
        } else {
            HomeTelemetryUiState(
                laptop = LaptopTelemetry(isOnline = false),
                light = LightTelemetry(isOn = false),
                relay = RelayTelemetry(isAcActive = false)
            )
        }
    }

    DeskyTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    selectedKey = Route.Home,
                    onSelectedKey = {}
                )
            },
            topBar = {
                MainTopBar()
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                HomeGreeting(isDeskOn = isDeskOn)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DeskPowerButton(
                        isDeskOn = isDeskOn,
                        onToggle = {
                            if (isDeskOn) {
                                showPowerOffDialog = true
                            } else {
                                isDeskOn = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                HomeTelemetryCard(
                    state = telemetryState
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showPowerOffDialog) {
                PowerOffConfirmationDialog(
                    onConfirm = {
                        isDeskOn = false
                        showPowerOffDialog = false
                    },
                    onDismiss = {
                        showPowerOffDialog = false
                    }
                )
            }
        }
    }
}