package org.example.desky.led_strip.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.desky.core.components.DeviceConnectionStatus
import org.example.desky.led_strip.data.datasource.LedPresetDataStore
import org.example.desky.led_strip.data.datasource.MagicHomeDiscoveryDataSource
import org.example.desky.led_strip.data.datasource.MagicHomeSocketDataSource
import org.example.desky.led_strip.domain.model.LedDeviceState
import org.example.desky.led_strip.domain.repository.LedControllerRepository

class LedControllerRepositoryImpl(
    private val socketDataSource: MagicHomeSocketDataSource,
    private val discoveryDataSource: MagicHomeDiscoveryDataSource,
    private val dataStore: LedPresetDataStore
) : LedControllerRepository {

    private val _connectionStatus = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Idle)
    override val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    private var currentIp: String = LedPresetDataStore.DEFAULT_IP
    private val connectMutex = Mutex()

    override suspend fun connect(
        forceRediscovery: Boolean
    ): Result<LedDeviceState> = connectMutex.withLock {
        _connectionStatus.value = DeviceConnectionStatus.Pinging
        val cachedIp = dataStore.controllerIpFlow.first()
        currentIp = cachedIp

        // 1. Try cached IP first (unless force rediscovery requested)
        if (!forceRediscovery) {
            val startMs = System.currentTimeMillis()
            try {
                val state = socketDataSource.queryStatus(cachedIp)
                if (state != null) {
                    val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                    _connectionStatus.value = DeviceConnectionStatus.Online(latency)
                    return Result.success(state)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Connection to cached IP failed, fall through to auto-discovery
            }
        }

        // 2. Auto-discovery fallback via UDP broadcast
        try {
            val discovered = discoveryDataSource.discoverDevices()
            val newIp = discovered.firstOrNull()

            if (newIp != null) {
                currentIp = newIp
                dataStore.saveControllerIp(newIp)

                val startMs = System.currentTimeMillis()
                val state = socketDataSource.queryStatus(newIp)
                if (state != null) {
                    val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                    _connectionStatus.value = DeviceConnectionStatus.Online(latency)
                    return Result.success(state)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _connectionStatus.value = DeviceConnectionStatus.Offline(e.message ?: "Discovery failed")
            return Result.failure(e)
        }

        _connectionStatus.value = DeviceConnectionStatus.Offline("Controller not found")
        return Result.failure(Exception("Controller not found on network"))
    }

    override suspend fun setPower(
        isOn: Boolean
    ): Result<Unit> {
        val startMs = System.currentTimeMillis()
        return try {
            if (isOn) {
                socketDataSource.turnOn(currentIp)
            } else {
                socketDataSource.turnOff(currentIp)
            }
            val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
            _connectionStatus.value = DeviceConnectionStatus.Online(latency)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Connection lost")
            Result.failure(e)
        }
    }

    override suspend fun setRgb(r: Int, g: Int, b: Int): Result<Unit> {
        val startMs = System.currentTimeMillis()
        return try {
            socketDataSource.setRgb(currentIp, r, g, b)
            val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
            _connectionStatus.value = DeviceConnectionStatus.Online(latency)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Connection lost")
            Result.failure(e)
        }
    }

    override suspend fun queryStatus(): Result<LedDeviceState> {
        return try {
            val state = socketDataSource.queryStatus(currentIp)
                ?: throw IllegalStateException("No response from controller")
            Result.success(state)
        } catch (e: CancellationException) {
            throw e
        }catch (e: Exception) {
            Result.failure(e)
        }
    }
}
