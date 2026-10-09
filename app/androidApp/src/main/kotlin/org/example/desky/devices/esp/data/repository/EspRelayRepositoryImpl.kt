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

    override suspend fun checkStatus(): DeviceConnectionStatus {
        _connectionStatus.value = DeviceConnectionStatus.Pinging

        // 1. Try pinging current saved host/port
        pingCurrentEndpoint()?.let { online ->
            _connectionStatus.value = online
            return online
        }

        // 2. Fallback to mDNS auto-discovery if configured host failed
        if (discover().isSuccess) {
            pingCurrentEndpoint()?.let { online ->
                _connectionStatus.value = online
                return online
            }
        }

        val offlineStatus = DeviceConnectionStatus.Offline("Unreachable")
        _connectionStatus.value = offlineStatus
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
            val status = remoteDataSource.getStatus()
            Result.success(status.toDomain())
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun powerOn(): Result<Unit> = executeCommand("Power ON") { remoteDataSource.powerOn() }

    override suspend fun powerOff(): Result<Unit> = executeCommand("Power OFF") { remoteDataSource.powerOff() }

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
