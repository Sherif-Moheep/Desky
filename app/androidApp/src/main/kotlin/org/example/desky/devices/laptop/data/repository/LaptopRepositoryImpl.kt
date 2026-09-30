package org.example.desky.devices.laptop.data.repository

import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.laptop.data.datasource.LaptopRemoteDataSource
import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.model.LaptopTelemetryDto

class LaptopRepositoryImpl(
    private val remoteDataSource: LaptopRemoteDataSource
) : LaptopRepository {

    private val _connectionStatus = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Idle)
    override val connectionStatus: StateFlow<DeviceConnectionStatus> = _connectionStatus.asStateFlow()

    override suspend fun checkStatus(): DeviceConnectionStatus {
        _connectionStatus.value = DeviceConnectionStatus.Pinging
        val startMs = System.currentTimeMillis()
        return try {
            val response = remoteDataSource.ping()
            if (response.status.isSuccess()) {
                val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(1)
                val status = DeviceConnectionStatus.Online(latency)
                _connectionStatus.value = status
                status
            } else {
                val status = DeviceConnectionStatus.Offline("HTTP ${response.status.value}")
                _connectionStatus.value = status
                status
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            val status = DeviceConnectionStatus.Offline(e.message ?: "Unreachable")
            _connectionStatus.value = status
            status
        }
    }

    override suspend fun getTelemetry(): Result<LaptopTelemetryDto> {
        return try {
            val telemetry = remoteDataSource.getTelemetry()
            Result.success(telemetry)
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun lock(): Result<Unit> {
        return try {
            val response = remoteDataSource.lock()
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lock failed with status ${response.status}"))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sleep(): Result<Unit> {
        return try {
            val response = remoteDataSource.sleep()
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sleep failed with status ${response.status}"))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restart(): Result<Unit> {
        return try {
            val response = remoteDataSource.restart()
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Restart failed with status ${response.status}"))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun shutdown(): Result<Unit> {
        return try {
            val response = remoteDataSource.shutdown()
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Shutdown failed with status ${response.status}"))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
