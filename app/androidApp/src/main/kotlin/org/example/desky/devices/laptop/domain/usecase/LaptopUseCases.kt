package org.example.desky.devices.laptop.domain.usecase

data class LaptopUseCases(
    val checkStatus: CheckLaptopStatusUseCase,
    val observeConnectionStatus: ObserveLaptopConnectionStatusUseCase,
    val discover: DiscoverLaptopUseCase,
    val getTelemetry: GetLaptopTelemetryUseCase,
    val lock: LockLaptopUseCase,
    val sleep: SleepLaptopUseCase,
    val restart: RestartLaptopUseCase,
    val shutdown: ShutdownLaptopUseCase,
    val observeTelemetry: ObserveLaptopTelemetryUseCase
)
