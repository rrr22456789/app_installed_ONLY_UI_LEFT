package com.example.adaptiveisland.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAppUsage(appUsage: AppUsageEntity)

    @Query("SELECT * FROM app_usage_history WHERE date = :date AND packageName = :packageName LIMIT 1")
    suspend fun getAppUsageByDateAndPackage(date: String, packageName: String): AppUsageEntity?

    @Query("SELECT * FROM app_usage_history WHERE date = :date ORDER BY totalTimeMs DESC")
    fun observeAppUsageForDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_history WHERE date = :date")
    suspend fun getAppUsageForDate(date: String): List<AppUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDailyUsage(dailyUsage: DailyUsageEntity)

    @Query("SELECT * FROM daily_usage_history WHERE date = :date LIMIT 1")
    suspend fun getDailyUsageByDate(date: String): DailyUsageEntity?

    @Query("SELECT * FROM daily_usage_history WHERE date = :date LIMIT 1")
    fun observeDailyUsageForDate(date: String): Flow<DailyUsageEntity?>

    @Query("SELECT * FROM daily_usage_history ORDER BY date DESC")
    fun observeAllDailyUsages(): Flow<List<DailyUsageEntity>>
}
