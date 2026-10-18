package org.example.desky.devices.laptop.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.model.LaptopTelemetryDto

interface LaptopRepository {
    val connectionStatus: StateFlow<DeviceConnectionStatus>
    val telemetry: StateFlow<LaptopTelemetryDto?>
    suspend fun checkStatus(): DeviceConnectionStatus
    suspend fun discover(): Result<DeviceEndpoint>
    suspend fun getTelemetry(): Result<LaptopTelemetryDto>
    suspend fun lock(): Result<Unit>
    suspend fun sleep(): Result<Unit>
    suspend fun restart(): Result<Unit>
    suspend fun shutdown(): Result<Unit>
}
