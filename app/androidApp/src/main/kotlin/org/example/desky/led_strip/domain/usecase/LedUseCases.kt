package org.example.desky.led_strip.domain.usecase

/**
 * Clean Architecture Use Case Wrapper.
 * Bundles all LED domain operations into a single cohesive dependency for the presentation layer.
 */
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
