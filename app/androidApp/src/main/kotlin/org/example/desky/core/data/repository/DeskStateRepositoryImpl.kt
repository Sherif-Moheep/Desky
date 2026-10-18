package org.example.desky.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.example.desky.core.domain.model.DeskSnapshot
import org.example.desky.core.domain.model.DeviceConnectionStatus
import org.example.desky.core.domain.model.DeviceType
import org.example.desky.core.domain.model.PowerState
import org.example.desky.core.domain.repository.DeskStateRepository
import org.example.desky.devices.esp.domain.repository.EspRelayRepository
import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.devices.led.domain.repository.LedControllerRepository
import org.example.desky.devices.led.domain.repository.LedPresetRepository
import kotlin.time.Duration.Companion.milliseconds

class DeskStateRepositoryImpl(
    private val laptopRepository: LaptopRepository,
    private val espRelayRepository: EspRelayRepository,
    private val ledControllerRepository: LedControllerRepository,
    ledPresetRepository: LedPresetRepository
) : DeskStateRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val transitionState = MutableStateFlow<PowerState?>(null)

    override val deskState: StateFlow<DeskSnapshot> = combine(
        combine(laptopRepository.connectionStatus, laptopRepository.telemetry, ::Pair),
        combine(espRelayRepository.connectionStatus, espRelayRepository.relayStatus, ::Pair),
        combine(ledControllerRepository.connectionStatus, ledControllerRepository.deviceState, ::Pair),
        ledPresetRepository.getLastBrightness(),
        transitionState
    ) { laptop, esp, led, ledBrightness: Float, transition: PowerState? ->
        val (laptopStatus, laptopTelemetry) = laptop
        val (espStatus, espRelayStatus) = esp
        val (ledStatus, ledDeviceState) = led
        DeskSnapshot(
            laptopStatus = laptopStatus,
            laptopTelemetry = laptopTelemetry,
            espStatus = espStatus,
            espRelayStatus = espRelayStatus,
            ledStatus = ledStatus,
            ledDeviceState = ledDeviceState,
            ledBrightness = ledBrightness,
            transitionState = transition
        )
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = DeskSnapshot()
    )

    override suspend fun refreshAll(): DeskSnapshot = coroutineScope {
        val laptopDef = async {
            val status = laptopRepository.checkStatus()
            if (status is DeviceConnectionStatus.Online) {
                laptopRepository.getTelemetry()
            }
            status
        }
        val espDef = async {
            val status = espRelayRepository.checkStatus()
            if (status is DeviceConnectionStatus.Online) {
                espRelayRepository.getStatus()
            }
            status
        }
        val ledDef = async {
            ledControllerRepository.connect(forceRediscovery = false)
        }

        laptopDef.await()
        espDef.await()
        ledDef.await()

        deskState.value
    }

    override suspend fun pingDevice(device: DeviceType): DeviceConnectionStatus = when (device) {
        DeviceType.ESP_RELAY -> espRelayRepository.checkStatus()
        DeviceType.LAPTOP -> laptopRepository.checkStatus()
        DeviceType.LED_STRIP -> {
            val result = ledControllerRepository.connect(forceRediscovery = false)
            if (result.isSuccess) {
                ledControllerRepository.connectionStatus.value
            } else {
                DeviceConnectionStatus.Offline(result.exceptionOrNull()?.message ?: "Unreachable")
            }
        }
    }

    override suspend fun powerOnDesk(): Result<Unit> {
        transitionState.value = PowerState.BOOTING
        return try {
            val relayResult = espRelayRepository.powerOn()
            ledControllerRepository.setPower(true)
            if (relayResult.isFailure) {
                relayResult
            } else {
                var attempts = 0
                while (attempts < 15) {
                    delay(2000.milliseconds)
                    val status = laptopRepository.checkStatus()
                    if (status is DeviceConnectionStatus.Online) {
                        laptopRepository.getTelemetry()
                        break
                    }
                    attempts++
                }
                Result.success(Unit)
            }
        } finally {
            transitionState.value = null
        }
    }

    override suspend fun powerOffDesk(): Result<Unit> {
        transitionState.value = PowerState.SHUTTING_DOWN
        return try {
            laptopRepository.shutdown()
            espRelayRepository.powerOff()
            ledControllerRepository.setPower(false)
            Result.success(Unit)
        } finally {
            transitionState.value = null
        }
    }

    override suspend fun setLedPower(isOn: Boolean): Result<Unit> {
        return ledControllerRepository.setPower(isOn)
    }

    override suspend fun shutdownLaptop(): Result<Unit> {
        transitionState.value = PowerState.SHUTTING_DOWN
        return try {
            val result = laptopRepository.shutdown()
            espRelayRepository.powerOff()
            result
        } finally {
            transitionState.value = null
        }
    }
}
