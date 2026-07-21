package com.example.adaptiveisland.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the global aggregated screen time for the entire device across a 
 * single calendar date boundary.
 */
@Entity(tableName = "daily_usage_history")
data class DailyUsageEntity(
    @PrimaryKey
    val date: String,
    val totalScreenTimeMs: Long,
    val lastUpdatedTimestamp: Long
)