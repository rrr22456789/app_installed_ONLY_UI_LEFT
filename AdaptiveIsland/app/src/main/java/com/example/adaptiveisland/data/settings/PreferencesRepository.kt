package com.example.adaptiveisland.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Enforce single instance of DataStore globally via extension delegate to avoid multiple instances per file scope
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "adaptive_island_settings")

/**
 * Clean data access repository managing user preference persistence via Jetpack Preferences DataStore.
 * 
 * Handles reading and writing underlying visual configuration profiles cleanly 
 * away from the lifecycle of WindowManager elements or internal Room entities.
 */
class PreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    private object PreferencesKeys {
        val IS_OVERLAY_ENABLED = booleanPreferencesKey("is_overlay_enabled")
        val POSITION_X = intPreferencesKey("position_x")
        val POSITION_Y = intPreferencesKey("position_y")
        val CAPSULE_WIDTH = intPreferencesKey("capsule_width")
        val CAPSULE_HEIGHT = intPreferencesKey("capsule_height")
    }

    /**
     * Exposes a continuous stream of configuration profiles initialized with robust fallback parameters.
     */
    val preferencesFlow: Flow<AdaptiveIslandPreferences> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AdaptiveIslandPreferences(
                isOverlayEnabled = preferences[PreferencesKeys.IS_OVERLAY_ENABLED] ?: true,
                positionX = preferences[PreferencesKeys.POSITION_X] ?: 0,
                positionY = preferences[PreferencesKeys.POSITION_Y] ?: 0,
                capsuleWidth = preferences[PreferencesKeys.CAPSULE_WIDTH] ?: 500, // Safe design default fallback px
                capsuleHeight = preferences[PreferencesKeys.CAPSULE_HEIGHT] ?: 120 // Safe design default fallback px
            )
        }

    /**
     * Persists a total overlay visibility state toggle switch mutation block.
     */
    suspend fun updateOverlayEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_OVERLAY_ENABLED] = enabled
        }
    }

    /**
     * Persists raw visual coordinate translation spaces calculated inside the movement gestures logic.
     */
    suspend fun updatePosition(x: Int, y: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POSITION_X] = x
            preferences[PreferencesKeys.POSITION_Y] = y
        }
    }

    /**
     * Persists total bounds properties defining structural length specs for the display capsule container.
     */
    suspend fun updateCapsuleDimensions(width: Int, height: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAPSULE_WIDTH] = width
            preferences[PreferencesKeys.CAPSULE_HEIGHT] = height
        }
    }
}