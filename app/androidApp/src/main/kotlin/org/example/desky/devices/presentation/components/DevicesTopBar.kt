package org.example.desky.devices.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun DevicesTopBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "Devices",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    )
}

@PreviewLightDark
@Composable
fun PreviewDevicesTopBar() {
    DeskyTheme {
        DevicesTopBar()
    }
}
