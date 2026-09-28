package org.example.desky.devices.led.domain.usecase

class SetLedPowerUseCase(
    private val repository: org.example.desky.devices.led.domain.repository.LedControllerRepository
) {
    suspend operator fun invoke(
        isOn: Boolean
    ): Result<Unit> = repository.setPower(isOn)
}
