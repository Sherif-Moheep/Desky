package org.example.desky.core.domain.usecase

data class DeskUseCases(
    val observeDeskState: ObserveDeskStateUseCase,
    val refreshDeskState: RefreshDeskStateUseCase,
    val pingDevice: PingDeskDeviceUseCase,
    val powerOnDesk: PowerOnDeskUseCase,
    val powerOffDesk: PowerOffDeskUseCase,
    val setLedPower: SetDeskLedPowerUseCase,
    val shutdownLaptop: ShutdownDeskLaptopUseCase
)
