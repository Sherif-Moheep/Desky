package org.example.desky.core.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeskSnapshot
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceType

interface DeskStateRepository {
    val deskState: StateFlow<DeskSnapshot>
    suspend fun refreshAll(): DeskSnapshot
    suspend fun pingDevice(device: DeviceType): DeviceConnectionStatus
    suspend fun powerOnDesk(): Result<Unit>
    suspend fun powerOffDesk(): Result<Unit>
    suspend fun setLedPower(isOn: Boolean): Result<Unit>
    suspend fun shutdownLaptop(): Result<Unit>
}
