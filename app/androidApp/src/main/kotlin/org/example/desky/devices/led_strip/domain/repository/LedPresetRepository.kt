package org.example.desky.devices.led_strip.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.desky.devices.led_strip.domain.model.LedPreset

interface LedPresetRepository {
    fun getPresets(): Flow<List<LedPreset>>
    suspend fun savePreset(preset: LedPreset): Result<Unit>
    suspend fun deletePreset(id: String): Result<Unit>
    fun getLastBrightness(): Flow<Float>
    suspend fun saveLastBrightness(brightness: Float): Result<Unit>
}
