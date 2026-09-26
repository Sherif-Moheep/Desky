package org.example.desky.devices.led_strip.domain.usecase

data class LedUseCases(
    val connect: ConnectLedUseCase,
    val setPower: SetLedPowerUseCase,
    val setColor: SetLedColorUseCase,
    val observeConnectionStatus: ObserveConnectionStatusUseCase,
    val observePresets: ObservePresetsUseCase,
    val savePreset: SavePresetUseCase,
    val deletePreset: DeletePresetUseCase,
    val observeLastBrightness: ObserveLastBrightnessUseCase,
    val saveLastBrightness: SaveLastBrightnessUseCase
)
