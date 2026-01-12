package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.ContentBehaviorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Content Behavior Learning
 * Tracks learned behavior for specific channels/senders
 */
@Dao
interface ContentBehaviorDao {

    /**
     * Insert or update behavior
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(behavior: ContentBehaviorEntity): Long

    /**
     * Get behavior for specific content
     */
    @Query("SELECT * FROM content_behavior WHERE appPackage = :appPackage AND contentId = :contentId")
    suspend fun getBehavior(appPackage: String, contentId: String): ContentBehaviorEntity?

    /**
     * Get all behaviors
     */
    @Query("SELECT * FROM content_behavior ORDER BY lastUpdated DESC")
    fun getAllBehaviors(): Flow<List<ContentBehaviorEntity>>

    /**
     * Get behaviors by app
     */
    @Query("SELECT * FROM content_behavior WHERE appPackage = :appPackage ORDER BY openRate DESC")
    fun getBehaviorsByApp(appPackage: String): Flow<List<ContentBehaviorEntity>>

    /**
     * Get high engagement content
     */
    @Query("SELECT * FROM content_behavior WHERE openRate > 0.7 AND totalReceived > 5 ORDER BY openRate DESC")
    fun getHighEngagementContent(): Flow<List<ContentBehaviorEntity>>

    /**
     * Get ignored content
     */
    @Query("SELECT * FROM content_behavior WHERE ignoreRate > 0.8 AND totalReceived > 5 ORDER BY ignoreRate DESC")
    fun getIgnoredContent(): Flow<List<ContentBehaviorEntity>>

    /**
     * Increment received count
     */
    @Query("""
        UPDATE content_behavior 
        SET totalReceived = totalReceived + 1,
            lastNotificationTime = :timestamp,
            lastUpdated = :timestamp
        WHERE appPackage = :appPackage AND contentId = :contentId
    """)
    suspend fun incrementReceived(appPackage: String, contentId: String, timestamp: Long)

    /**
     * Increment opened count
     */
    @Query("""
        UPDATE content_behavior 
        SET totalOpened = totalOpened + 1,
            openRate = CAST(totalOpened + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE appPackage = :appPackage AND contentId = :contentId
    """)
    suspend fun incrementOpened(appPackage: String, contentId: String, timestamp: Long)

    /**
     * Increment dismissed count
     */
    @Query("""
        UPDATE content_behavior 
        SET totalDismissed = totalDismissed + 1,
            dismissRate = CAST(totalDismissed + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE appPackage = :appPackage AND contentId = :contentId
    """)
    suspend fun incrementDismissed(appPackage: String, contentId: String, timestamp: Long)

    /**
     * Increment ignored count
     */
    @Query("""
        UPDATE content_behavior 
        SET totalIgnored = totalIgnored + 1,
            ignoreRate = CAST(totalIgnored + 1 AS FLOAT) / totalReceived,
            lastUpdated = :timestamp
        WHERE appPackage = :appPackage AND contentId = :contentId
    """)
    suspend fun incrementIgnored(appPackage: String, contentId: String, timestamp: Long)

    /**
     * Update behavior score
     */
    @Query("UPDATE content_behavior SET behaviorScore = :score WHERE appPackage = :appPackage AND contentId = :contentId")
    suspend fun updateBehaviorScore(appPackage: String, contentId: String, score: Int)

    /**
     * Delete behavior
     */
    @Delete
    suspend fun deleteBehavior(behavior: ContentBehaviorEntity)

    /**
     * Clean up old behavior data (not seen in 30+ days)
     */
    @Query("DELETE FROM content_behavior WHERE lastNotificationTime < :timestamp")
    suspend fun cleanupOldBehavior(timestamp: Long): Int
}