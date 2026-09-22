package org.example.desky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.desky.core.domain.model.AppThemeMode
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.presentation.theme.DeskyTheme
import org.example.desky.led_strip.presentation.LedScreen
import org.example.desky.settings.SettingsScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by settingsRepository.themeModeFlow.collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            DeskyTheme(darkTheme = isDarkTheme) {
//                MainScreen()
                SettingsScreen()
//                LedScreen(
//                    onBackClick = {},
//                    onNavigateToTab = {}
//                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}