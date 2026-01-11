package com.notifmanager.data.repository

import com.notifmanager.data.database.dao.AppBehaviorDao
import com.notifmanager.data.database.dao.KeywordDao
import com.notifmanager.data.database.dao.NotificationDao
import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.data.database.entities.KeywordEntity
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.database.entities.KeywordType
import com.notifmanager.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository layer - Single source of truth for data operations
 * ViewModels talk to repository, repository talks to DAOs
 *
 * This pattern makes code cleaner and testable
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val appBehaviorDao: AppBehaviorDao,
    private val keywordDao: KeywordDao
) {

    // ==================== NOTIFICATION OPERATIONS ====================

    /**
     * Save new notification to database
     */
    suspend fun insertNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    /**
     * Get all active notifications (for home screen)
     */
    fun getAllActiveNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllActiveNotifications()
    }

    /**
     * Get notifications by category
     */
    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByCategory(category)
    }

    /**
     * Get notifications from specific app
     */
    fun getNotificationsByApp(packageName: String, limit: Int = 50): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByApp(packageName, limit)
    }

    /**
     * Search notifications
     */
    fun searchNotifications(query: String, limit: Int = 100): Flow<List<NotificationEntity>> {
        return notificationDao.searchNotifications(query, limit)
    }

    /**
     * Mark notification as opened (user clicked it)
     */
    suspend fun markAsOpened(notificationId: Long) {
        val timestamp = System.currentTimeMillis()
        notificationDao.markAsOpened(notificationId, timestamp)

        // Also update app behavior
        val notification = notificationDao.getNotificationById(notificationId)
        notification?.let {
            appBehaviorDao.incrementOpened(it.packageName, timestamp)
        }
    }

    /**
     * Mark notification as dismissed
     */
    suspend fun markAsDismissed(notificationId: Long) {
        val timestamp = System.currentTimeMillis()
        notificationDao.markAsDismissed(notificationId, timestamp)

        // Check if it was a quick dismiss (< 3 seconds)
        val notification = notificationDao.getNotificationById(notificationId)
        notification?.let {
            val timeToAction = timestamp - it.receivedTime
            if (timeToAction < Constants.TIME_QUICK_DISMISS_THRESHOLD_MS) {
                // Quick dismiss = user doesn't care about these
                appBehaviorDao.incrementDismissed(it.packageName, timestamp)
            }
        }
    }

    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: Long) {
        notificationDao.softDelete(notificationId)
    }

    /**
     * Get count by category
     */
    suspend fun getCountByCategory(category: String): Int {
        return notificationDao.getCountByCategory(category)
    }

    /**
     * Clean up old notifications
     */
    suspend fun cleanupOldNotifications(days: Int): Int {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return notificationDao.deleteOlderThan(timestamp)
    }

    /**
     * Clean up silent notifications
     */
    suspend fun cleanupSilentNotifications(days: Int): Int {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return notificationDao.deleteSilentOlderThan(timestamp)
    }

    // ==================== APP BEHAVIOR OPERATIONS ====================

    /**
     * Get or create app behavior entry
     */
    suspend fun getOrCreateAppBehavior(packageName: String): AppBehaviorEntity {
        return appBehaviorDao.getAppBehavior(packageName) ?: AppBehaviorEntity(
            packageName = packageName
        ).also {
            appBehaviorDao.insertOrUpdate(it)
        }
    }

    /**
     * Get app behavior
     */
    suspend fun getAppBehavior(packageName: String): AppBehaviorEntity? {
        return appBehaviorDao.getAppBehavior(packageName)
    }

    /**
     * Get all app behaviors
     */
    fun getAllAppBehaviors(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getAllAppBehaviors()
    }

    /**
     * Update app behavior
     */
    suspend fun updateAppBehavior(appBehavior: AppBehaviorEntity) {
        appBehaviorDao.insertOrUpdate(appBehavior)
    }

    /**
     * Increment notification received
     */
    suspend fun incrementNotificationReceived(packageName: String) {
        val timestamp = System.currentTimeMillis()
        appBehaviorDao.incrementReceived(packageName, timestamp)
    }

    /**
     * Update frequency metrics for an app
     */
    suspend fun updateFrequencyMetrics(packageName: String, lastHour: Int, lastDay: Int, avgPerDay: Float) {
        appBehaviorDao.updateFrequencyMetrics(packageName, lastHour, lastDay, avgPerDay)
    }

    /**
     * Update behavior adjustment score
     */
    suspend fun updateBehaviorAdjustment(packageName: String, adjustment: Int) {
        appBehaviorDao.updateBehaviorAdjustment(packageName, adjustment)
    }

    /**
     * Lock app to category
     */
    suspend fun lockAppToCategory(packageName: String, category: String) {
        appBehaviorDao.lockToCategory(packageName, category)
    }

    /**
     * Unlock app
     */
    suspend fun unlockApp(packageName: String) {
        appBehaviorDao.unlock(packageName)
    }

    /**
     * Reset app behavior
     */
    suspend fun resetAppBehavior(packageName: String) {
        appBehaviorDao.resetBehavior(packageName)
    }

    /**
     * Get spammy apps
     */
    fun getSpammyApps(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getSpammyApps()
    }

    /**
     * Get high engagement apps
     */
    fun getHighEngagementApps(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getHighEngagementApps()
    }

    // ==================== KEYWORD OPERATIONS ====================

    /**
     * Add custom keyword
     */
    suspend fun addKeyword(keyword: String, type: KeywordType, scoreModifier: Int): Long {
        val keywordEntity = KeywordEntity(
            keyword = keyword.lowercase().trim(),
            type = type,
            scoreModifier = scoreModifier
        )
        return keywordDao.insertKeyword(keywordEntity)
    }

    /**
     * Get all keywords
     */
    fun getAllKeywords(): Flow<List<KeywordEntity>> {
        return keywordDao.getAllKeywords()
    }

    /**
     * Get keywords list (for scoring)
     */
    suspend fun getAllKeywordsList(): List<KeywordEntity> {
        return keywordDao.getAllKeywordsList()
    }

    /**
     * Delete keyword
     */
    suspend fun deleteKeyword(keyword: KeywordEntity) {
        keywordDao.deleteKeyword(keyword)
    }

    /**
     * Check if keyword exists
     */
    suspend fun keywordExists(keyword: String): Boolean {
        return keywordDao.getKeywordByText(keyword) != null
    }
}