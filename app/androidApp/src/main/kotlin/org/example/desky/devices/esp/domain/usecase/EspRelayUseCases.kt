package org.example.desky.devices.esp.domain.usecase

data class EspRelayUseCases(
    val checkStatus: CheckEspStatusUseCase,
    val observeConnectionStatus: ObserveEspConnectionStatusUseCase,
    val discover: DiscoverEspUseCase,
    val getStatus: GetEspStatusUseCase,
    val powerOn: PowerOnEspUseCase,
    val powerOff: PowerOffEspUseCase
)
