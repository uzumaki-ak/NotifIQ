package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notifications
 * Defines all database operations for notification data
 *
 * Using Flow for reactive updates - UI automatically updates when data changes
 */
@Dao
interface NotificationDao {

    /**
     * Insert a new notification into database
     * Returns the auto-generated ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    /**
     * Insert multiple notifications (for batch operations)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    /**
     * Update existing notification (when user interacts with it)
     */
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    /**
     * Delete notification permanently
     */
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    /**
     * Get all active notifications, grouped by category, sorted by time (newest first)
     * This is the main query for the home screen
     */
    @Query("""
        SELECT * FROM notifications 
        WHERE isActive = 1 AND isDeleted = 0
        ORDER BY 
            CASE category
                WHEN 'CRITICAL' THEN 1
                WHEN 'IMPORTANT' THEN 2
                WHEN 'NORMAL' THEN 3
                WHEN 'SILENT' THEN 4
            END,
            receivedTime DESC
    """)
    fun getAllActiveNotifications(): Flow<List<NotificationEntity>>

    /**
     * Get notifications by category
     */
    @Query("SELECT * FROM notifications WHERE category = :category AND isActive = 1 AND isDeleted = 0 ORDER BY receivedTime DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>>

    /**
     * Get notifications from a specific app
     */
    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND isDeleted = 0 ORDER BY receivedTime DESC LIMIT :limit")
    fun getNotificationsByApp(packageName: String, limit: Int = 50): Flow<List<NotificationEntity>>

    /**
     * Count notifications by category (for statistics)
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE category = :category AND isActive = 1 AND isDeleted = 0")
    suspend fun getCountByCategory(category: String): Int

    /**
     * Search notifications by text content
     */
    @Query("""
        SELECT * FROM notifications 
        WHERE (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%')
        AND isDeleted = 0
        ORDER BY receivedTime DESC
        LIMIT :limit
    """)
    fun searchNotifications(query: String, limit: Int = 100): Flow<List<NotificationEntity>>

    /**
     * Mark notification as opened
     */
    @Query("UPDATE notifications SET isOpened = 1, openedAt = :timestamp, timeToAction = :timestamp - receivedTime WHERE id = :notificationId")
    suspend fun markAsOpened(notificationId: Long, timestamp: Long)

    /**
     * Mark notification as dismissed
     */
    @Query("UPDATE notifications SET isDismissed = 1, dismissedAt = :timestamp, timeToAction = :timestamp - receivedTime WHERE id = :notificationId")
    suspend fun markAsDismissed(notificationId: Long, timestamp: Long)

    /**
     * Soft delete notification (mark as deleted without removing from DB)
     */
    @Query("UPDATE notifications SET isDeleted = 1 WHERE id = :notificationId")
    suspend fun softDelete(notificationId: Long)

    /**
     * Mark notification as inactive (when dismissed from system tray)
     */
    @Query("UPDATE notifications SET isActive = 0 WHERE id = :notificationId")
    suspend fun markAsInactive(notificationId: Long)

    /**
     * Cleanup old notifications (for background worker)
     */
    @Query("DELETE FROM notifications WHERE receivedTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    /**
     * Delete all silent notifications older than X days
     */
    @Query("DELETE FROM notifications WHERE category = 'SILENT' AND receivedTime < :timestamp")
    suspend fun deleteSilentOlderThan(timestamp: Long): Int

    /**
     * Get notification count for last hour (for frequency checking)
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :packageName AND receivedTime > :timestampHourAgo")
    suspend fun getCountLastHour(packageName: String, timestampHourAgo: Long): Int

    /**
     * Get notification by ID
     */
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): NotificationEntity?

    /**
     * Get all notifications (for export/backup)
     */
    @Query("SELECT * FROM notifications ORDER BY receivedTime DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>
}