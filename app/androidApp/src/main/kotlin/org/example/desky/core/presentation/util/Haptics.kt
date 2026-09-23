package org.example.desky.core.presentation.util

import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Delivers a crisp, mechanical switch vibration pulse.
 * Uses modern USAGE_HARDWARE_FEEDBACK so it triggers even when
 * general touch interaction haptics are disabled in phone settings.
 */
fun Vibrator?.playTactileClick() {
    if (this == null) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val effect = VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
        val attributes = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
            .build()
        vibrate(effect, attributes)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrate(effect)
    }
}

/**
 * Convenience Composable to obtain and remember a platform Vibrator instance.
 */
@Composable
fun rememberVibrator(): Vibrator? {
    val context = LocalContext.current
    return remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator ?: context.getSystemService(Vibrator::class.java)
        } else {
            context.getSystemService(Vibrator::class.java)
        }
    }
}
