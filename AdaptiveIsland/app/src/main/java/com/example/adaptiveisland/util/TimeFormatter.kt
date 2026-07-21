package com.example.adaptiveisland.util

import java.util.Locale

/**
 * Stateless deterministic mathematical execution block parsing duration variables.
 * Bypasses calendar components completely to deliver light, performance-stable duration masks.
 */
object TimeFormatter {

    /**
     * Translates raw milliseconds into structured layout metrics: HH:mm:ss.
     * Supports values expanding beyond standard 24-hour cycles cleanly.
     */
    fun formatElapsedTime(elapsedTimeMs: Long): String {
        if (elapsedTimeMs <= 0L) return "00:00:00"

        val totalSeconds = elapsedTimeMs / 1000L
        val seconds = totalSeconds % 60L
        val totalMinutes = totalSeconds / 60L
        val minutes = totalMinutes % 60L
        val hours = totalMinutes / 60L

        // Fast arithmetic formatting prevents high allocation loops during frequent ui updates
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }
}