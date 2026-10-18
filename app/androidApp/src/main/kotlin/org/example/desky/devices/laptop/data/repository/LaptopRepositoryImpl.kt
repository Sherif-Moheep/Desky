package org.example.desky.devices.laptop.data.repository

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.core.domain.repository.SettingsRepository
import org.example.desky.devices.laptop.data.datasource.LaptopDiscoveryDataSource
import org.example.desky.devices.laptop.data.datasource.LaptopRemoteDataSource
import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.model.LaptopTelemetryDto

class LaptopRepositoryImpl(
    private val remoteDataSource: LaptopRemoteDataSource,
    private val discoveryDataSource: LaptopDiscoveryDataSource,
    private val settingsRepository: SettingsRepository
) : LaptopRepository {

    private val _connectionStatus = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Idle)
    override val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    private val _telemetry = MutableStateFlow<LaptopTelemetryDto?>(null)
    override val telemetry: StateFlow<LaptopTelemetryDto?> = _telemetry.asStateFlow()

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
        _telemetry.value = null
        return offlineStatus
    }

    override suspend fun discover(): Result<DeviceEndpoint> {
        val endpoint = discoveryDataSource.discoverLaptop()
        return if (endpoint != null) {
            settingsRepository.updateLaptopEndpoint(endpoint.host, endpoint.port)
            Result.success(endpoint)
        } else {
            Result.failure(Exception("No Desky workstation discovered on Wi-Fi"))
        }
    }

    override suspend fun getTelemetry(): Result<LaptopTelemetryDto> {
        return try {
            val telemetry = remoteDataSource.getTelemetry()
            _telemetry.value = telemetry
            Result.success(telemetry)
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun lock(): Result<Unit> {
        val result = executeCommand("Lock") { remoteDataSource.lock() }
        if (result.isSuccess) {
            _telemetry.value = _telemetry.value?.copy(isLocked = true)
        }
        return result
    }

    override suspend fun sleep(): Result<Unit> {
        val result = executeCommand("Sleep") { remoteDataSource.sleep() }
        if (result.isSuccess) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Sleeping")
            _telemetry.value = null
        }
        return result
    }

    override suspend fun restart(): Result<Unit> {
        val result = executeCommand("Restart") { remoteDataSource.restart() }
        if (result.isSuccess) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Restarting")
            _telemetry.value = null
        }
        return result
    }

    override suspend fun shutdown(): Result<Unit> {
        val result = executeCommand("Shutdown") { remoteDataSource.shutdown() }
        if (result.isSuccess) {
            _connectionStatus.value = DeviceConnectionStatus.Offline("Shut down")
            _telemetry.value = null
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
