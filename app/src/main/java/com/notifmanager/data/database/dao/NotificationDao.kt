package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Notifications
 *
 * UPDATED: Added cleanupOlderThan query
 */
@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

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

    @Query("SELECT * FROM notifications WHERE category = :category AND isActive = 1 AND isDeleted = 0 ORDER BY receivedTime DESC")
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND isDeleted = 0 ORDER BY receivedTime DESC LIMIT :limit")
    fun getNotificationsByApp(packageName: String, limit: Int = 50): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE category = :category AND isActive = 1 AND isDeleted = 0")
    suspend fun getCountByCategory(category: String): Int

    @Query("""
        SELECT * FROM notifications 
        WHERE (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%')
        AND isDeleted = 0
        ORDER BY receivedTime DESC
        LIMIT :limit
    """)
    fun searchNotifications(query: String, limit: Int = 100): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isOpened = 1, openedAt = :timestamp, timeToAction = :timestamp - receivedTime WHERE id = :notificationId")
    suspend fun markAsOpened(notificationId: Long, timestamp: Long)

    @Query("UPDATE notifications SET isDismissed = 1, dismissedAt = :timestamp, timeToAction = :timestamp - receivedTime WHERE id = :notificationId")
    suspend fun markAsDismissed(notificationId: Long, timestamp: Long)

    @Query("UPDATE notifications SET isDeleted = 1 WHERE id = :notificationId")
    suspend fun softDelete(notificationId: Long)

    @Query("UPDATE notifications SET isActive = 0 WHERE id = :notificationId")
    suspend fun markAsInactive(notificationId: Long)

    @Query("DELETE FROM notifications WHERE receivedTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM notifications WHERE category = 'SILENT' AND receivedTime < :timestamp")
    suspend fun deleteSilentOlderThan(timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :packageName AND receivedTime > :timestampHourAgo")
    suspend fun getCountLastHour(packageName: String, timestampHourAgo: Long): Int

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): NotificationEntity?

    @Query("SELECT * FROM notifications ORDER BY receivedTime DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>
}