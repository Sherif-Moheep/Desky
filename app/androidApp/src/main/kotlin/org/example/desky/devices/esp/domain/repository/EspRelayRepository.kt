package org.example.desky.devices.esp.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceEndpoint
import org.example.desky.devices.esp.domain.model.EspRelayStatus

interface EspRelayRepository {
    val connectionStatus: StateFlow<DeviceConnectionStatus>
    val relayStatus: StateFlow<EspRelayStatus?>
    suspend fun checkStatus(): DeviceConnectionStatus
    suspend fun discover(): Result<DeviceEndpoint>
    suspend fun getStatus(): Result<EspRelayStatus>
    suspend fun powerOn(): Result<Unit>
    suspend fun powerOff(): Result<Unit>
}
