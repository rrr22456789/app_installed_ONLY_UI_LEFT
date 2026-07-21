package com.example.adaptiveisland.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

/**
 * Immutable model representing the current user-configured settings for the Adaptive Island.
 *
 * This data class is used to pass the settings state from the PreferencesRepository to the 
 * UI and Overlay Service without exposing the underlying DataStore implementation details.
 */
data class AdaptiveIslandPreferences(
    /**
     * Whether the overlay service is allowed to display the capsule.
     */
    val isOverlayEnabled: Boolean = true,
    
    /**
     * The X coordinate of the capsule on the screen.
     * Defaulted to 0 (typically implies horizontal centering in WindowManager configurations).
     */
    val positionX: Int = 0,
    
    /**
     * The Y coordinate of the capsule on the screen.
     * Defaulted to 100 to avoid clipping into the Realme 7 status bar.
     */
    val positionY: Int = 100,
    
    /**
     * The physical width of the capsule in pixels.
     * Defaulted to 500, roughly half the width of the Realme 7 FHD+ display (1080px).
     */
    val capsuleWidth: Int = 500,
    
    /**
     * The physical height of the capsule in pixels.
     * Defaulted to 120, providing enough space for two lines of text (App Name + Timer).
     */
    val capsuleHeight: Int = 120,
    
    /**
     * The corner radius of the capsule in pixels.
     * Defaulted to 60f (half of capsuleHeight) to maintain a perfect "pill" shape.
     */
    val cornerRadius: Float = 60f
)

/**
 * Defines the strict DataStore keys used to read and write preferences.
 * Kept in a separate object to prevent hardcoded strings throughout the repository.
 */
object PreferencesKeys {
    
    /** Key for the overlay toggle (Boolean). */
    val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    
    /** Key for the X coordinate position (Int). */
    val POSITION_X = intPreferencesKey("position_x")
    
    /** Key for the Y coordinate position (Int). */
    val POSITION_Y = intPreferencesKey("position_y")
    
    /** Key for the capsule width (Int). */
    val CAPSULE_WIDTH = intPreferencesKey("capsule_width")
    
    /** Key for the capsule height (Int). */
    val CAPSULE_HEIGHT = intPreferencesKey("capsule_height")
    
    /** Key for the capsule corner radius (Float). */
    val CORNER_RADIUS = floatPreferencesKey("corner_radius")
}