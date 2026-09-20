package org.example.desky.led_strip.data.datasource

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

private const val DISCOVERY_PORT = 48899
private const val DEFAULT_TIMEOUT_MS = 2000

/**
 * Discovers Magic Home (Flux LED / HF-A11) devices on the local LAN using UDP broadcast on port 48899.
 */
class MagicHomeDiscoveryDataSource(
    private val context: Context
) {
    /**
     * Broadcasts discovery packet and listens for device responses.
     * Returns list of discovered IP addresses.
     */
    suspend fun discoverDevices(): List<String> = withContext(Dispatchers.IO) {
        val discoveredIps = mutableListOf<String>()
        val payload = "HF-A11ASSISTHREAD".toByteArray(Charsets.US_ASCII)

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("DeskyLedDiscovery")?.apply {
            setReferenceCounted(true)
            acquire()
        }

        try {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = 250

                val broadcastAddr = InetAddress.getByName("255.255.255.255")
                val sendPacket = DatagramPacket(payload, payload.size, broadcastAddr, DISCOVERY_PORT)

                val buffer = ByteArray(1024)
                val receivePacket = DatagramPacket(buffer, buffer.size)

                var packetsSent = 0
                val maxBurst = 3
                var lastSentTime = 0L

                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < DEFAULT_TIMEOUT_MS) {
                    val now = System.currentTimeMillis()
                    // Send up to 3 burst packets spaced 150ms apart to prevent UDP packet loss
                    if (packetsSent < maxBurst && (now - lastSentTime >= 150L)) {
                        try {
                            socket.send(sendPacket)
                            packetsSent++
                            lastSentTime = now
                        } catch (_: Exception) {
                        }
                    }

                    try {
                        socket.receive(receivePacket)
                        val hostAddress = receivePacket.address.hostAddress
                        if (hostAddress != null && !hostAddress.startsWith("127.")) {
                            discoveredIps.add(hostAddress)
                            // Break early on first discovered controller for instant connection
                            break
                        }
                    } catch (_: SocketTimeoutException) {
                        // Expected periodic timeout, continue until overall timeoutMs expires
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (multicastLock?.isHeld == true) {
                    multicastLock.release()
                }
            } catch (_: Exception) {
            }
        }

        discoveredIps.distinct()
    }
}
