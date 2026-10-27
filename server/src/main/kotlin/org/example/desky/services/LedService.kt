package org.example.desky.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.Properties
import kotlin.time.Duration.Companion.milliseconds

object LedService {
    private val logger = LoggerFactory.getLogger(LedService::class.java)

    const val DEFAULT_PORT = 5577
    const val DISCOVERY_PORT = 48899
    const val DEFAULT_HOST = "192.168.1.150"
    private const val SOCKET_TIMEOUT_MS = 2500

    // Magic Home turn-ON opcode [0x71, 0x23, 0x0F, 0xA3] (preserves current color & brightness)
    private val TURN_ON_PAYLOAD = byteArrayOf(
        0x71.toByte(),
        0x23.toByte(),
        0x0F.toByte(),
        0xA3.toByte()
    )

    private val DISCOVERY_PAYLOAD = "HF-A11ASSISTHREAD".toByteArray(Charsets.US_ASCII)

    @Volatile
    private var cachedIp: String? = null

    private val cacheDir: File by lazy {
        val base = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
            ?: System.getProperty("user.home")
        File(base, "Desky")
    }

    private val cacheFile: File by lazy {
        File(cacheDir, "server-cache.properties")
    }

    private fun loadCachedIp(): String {
        return try {
            if (cacheFile.exists()) {
                val props = Properties()
                cacheFile.inputStream().use { props.load(it) }
                props.getProperty("led_host")?.takeIf { it.isNotBlank() } ?: DEFAULT_HOST
            } else {
                DEFAULT_HOST
            }
        } catch (e: Exception) {
            logger.debug("Could not read disk cache file: {}", e.message)
            DEFAULT_HOST
        }
    }

    private fun updateCachedIp(ip: String) {
        cachedIp = ip
        try {
            cacheDir.mkdirs()
            val props = Properties()
            if (cacheFile.exists()) {
                cacheFile.inputStream().use { props.load(it) }
            }
            props.setProperty("led_host", ip)
            cacheFile.outputStream().use { props.store(it, "Desky Server Cache") }
        } catch (e: Exception) {
            logger.debug("Failed to persist cached LED IP: {}", e.message)
        }
    }

    private fun sendTurnOnPacket(
        host: String,
        port: Int = DEFAULT_PORT
    ): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), SOCKET_TIMEOUT_MS)
                socket.soTimeout = SOCKET_TIMEOUT_MS
                val out: OutputStream = socket.getOutputStream()
                out.write(TURN_ON_PAYLOAD)
                out.flush()
            }
            logger.info("Successfully turned on LED strip at {}:{}", host, port)
            true
        } catch (e: Exception) {
            logger.debug("Could not reach LED strip at {}:{}: {}", host, port, e.message)
            false
        }
    }

    suspend fun turnOn(
        targetHost: String? = null,
        port: Int = DEFAULT_PORT
    ): Boolean = withContext(Dispatchers.IO) {
        targetHost?.let { host ->
            val ok = sendTurnOnPacket(host, port)
            if (ok) updateCachedIp(host)
            return@withContext ok
        }

        val candidateHost = cachedIp ?: loadCachedIp().also { cachedIp = it }
        if (sendTurnOnPacket(candidateHost, port)) {
            updateCachedIp(candidateHost)
            return@withContext true
        }

        logger.info("Direct connection to {} failed; attempting auto-discovery via UDP...", candidateHost)
        val discovered = discoverIp()
        if (discovered != null && discovered != candidateHost) {
            if (sendTurnOnPacket(discovered, port)) {
                updateCachedIp(discovered)
                return@withContext true
            }
        }

        false
    }

    suspend fun discoverIp(
        timeoutMs: Long = 1500L
    ): String? = withContext(Dispatchers.IO) {
        val broadcastAddresses = getBroadcastAddresses()
        try {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = 250

                val buffer = ByteArray(1024)
                val receivePacket = DatagramPacket(buffer, buffer.size)

                var packetsSent = 0
                val maxBurst = 3
                var lastSentTime = 0L
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    val now = System.currentTimeMillis()
                    if (packetsSent < maxBurst && (now - lastSentTime >= 150L)) {
                        for (bAddr in broadcastAddresses) {
                            try {
                                val sendPacket = DatagramPacket(
                                    DISCOVERY_PAYLOAD,
                                    DISCOVERY_PAYLOAD.size,
                                    bAddr,
                                    DISCOVERY_PORT
                                )
                                socket.send(sendPacket)
                            } catch (_: Exception) {
                            }
                        }
                        packetsSent++
                        lastSentTime = now
                    }

                    try {
                        socket.receive(receivePacket)
                        val hostAddress = receivePacket.address?.hostAddress
                        if (hostAddress != null && !hostAddress.startsWith("127.")) {
                            logger.info("Discovered LED controller at {}", hostAddress)
                            return@withContext hostAddress
                        }
                    } catch (_: SocketTimeoutException) {
                    }
                }
            }
        } catch (e: Exception) {
            logger.debug("LED auto-discovery encountered an error: {}", e.message)
        }
        null
    }

    fun turnOnOnStartup(
        maxAttempts: Int = 5,
        retryDelayMs: Long = 2000L
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("Initiating LED strip startup sequence...")
            var attempt = 1
            while (attempt <= maxAttempts) {
                logger.info("Attempting to turn on LED strip (attempt {}/{})", attempt, maxAttempts)

                val success = turnOn()
                if (success) {
                    logger.info("LED strip turned on successfully on startup.")
                    return@launch
                }

                if (attempt < maxAttempts) {
                    delay(retryDelayMs.milliseconds)
                }
                attempt++
            }
            logger.warn("Could not turn on LED strip on startup after {} attempts.", maxAttempts)
        }
    }

    private fun getBroadcastAddresses(): List<InetAddress> {
        val list = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
                for (addr in iface.interfaceAddresses) {
                    val broadcast = addr.broadcast
                    if (broadcast is Inet4Address && !broadcast.isLoopbackAddress) {
                        list.add(broadcast)
                    }
                }
            }
        } catch (_: Exception) {
        }

        if (list.isEmpty()) {
            try {
                list.add(InetAddress.getByName("255.255.255.255"))
            } catch (_: Exception) {
            }
        }
        return list.distinct()
    }
}
