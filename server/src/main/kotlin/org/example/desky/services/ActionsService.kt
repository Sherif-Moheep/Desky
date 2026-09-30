package org.example.desky.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ActionsService {
    suspend fun lock(): Boolean = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder("rundll32.exe", "user32.dll,LockWorkStation").start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sleep(): Boolean = withContext(Dispatchers.IO) {
        try {
            val sleepScript = "Add-Type -AssemblyName System.Windows.Forms; " +
                    "[System.Windows.Forms.Application]::SetSuspendState([System.Windows.Forms.PowerState]::Suspend, \$false, \$false)"
            ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                sleepScript
            ).start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}