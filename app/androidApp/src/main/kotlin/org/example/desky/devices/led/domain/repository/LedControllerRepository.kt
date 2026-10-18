package org.example.desky.devices.led.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.devices.led.domain.model.LedDeviceState

interface LedControllerRepository {
    val connectionStatus: StateFlow<DeviceConnectionStatus>
    val deviceState: StateFlow<LedDeviceState?>
    suspend fun connect(forceRediscovery: Boolean = false): Result<LedDeviceState>
    suspend fun setPower(isOn: Boolean): Result<Unit>
    suspend fun setRgb(r: Int, g: Int, b: Int): Result<Unit>
    suspend fun queryStatus(): Result<LedDeviceState>
}
