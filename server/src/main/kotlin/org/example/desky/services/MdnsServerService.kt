package org.example.desky.services

import org.example.desky.network.LaptopDiscovery
import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

object MdnsServerService {
    private val logger = LoggerFactory.getLogger(MdnsServerService::class.java)
    private var jmdns: JmDNS? = null
    private var serviceInfo: ServiceInfo? = null

    fun start(
        port: Int = 8080,
        serviceName: String = LaptopDiscovery.DEFAULT_SERVICE_NAME
    ) {
        if (jmdns != null) return

        try {
            val lanAddress = getLanAddress()
            val hostname = lanAddress.hostName.ifBlank { serviceName }
            val mdns = JmDNS.create(lanAddress, hostname)
            jmdns = mdns

            val info = ServiceInfo.create(
                LaptopDiscovery.SERVICE_TYPE_DNS_SD,
                serviceName,
                port,
                0,
                0,
                mapOf("version" to "1.0", "device" to "laptop")
            )
            serviceInfo = info
            mdns.registerService(info)
            logger.info("mDNS Service registered: {} on {}:{}", info.qualifiedName, lanAddress.hostAddress, port)
        } catch (e: Exception) {
            logger.error("Failed to start mDNS service: {}", e.message, e)
        }
    }

    fun stop() {
        try {
            serviceInfo?.let { jmdns?.unregisterService(it) }
            jmdns?.close()
        } catch (e: Exception) {
            logger.error("Error closing mDNS service: {}", e.message, e)
        } finally {
            jmdns = null
            serviceInfo = null
        }
    }

    private fun getLanAddress(): InetAddress {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            val nonVirtual = interfaces.filter { it.isUp && !it.isLoopback && !it.isVirtual }

            val candidate = nonVirtual.flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull {
                    !it.isLoopbackAddress &&
                    !it.isLinkLocalAddress &&
                    (it.hostAddress.startsWith("192.168.") || it.hostAddress.startsWith("10.")) &&
                    !it.hostAddress.startsWith("192.168.56.")
                }

            if (candidate != null) return candidate
        } catch (_: Exception) {
        }
        return InetAddress.getLocalHost()
    }
}
