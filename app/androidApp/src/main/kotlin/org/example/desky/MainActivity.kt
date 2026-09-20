package org.example.desky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.desky.led_strip.presentation.LedScreen
import org.example.desky.core.theme.DeskyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DeskyTheme {
//                MainScreen()
//                SettingsScreen()
                LedScreen(
                    onBackClick = {},
                    onNavigateToTab = {}
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}