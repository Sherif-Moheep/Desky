package org.example.desky.devices.led_strip.domain.usecase

import org.example.desky.devices.led_strip.domain.repository.LedControllerRepository

class SetLedPowerUseCase(
    private val repository: org.example.desky.devices.led_strip.domain.repository.LedControllerRepository
) {
    suspend operator fun invoke(
        isOn: Boolean
    ): Result<Unit> = repository.setPower(isOn)
}
