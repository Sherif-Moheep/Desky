package org.example.desky.devices.esp.data.repository

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.devices.esp.data.datasource.EspDiscoveryDataSource
import org.example.desky.devices.esp.data.datasource.EspRemoteDataSource
import org.example.desky.devices.esp.domain.model.EspRelayStatus
import org.example.desky.devices.esp.domain.repository.EspRelayRepository

class EspRelayRepositoryImpl(
    private val remoteDataSource: EspRemoteDataSource,
    private val discoveryDataSource: EspDiscoveryDataSource,
    private val settingsRepository: SettingsRepository
) : EspRelayRepository {

    private val _connectionStatus = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Idle)
    override val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    private val _relayStatus = MutableStateFlow<EspRelayStatus?>(null)
    override val relayStatus: StateFlow<EspRelayStatus?> = _relayStatus.asStateFlow()

    override suspend fun checkStatus(): DeviceConnectionStatus {
        _connectionStatus.value = DeviceConnectionStatus.Pinging

        // 1. Try pinging current saved host/port
        pingCurrentEndpoint()?.let { online ->
            _connectionStatus.value = online
            return online
        }

        // 2. Fallback to mDNS auto-discovery if the configured host failed
        if (discover().isSuccess) {
            pingCurrentEndpoint()?.let { online ->
                _connectionStatus.value = online
                return online
            }
        }

        val offlineStatus = DeviceConnectionStatus.Offline("Unreachable")
        _connectionStatus.value = offlineStatus
        _relayStatus.value = null
        return offlineStatus
    }

    override suspend fun discover(): Result<DeviceEndpoint> {
        val endpoint = discoveryDataSource.discoverEsp()
        return if (endpoint != null) {
            settingsRepository.updateEspEndpoint(endpoint.host, endpoint.port)
            Result.success(endpoint)
        } else {
            Result.failure(Exception("No Desky relay discovered on Wi-Fi"))
        }
    }

    override suspend fun getStatus(): Result<EspRelayStatus> {
        return try {
            val status = remoteDataSource.getStatus().toDomain()
            _relayStatus.value = status
            Result.success(status)
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun powerOn(): Result<Unit> {
        val result = executeCommand("Power ON") { remoteDataSource.powerOn() }
        if (result.isSuccess) {
            _relayStatus.value = _relayStatus.value?.copy(isOn = true)
                ?: EspRelayStatus(
                    isOnline = true,
                    isOn = true,
                    uptimeSeconds = 0L,
                    wifiRssi = 0,
                    ip = ""
                )
        }
        return result
    }

    override suspend fun powerOff(): Result<Unit> {
        val result = executeCommand("Power OFF") { remoteDataSource.powerOff() }
        if (result.isSuccess) {
            _relayStatus.value = _relayStatus.value?.copy(isOn = false)
                ?: EspRelayStatus(
                    isOnline = true,
                    isOn = false,
                    uptimeSeconds = 0L,
                    wifiRssi = 0,
                    ip = ""
                )
        }
        return result
    }

    private suspend fun pingCurrentEndpoint(): DeviceConnectionStatus.Online? {
        val startMs = System.currentTimeMillis()
        return try {
            val response = remoteDataSource.ping()
            if (response.status.isSuccess()) {
                val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                DeviceConnectionStatus.Online(latency)
            } else {
                null
            }
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun executeCommand(
        actionName: String,
        action: suspend () -> HttpResponse
    ): Result<Unit> {
        return try {
            val response = action()
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("$actionName failed with status ${response.status}"))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
