package org.example.desky.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PowerService {

    private const val powerInterval = 5
    suspend fun restart(): Boolean  = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder(
                "shutdown.exe",
                "/r",
                "/t", "$powerInterval",
                "/c", "Restart initiated from Desky app"
            ).start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun shutdown(): Boolean = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder(
                "shutdown.exe",
                "/s",
                "/t", "$powerInterval",
                "/c", "Shutdown initiated from Desky app"
            ).start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}