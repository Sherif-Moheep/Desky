package org.example.desky.devices.laptop.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.desky.devices.laptop.domain.repository.LaptopRepository
import org.example.desky.model.LaptopTelemetryDto

class ObserveLaptopTelemetryUseCase(
    private val repository: LaptopRepository
) {
    operator fun invoke(): StateFlow<LaptopTelemetryDto?> = repository.telemetry
}
