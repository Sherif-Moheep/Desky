package org.example.desky.devices.led_strip.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.example.desky.devices.led_strip.data.datasource.LedPresetDataStore
import org.example.desky.devices.led_strip.domain.model.LedPreset
import org.example.desky.devices.led_strip.domain.repository.LedPresetRepository

class LedPresetRepositoryImpl(
    private val dataStore: LedPresetDataStore
) : LedPresetRepository {

    override fun getPresets(): Flow<List<LedPreset>> {
        return dataStore.presetsFlow
    }

    override suspend fun savePreset(
        preset: LedPreset
    ): Result<Unit> = runCatching {
        val current = dataStore.presetsFlow.first()
        val updated = if (current.any { it.id == preset.id }) {
            current.map { if (it.id == preset.id) preset else it }
        } else {
            current + preset
        }
        dataStore.savePresets(updated)
    }

    override suspend fun deletePreset(
        id: String
    ): Result<Unit> = runCatching {
        val current = dataStore.presetsFlow.first()
        val updated = current.filterNot { it.id == id }
        dataStore.savePresets(updated)
    }

    override fun getLastBrightness(): Flow<Float> {
        return dataStore.lastBrightnessFlow
    }

    override suspend fun saveLastBrightness(
        brightness: Float
    ): Result<Unit> = runCatching {
        dataStore.saveLastBrightness(brightness)
    }
}
