package org.example.desky.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO

object SystemTrayService {
    private val logger = LoggerFactory.getLogger(SystemTrayService::class.java)
    private var trayIcon: TrayIcon? = null

    fun init(
        port: Int,
        onExit: () -> Unit
    ) {
        if (!SystemTray.isSupported() || GraphicsEnvironment.isHeadless()) {
            logger.info("SystemTray is not supported or environment is headless; skipping tray icon.")
            return
        }

        try {
            val systemTray = SystemTray.getSystemTray()
            val image = loadOrCreateImage()

            val popup = PopupMenu().apply {
                val browseItem = MenuItem("Open in Browser").apply {
                    addActionListener {
                        try {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(URI("http://localhost:$port/telemetry"))
                            }
                        } catch (e: Exception) {
                            logger.warn("Failed to open browser: {}", e.message)
                        }
                    }
                }
                val turnOnLedItem = MenuItem("Turn On LED Strip").apply {
                    addActionListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            LedService.turnOn()
                        }
                    }
                }
                val exitItem = MenuItem("Exit").apply {
                    addActionListener {
                        logger.info("Exit requested via System Tray.")
                        onExit()
                    }
                }

                add(browseItem)
                add(turnOnLedItem)
                addSeparator()
                add(exitItem)
            }

            val icon = TrayIcon(
                image,
                "Desky Server (Port $port)",
                popup
            ).apply {
                isImageAutoSize = true
            }

            systemTray.add(icon)
            trayIcon = icon
            logger.info("System Tray icon registered successfully.")

            try {
                icon.displayMessage(
                    "Desky Server",
                    "Running on port $port",
                    TrayIcon.MessageType.INFO
                )
            } catch (_: Exception) { }
        } catch (e: Exception) {
            logger.error("Failed to initialize system tray: {}", e.message, e)
        }
    }

    fun remove() {
        try {
            trayIcon?.let {
                if (SystemTray.isSupported()) {
                    SystemTray.getSystemTray().remove(it)
                }
            }
        } catch (e: Exception) {
            logger.warn("Error removing tray icon: {}", e.message)
        } finally {
            trayIcon = null
        }
    }

    private fun loadOrCreateImage(): Image {
        // 1. Try loading custom icon from resources (for when you add an icon later)
        val resourceStream = javaClass.getResourceAsStream("/icon.png")
            ?: javaClass.getResourceAsStream("/tray-icon.png")
        if (resourceStream != null) {
            try {
                val loaded = ImageIO.read(resourceStream)
                if (loaded != null) return loaded
            } catch (_: Exception) {
            }
        }

        // 2. Red rounded corners box with white letter 'D'
        val size = 32
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Red rounded corners box
        g.color = Color(0xDC, 0x26, 0x26) // Vibrant red (#DC2626)
        g.fillRoundRect(1, 1, size - 2, size - 2, 8, 8)

        // Bold white 'D' for Desky
        g.color = Color.WHITE
        g.font = Font("Segoe UI", Font.BOLD, 20)
        val metrics = g.fontMetrics
        val text = "D"
        val x = (size - metrics.stringWidth(text)) / 2
        val y = ((size - metrics.height) / 2) + metrics.ascent
        g.drawString(text, x, y)

        g.dispose()
        return img
    }
}
