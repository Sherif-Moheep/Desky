package org.example.desky

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.desky.model.LaptopTelemetryDto

object WindowsTelemetryService {
    private const val POWERSHELL_TELEMETRY_SCRIPT = """
        ${'$'}b = Get-CimInstance Win32_Battery -ErrorAction SilentlyContinue
        ${'$'}os = Get-CimInstance Win32_OperatingSystem
        ${'$'}diff = (Get-Date) - ${'$'}os.LastBootUpTime
        ${'$'}uptime = '{0}h {1}m' -f [int]${'$'}diff.TotalHours, ${'$'}diff.Minutes
        ${'$'}locked = [bool](Get-Process -Name LogonUI -ErrorAction SilentlyContinue)
        ${'$'}percent = if (${'$'}b) { ${'$'}b.EstimatedChargeRemaining } else { 100 }
        ${'$'}ac = if (${'$'}b) { (${'$'}b.BatteryStatus -in 2,3,6,7,8,9) } else { ${'$'}true }
        Write-Output ${'$'}locked
        Write-Output ${'$'}ac
        Write-Output ${'$'}percent
        Write-Output ${'$'}uptime
    """
    suspend fun getTelemetry(): LaptopTelemetryDto = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                POWERSHELL_TELEMETRY_SCRIPT
            ).start()
            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()
            if (lines.size >= 4) {
                LaptopTelemetryDto(
                    isLocked = lines[0].trim().toBoolean(),
                    isAcConnected = lines[1].trim().toBoolean(),
                    batteryPercent = lines[2].trim().toIntOrNull() ?: 100,
                    uptimeFormatted = lines[3].trim()
                )
            } else {
                fallbackTelemetry()
            }
        } catch (_: Exception) {
            fallbackTelemetry()
        }
    }
    private fun fallbackTelemetry() = LaptopTelemetryDto(
        isLocked = false,
        isAcConnected = true,
        batteryPercent = 100,
        uptimeFormatted = "Unknown"
    )
}