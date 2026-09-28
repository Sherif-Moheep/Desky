package org.example.desky.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceEndpoint
import java.net.InetSocketAddress
import java.net.Socket

suspend fun checkSocketConnection(
    host: String,
    port: Int,
    timeoutMs: Int = 2000
): DeviceConnectionStatus {
    if (host.isBlank()) return DeviceConnectionStatus.Offline("Host empty")
    return withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
            }
            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            DeviceConnectionStatus.Online(latency)
        } catch (e: Exception) {
            DeviceConnectionStatus.Offline(e.localizedMessage?.take(25) ?: "Unreachable")
        }
    }
}


suspend fun checkSocketConnection(
    endpoint: DeviceEndpoint,
    timeoutMs: Int = 2000
): DeviceConnectionStatus = checkSocketConnection(endpoint.host, endpoint.port, timeoutMs)
