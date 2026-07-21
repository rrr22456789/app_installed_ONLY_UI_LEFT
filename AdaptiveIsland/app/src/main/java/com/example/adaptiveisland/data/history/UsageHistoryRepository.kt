package com.example.adaptiveisland.data.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for managing all screen usage history data.
 * * This class acts as the single, clean API for the rest of the application to interact 
 * with local SQLite persistence. It strictly abstracts away the DAO and exposes standard 
 * Kotlin Coroutine suspend functions and Flows.
 * * Per architectural constraints, this repository is completely devoid of business logic, 
 * time calculations, and session management. It strictly reads and writes data.
 */
class UsageHistoryRepository(
    private val usageDao: UsageDao
) {

    // ============================================================================
    // APP USAGE OPERATIONS (Microscopic View)
    // ============================================================================

    /**
     * Persists an [AppUsageEntity] to the database.
     * Overwrites any existing record with the same date and package name.
     * * @param appUsage The fully constructed and calculated entity to save.
     */
    suspend fun saveAppUsage(appUsage: AppUsageEntity) {
        usageDao.insertOrReplaceAppUsage(appUsage)
    }

    /**
     * Retrieves a single app's usage record for a specific date.
     * * @param date The date string (e.g., "YYYY-MM-DD").
     * @param packageName The application's unique package ID.
     * @return The existing [AppUsageEntity], or null if no record exists yet.
     */
    suspend fun getAppUsage(date: String, packageName: String): AppUsageEntity? {
        return usageDao.getAppUsageByDateAndPackage(date, packageName)
    }

    /**
     * Exposes a continuous stream of app usage data for a specific date,
     * sorted by highest usage time.
     * * @param date The date string to observe.
     * @return A reactive [Flow] emitting the list of app usages.
     */
    fun observeAppUsageForDate(date: String): Flow<List<AppUsageEntity>> {
        return usageDao.observeAppUsageForDate(date)
    }

    /**
     * Fetches a static list of all app usages for a specific date.
     * * @param date The date string to fetch.
     * @return A one-shot list of [AppUsageEntity] for the requested date.
     */
    suspend fun getAppUsageListForDate(date: String): List<AppUsageEntity> {
        return usageDao.getAppUsageForDate(date)
    }

    // ============================================================================
    // DAILY USAGE OPERATIONS (Macroscopic View)
    // ============================================================================

    /**
     * Persists a [DailyUsageEntity] to the database.
     * Overwrites any existing record for the exact same date.
     * * @param dailyUsage The fully constructed and calculated entity to save.
     */
    suspend fun saveDailyUsage(dailyUsage: DailyUsageEntity) {
        usageDao.insertOrReplaceDailyUsage(dailyUsage)
    }

    /**
     * Retrieves the total daily usage record for a specific date.
     * * @param date The date string (e.g., "YYYY-MM-DD").
     * @return The existing [DailyUsageEntity], or null if no record exists yet.
     */
    suspend fun getDailyUsage(date: String): DailyUsageEntity? {
        return usageDao.getDailyUsageByDate(date)
    }

    /**
     * Exposes a continuous stream of the total device screen time for a specific date.
     * * @param date The date string to observe.
     * @return A reactive [Flow] emitting the daily total.
     */
    fun observeDailyUsageForDate(date: String): Flow<DailyUsageEntity?> {
        return usageDao.observeDailyUsageForDate(date)
    }

    /**
     * Exposes a continuous stream of all historical daily totals, sorted newest to oldest.
     * * @return A reactive [Flow] emitting all past and present daily records.
     */
    fun observeAllDailyUsages(): Flow<List<DailyUsageEntity>> {
        return usageDao.observeAllDailyUsages()
    }
    /**
 * Compatibility API used by UsageTracker.
 * Adds the supplied duration to the application's existing total for the day.
 */
suspend fun recordAppUsage(
    packageName: String,
    appName: String,
    dateStr: String,
    durationMs: Long
) {
    val existing = usageDao.getAppUsageByDateAndPackage(dateStr, packageName)

    val updated = AppUsageEntity(
        date = dateStr,
        packageName = packageName,
        appName = appName,
        totalTimeMs = (existing?.totalTimeMs ?: 0L) + durationMs,
        lastUpdatedTimestamp = System.currentTimeMillis()
    )

    usageDao.insertOrReplaceAppUsage(updated)

    val currentDaily = usageDao.getDailyUsageByDate(dateStr)

    usageDao.insertOrReplaceDailyUsage(
        DailyUsageEntity(
            date = dateStr,
            totalScreenTimeMs = (currentDaily?.totalScreenTimeMs ?: 0L) + durationMs,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    )
}

/**
 * Compatibility API used by DashboardViewModel.
 */
suspend fun getAllAppsForDate(date: String): List<AppUsageEntity> {
    return usageDao.getAppUsageForDate(date)
}

/**
 * Compatibility API used by DashboardViewModel.
 */
suspend fun getDailyHistory(): List<DailyUsageEntity> {
    return observeAllDailyUsages().first()
}
}