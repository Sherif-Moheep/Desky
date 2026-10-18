package org.example.desky.devices.led.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.led.data.datasource.MagicHomeDiscoveryDataSource
import org.example.desky.devices.led.data.datasource.MagicHomeSocketDataSource
import org.example.desky.devices.led.domain.model.LedDeviceState
import org.example.desky.devices.led.domain.repository.LedControllerRepository

class LedControllerRepositoryImpl(
    private val socketDataSource: MagicHomeSocketDataSource,
    private val discoveryDataSource: MagicHomeDiscoveryDataSource,
    private val settingsRepository: SettingsRepository
) : LedControllerRepository {

    private val _connectionStatus = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Idle)
    override val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    private val _deviceState = MutableStateFlow<LedDeviceState?>(null)
    override val deviceState: StateFlow<LedDeviceState?> = _deviceState.asStateFlow()

    private var currentIp: String = ""
    private val connectMutex = Mutex()

    private suspend fun resolveIp(): String {
        if (currentIp.isBlank()) {
            currentIp = settingsRepository.ledEndpointFlow.first().host
        }
        return currentIp
    }

    override suspend fun connect(
        forceRediscovery: Boolean
    ): Result<LedDeviceState> = connectMutex.withLock {
        _connectionStatus.value = DeviceConnectionStatus.Pinging
        val configuredEndpoint = settingsRepository.ledEndpointFlow.first()
        val targetIp = configuredEndpoint.host
        currentIp = targetIp

        // 1. Try configured IP from Settings first (unless force rediscovery requested)
        if (!forceRediscovery && targetIp.isNotBlank()) {
            val startMs = System.currentTimeMillis()
            try {
                val state = socketDataSource.queryStatus(targetIp)
                if (state != null) {
                    val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                    _connectionStatus.value = DeviceConnectionStatus.Online(latency)
                    _deviceState.value = state
                    return Result.success(state)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Connection to configured IP failed, fall through to auto-discovery
            }
        }

        // 2. Auto-discovery fallback via UDP broadcast
        try {
            val discovered = discoveryDataSource.discoverDevices()
            val newIp = discovered.firstOrNull()

            if (newIp != null) {
                currentIp = newIp
                settingsRepository.updateLedEndpoint(newIp, configuredEndpoint.port)

                val startMs = System.currentTimeMillis()
                val state = socketDataSource.queryStatus(newIp)
                if (state != null) {
                    val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                    _connectionStatus.value = DeviceConnectionStatus.Online(latency)
                    _deviceState.value = state
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
            val ip = resolveIp()
            if (isOn) {
                socketDataSource.turnOn(ip)
            } else {
                socketDataSource.turnOff(ip)
            }
            val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
            _connectionStatus.value = DeviceConnectionStatus.Online(latency)
            _deviceState.update { it?.copy(isOn = isOn) ?: LedDeviceState(
                isOn = isOn,
                red = 255,
                green = 57,
                blue = 53
            ) }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Connection lost")
            Result.failure(e)
        }
    }

    override suspend fun setRgb(
        r: Int,
        g: Int,
        b: Int
    ): Result<Unit> {
        val startMs = System.currentTimeMillis()
        return try {
            val ip = resolveIp()
            socketDataSource.setRgb(ip, r, g, b)
            val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
            _connectionStatus.value = DeviceConnectionStatus.Online(latency)
            _deviceState.update { it?.copy(red = r, green = g, blue = b) ?: LedDeviceState(
                isOn = true,
                red = r,
                green = g,
                blue = b
            ) }
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
            val ip = resolveIp()
            val state = socketDataSource.queryStatus(ip)
                ?: throw IllegalStateException("No response from controller")
            _deviceState.value = state
            Result.success(state)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
