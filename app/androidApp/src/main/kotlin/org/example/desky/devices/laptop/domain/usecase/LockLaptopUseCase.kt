package org.example.desky.devices.laptop.domain.usecase

import org.example.desky.devices.laptop.domain.repository.LaptopRepository

class LockLaptopUseCase(
    private val repository: LaptopRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.lock()
}
