package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.AppBehaviorEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for App Behavior Learning
 * Tracks user behavior patterns per app
 */
@Dao
interface AppBehaviorDao {

    /**
     * Insert or update app behavior data
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appBehavior: AppBehaviorEntity)

    /**
     * Get behavior data for a specific app
     */
    @Query("SELECT * FROM app_behavior WHERE packageName = :packageName")
    suspend fun getAppBehavior(packageName: String): AppBehaviorEntity?

    /**
     * Get behavior data as Flow (for reactive updates)
     */
    @Query("SELECT * FROM app_behavior WHERE packageName = :packageName")
    fun getAppBehaviorFlow(packageName: String): Flow<AppBehaviorEntity?>

    /**
     * Get all app behaviors
     */
    @Query("SELECT * FROM app_behavior ORDER BY lastNotificationTime DESC")
    fun getAllAppBehaviors(): Flow<List<AppBehaviorEntity>>

    /**
     * Get apps with high engagement (user opens frequently)
     */
    @Query("SELECT * FROM app_behavior WHERE openRate > 0.7 AND totalReceived > 5 ORDER BY openRate DESC")
    fun getHighEngagementApps(): Flow<List<AppBehaviorEntity>>

    /**
     * Get spammy apps (too many notifications)
     */
    @Query("SELECT * FROM app_behavior WHERE notificationsLastHour > 10 OR averagePerDay > 30 ORDER BY averagePerDay DESC")
    fun getSpammyApps(): Flow<List<AppBehaviorEntity>>

    /**
     * Increment notification received count
     */
    @Query("""
        UPDATE app_behavior 
        SET totalReceived = totalReceived + 1,
            lastNotificationTime = :timestamp,
            lastUpdated = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun incrementReceived(packageName: String, timestamp: Long)

    /**
     * Increment opened count
     */
    @Query("""
        UPDATE app_behavior 
        SET totalOpened = totalOpened + 1,
            openRate = CAST(totalOpened + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun incrementOpened(packageName: String, timestamp: Long)

    /**
     * Increment dismissed count
     */
    @Query("""
        UPDATE app_behavior 
        SET totalDismissed = totalDismissed + 1,
            dismissRate = CAST(totalDismissed + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun incrementDismissed(packageName: String, timestamp: Long)

    /**
     * Increment ignored count
     */
    @Query("""
        UPDATE app_behavior 
        SET totalIgnored = totalIgnored + 1,
            ignoreRate = CAST(totalIgnored + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE packageName = :packageName
    """)
    suspend fun incrementIgnored(packageName: String, timestamp: Long)

    /**
     * Update behavior adjustment score
     * This is recalculated periodically based on open/dismiss/ignore rates
     */
    @Query("UPDATE app_behavior SET behaviorAdjustment = :adjustment WHERE packageName = :packageName")
    suspend fun updateBehaviorAdjustment(packageName: String, adjustment: Int)

    /**
     * Update frequency counters
     */
    @Query("""
        UPDATE app_behavior 
        SET notificationsLastHour = :lastHour,
            notificationsLastDay = :lastDay,
            averagePerDay = :averagePerDay
        WHERE packageName = :packageName
    """)
    suspend fun updateFrequencyMetrics(
        packageName: String,
        lastHour: Int,
        lastDay: Int,
        averagePerDay: Float
    )

    /**
     * Lock app to specific category (user preference)
     */
    @Query("UPDATE app_behavior SET isLocked = 1, lockedCategory = :category WHERE packageName = :packageName")
    suspend fun lockToCategory(packageName: String, category: String)

    /**
     * Unlock app (allow learning again)
     */
    @Query("UPDATE app_behavior SET isLocked = 0, lockedCategory = NULL WHERE packageName = :packageName")
    suspend fun unlock(packageName: String)

    /**
     * Set custom base score for app
     */
    @Query("UPDATE app_behavior SET customBaseScore = :score WHERE packageName = :packageName")
    suspend fun setCustomBaseScore(packageName: String, score: Int)

    /**
     * Reset behavior data for app (fresh start)
     */
    @Query("""
        UPDATE app_behavior 
        SET totalOpened = 0, 
            totalDismissed = 0, 
            totalIgnored = 0,
            openRate = 0,
            dismissRate = 0,
            ignoreRate = 0,
            behaviorAdjustment = 0
        WHERE packageName = :packageName
    """)
    suspend fun resetBehavior(packageName: String)

    /**
     * Delete app behavior data
     */
    @Query("DELETE FROM app_behavior WHERE packageName = :packageName")
    suspend fun deleteAppBehavior(packageName: String)

    /**
     * Clean up old behavior data (apps not seen in 30+ days)
     */
    @Query("DELETE FROM app_behavior WHERE lastNotificationTime < :timestamp AND isLocked = 0")
    suspend fun cleanupOldBehavior(timestamp: Long): Int
}