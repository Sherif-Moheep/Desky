package org.example.desky.devices.laptop.data.datasource

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.network.LaptopDiscovery
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

@Suppress("DEPRECATION")
class LaptopDiscoveryDataSource(
    private val context: Context
) {
    private val nsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    }

    suspend fun discoverLaptop(
        timeoutMs: Long = 3500L
    ): DeviceEndpoint? = withContext(Dispatchers.IO) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val multicastLock = wifiManager?.createMulticastLock("DeskyLaptopDiscovery")?.apply {
            setReferenceCounted(false)
        }

        var discoveryListenerRef: NsdManager.DiscoveryListener? = null
        val isDiscoveryStarted = AtomicBoolean(false)

        try {
            multicastLock?.acquire()
            withTimeoutOrNull(timeoutMs.milliseconds) {
                suspendCancellableCoroutine<DeviceEndpoint?> { continuation ->
                    val isResolved = AtomicBoolean(false)

                    val resolveListener = object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            // Keep listening if individual service resolution fails
                        }

                        override fun onServiceResolved(
                            serviceInfo: NsdServiceInfo?
                        ) {
                            if (isResolved.compareAndSet(false, true)) {
                                val host = serviceInfo?.host?.hostAddress
                                val port = serviceInfo?.port ?: 8080
                                if (!host.isNullOrBlank() && !host.startsWith("127.")) {
                                    continuation.resume(DeviceEndpoint(host, port))
                                } else {
                                    continuation.resume(null)
                                }
                            }
                        }
                    }

                    val listener = object : NsdManager.DiscoveryListener {
                        override fun onStartDiscoveryFailed(
                            serviceType: String?,
                            errorCode: Int
                        ) {
                            if (isResolved.compareAndSet(false, true)) {
                                continuation.resume(null)
                            }
                        }

                        override fun onStopDiscoveryFailed(
                            serviceType: String?,
                            errorCode: Int
                        ) {}

                        override fun onDiscoveryStarted(
                            regType: String?
                        ) {
                            isDiscoveryStarted.set(true)
                        }

                        override fun onDiscoveryStopped(
                            serviceType: String?
                        ) {
                            isDiscoveryStarted.set(false)
                        }

                        override fun onServiceFound(
                            serviceInfo: NsdServiceInfo?
                        ) {
                            val type = serviceInfo?.serviceType ?: ""
                            if (type.contains("desky", ignoreCase = true) && !isResolved.get()) {
                                try {
                                    nsdManager?.resolveService(serviceInfo, resolveListener)
                                } catch (_: Exception) {
                                }
                            }
                        }

                        override fun onServiceLost(
                            serviceInfo: NsdServiceInfo?
                        ) {}
                    }

                    discoveryListenerRef = listener

                    try {
                        nsdManager?.discoverServices(
                            LaptopDiscovery.SERVICE_TYPE,
                            NsdManager.PROTOCOL_DNS_SD,
                            listener
                        )
                    } catch (_: Exception) {
                        if (isResolved.compareAndSet(false, true)) {
                            continuation.resume(null)
                        }
                    }
                }
            }
        } finally {
            try {
                if (isDiscoveryStarted.get() && discoveryListenerRef != null) {
                    nsdManager?.stopServiceDiscovery(discoveryListenerRef)
                }
            } catch (_: Exception) {
            }
            try {
                if (multicastLock?.isHeld == true) {
                    multicastLock.release()
                }
            } catch (_: Exception) {
            }
        }
    }
}
