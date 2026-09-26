package org.example.desky.devices.led_strip.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.desky.devices.led_strip.domain.model.LedPreset
import org.json.JSONArray
import org.json.JSONObject

private val Context.ledDataStore: DataStore<Preferences> by preferencesDataStore(name = "desky_led_prefs")

class LedPresetDataStore(
    private val context: Context
) {
    companion object {
        val KEY_PRESETS_JSON = stringPreferencesKey("led_presets_json")
        val KEY_LAST_BRIGHTNESS = floatPreferencesKey("led_last_brightness")
        const val DEFAULT_BRIGHTNESS = 0.85f

        private val InitialPresets = listOf(
            LedPreset(
                id = "preset_focus",
                name = "Focus Amber",
                red = 255,
                green = 158,
                blue = 59,
                brightness = 0.85f
            ),
            LedPreset(
                id = "preset_cyberpunk",
                name = "Cyberpunk Teal",
                red = 0,
                green = 229,
                blue = 255,
                brightness = 1.0f
            ),
            LedPreset(
                id = "preset_cozy",
                name = "Cozy Warm",
                red = 255,
                green = 180,
                blue = 100,
                brightness = 0.6f
            ),
            LedPreset(
                id = "preset_forest",
                name = "Forest Green",
                red = 56,
                green = 142,
                blue = 60,
                brightness = 0.7f
            )
        )
    }

    val presetsFlow: Flow<List<LedPreset>> = context.ledDataStore.data.map { prefs ->
        val json = prefs[KEY_PRESETS_JSON]
        if (json.isNullOrBlank()) {
            InitialPresets
        } else {
            try {
                deserializePresets(json)
            } catch (_: Exception) {
                InitialPresets
            }
        }
    }

    val lastBrightnessFlow: Flow<Float> = context.ledDataStore.data.map { prefs ->
        prefs[KEY_LAST_BRIGHTNESS] ?: DEFAULT_BRIGHTNESS
    }

    suspend fun savePresets(presets: List<LedPreset>) {
        val json = serializePresets(presets)
        context.ledDataStore.edit { prefs ->
            prefs[KEY_PRESETS_JSON] = json
        }
    }

    suspend fun saveLastBrightness(brightness: Float) {
        context.ledDataStore.edit { prefs ->
            prefs[KEY_LAST_BRIGHTNESS] = brightness
        }
    }

    private fun serializePresets(presets: List<LedPreset>): String {
        val array = JSONArray()
        presets.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("r", p.red)
                put("g", p.green)
                put("b", p.blue)
                if (p.brightness != null) {
                    put("brightness", p.brightness.toDouble())
                }
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializePresets(json: String): List<LedPreset> {
        val array = JSONArray(json)
        val list = mutableListOf<LedPreset>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                LedPreset(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    red = obj.getInt("r"),
                    green = obj.getInt("g"),
                    blue = obj.getInt("b"),
                    brightness = if (obj.has("brightness")) obj.getDouble("brightness").toFloat() else null
                )
            )
        }
        return list
    }
}
