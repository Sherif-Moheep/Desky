package org.example.desky.led_strip.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.desky.led_strip.domain.model.LedDeviceState
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

private const val PORT = 5577
private const val TIMEOUT_MS = 2500

/**
 * Low-level TCP socket client for Magic Home (Flux LED / Zengge) Wi-Fi controllers.
 * Sends raw command packets to port 5577 on Dispatchers.IO.
 */
class MagicHomeSocketDataSource {

    suspend fun sendCommand(
        ip: String,
        bytes: ByteArray,
        readResponseLength: Int = 0
    ): ByteArray = withContext(Dispatchers.IO) {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(ip, PORT), TIMEOUT_MS)
            socket.soTimeout = TIMEOUT_MS
            val out: OutputStream = socket.getOutputStream()
            val input: InputStream = socket.getInputStream()

            out.write(bytes)
            out.flush()

            if (readResponseLength > 0) {
                val buffer = ByteArray(readResponseLength)
                var totalBytesRead = 0
                while (totalBytesRead < readResponseLength) {
                    val count = input.read(buffer, totalBytesRead, readResponseLength - totalBytesRead)
                    if (count == -1) break
                    totalBytesRead += count
                }
                buffer.copyOf(totalBytesRead)
            } else {
                ByteArray(0)
            }
        }
    }

    suspend fun turnOn(ip: String): Unit = withContext(Dispatchers.IO) {
        sendCommand(
            ip = ip,
            bytes = byteArrayOf(
                0x71.toByte(),
                0x23.toByte(),
                0x0F.toByte(),
                0xA3.toByte()
            )
        )
    }

    suspend fun turnOff(ip: String): Unit = withContext(Dispatchers.IO) {
        sendCommand(
            ip = ip,
            bytes = byteArrayOf(
                0x71.toByte(),
                0x24.toByte(),
                0x0F.toByte(),
                0xA4.toByte()
            )
        )
    }

    suspend fun setRgb(
        ip: String,
        r: Int,
        g: Int,
        b: Int,
        warmWhite: Int = 0
    ): Unit = withContext(Dispatchers.IO) {
        val payload = intArrayOf(
            0x31,
            r.coerceIn(0, 255),
            g.coerceIn(0, 255),
            b.coerceIn(0, 255),
            warmWhite.coerceIn(0, 255),
            0x00,
            0x0F
        )
        val checksum = (payload.sum() and 0xFF)
        val fullPacket = (payload + checksum).map { it.toByte() }.toByteArray()
        sendCommand(ip = ip, bytes = fullPacket)
    }

    suspend fun queryStatus(ip: String): LedDeviceState? = withContext(Dispatchers.IO) {
        val queryCmd = byteArrayOf(
            0x81.toByte(),
            0x8A.toByte(),
            0x8B.toByte(),
            0x96.toByte()
        )
        val response = sendCommand(
            ip = ip,
            bytes = queryCmd,
            readResponseLength = 14
        )
        if (response.size < 14) return@withContext null

        val isOn = (response[2].toInt() and 0xFF) == 0x23
        val red = response[6].toInt() and 0xFF
        val green = response[7].toInt() and 0xFF
        val blue = response[8].toInt() and 0xFF
        val warmWhite = response[9].toInt() and 0xFF

        LedDeviceState(
            isOn = isOn,
            red = red,
            green = green,
            blue = blue,
            warmWhite = warmWhite
        )
    }
}
