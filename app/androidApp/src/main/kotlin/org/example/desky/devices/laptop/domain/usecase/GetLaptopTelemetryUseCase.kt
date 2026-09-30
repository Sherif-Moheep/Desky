package org.example.desky.devices.laptop.domain.usecase

import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.model.LaptopTelemetryDto

class GetLaptopTelemetryUseCase(
    private val repository: LaptopRepository
) {
    suspend operator fun invoke(): Result<LaptopTelemetryDto> = repository.getTelemetry()
}
