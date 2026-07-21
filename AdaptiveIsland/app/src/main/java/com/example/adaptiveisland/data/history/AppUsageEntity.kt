package com.example.adaptiveisland.data.history

import androidx.room.Entity
import androidx.room.Index

/**
 * Represents the cumulative screen time accumulated by a single specific application 
 * within a single calendar date boundary.
 *
 * Employs a composite primary key consisting of both [date] and [packageName] to ensure 
 * uniqueness per application per day.
 */
@Entity(
    tableName = "app_usage_history",
    primaryKeys = ["date", "packageName"],
    indices = [
        Index(value = ["date"]),
        Index(value = ["packageName"])
    ]
)
data class AppUsageEntity(
    val date: String,
    val packageName: String,
    val appName: String,
    val totalTimeMs: Long,
    val lastUpdatedTimestamp: Long
)