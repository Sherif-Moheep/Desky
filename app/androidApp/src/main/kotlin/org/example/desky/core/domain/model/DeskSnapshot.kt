package org.example.desky.core.domain.model

import org.example.desky.core.util.ColorNameResolver
import org.example.desky.devices.esp.domain.model.EspRelayStatus
import org.example.desky.devices.led.domain.model.LedDeviceState
import org.example.desky.model.LaptopTelemetryDto

data class DeskSnapshot(
    val laptopStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val laptopTelemetry: LaptopTelemetryDto? = null,
    val espStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val espRelayStatus: EspRelayStatus? = null,
    val ledStatus: DeviceConnectionStatus = DeviceConnectionStatus.Idle,
    val ledDeviceState: LedDeviceState? = null,
    val ledBrightness: Float = 0.8f,
    val transitionState: PowerState? = null
) {
    val isAnyDeviceOnline: Boolean
        get() = laptopStatus is DeviceConnectionStatus.Online ||
                espStatus is DeviceConnectionStatus.Online ||
                ledStatus is DeviceConnectionStatus.Online

    val onlineDevicesCount: Int
        get() = (if (laptopStatus is DeviceConnectionStatus.Online) 1 else 0) +
                (if (espStatus is DeviceConnectionStatus.Online) 1 else 0) +
                (if (ledStatus is DeviceConnectionStatus.Online) 1 else 0)

    val totalDevicesCount: Int = 3

    val primaryLatencyMs: Long
        get() = when {
            laptopStatus is DeviceConnectionStatus.Online -> laptopStatus.latencyMs
            espStatus is DeviceConnectionStatus.Online -> espStatus.latencyMs
            ledStatus is DeviceConnectionStatus.Online -> ledStatus.latencyMs
            else -> 0L
        }

    val primaryConnectionStatus: DeviceConnectionStatus
        get() = when {
            laptopStatus is DeviceConnectionStatus.Online -> laptopStatus
            espStatus is DeviceConnectionStatus.Online -> espStatus
            ledStatus is DeviceConnectionStatus.Online -> ledStatus
            else -> DeviceConnectionStatus.Offline("Offline")
        }

    val isAcActive: Boolean
        get() = espRelayStatus?.isOn ?: (laptopStatus is DeviceConnectionStatus.Online)

    val derivedPowerState: PowerState
        get() = when {
            transitionState != null -> transitionState
            laptopStatus is DeviceConnectionStatus.Online -> PowerState.ON
            else -> PowerState.OFF
        }

    val isLedPoweredOn: Boolean
        get() = ledDeviceState?.isOn ?: false

    val ledColorName: String
        get() = if (ledDeviceState != null) {
            ColorNameResolver.resolve(ledDeviceState.red, ledDeviceState.green, ledDeviceState.blue)
        } else "--"
}
