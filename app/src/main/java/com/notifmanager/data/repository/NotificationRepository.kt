package com.notifmanager.data.repository

import com.notifmanager.data.database.dao.*
import com.notifmanager.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository - UPDATED with content-level operations
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val appBehaviorDao: AppBehaviorDao,
    private val keywordDao: KeywordDao,
    private val contentPreferenceDao: ContentPreferenceDao,  // NEW
    private val contentBehaviorDao: ContentBehaviorDao       // NEW
) {

    // ==================== NOTIFICATION OPERATIONS ====================

    suspend fun insertNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    fun getAllActiveNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllActiveNotifications()
    }

    fun getNotificationsByCategory(category: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByCategory(category)
    }

    fun getNotificationsByApp(packageName: String, limit: Int = 50): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByApp(packageName, limit)
    }

    fun searchNotifications(query: String, limit: Int = 100): Flow<List<NotificationEntity>> {
        return notificationDao.searchNotifications(query, limit)
    }

    suspend fun markAsOpened(notificationId: Long) {
        val timestamp = System.currentTimeMillis()
        notificationDao.markAsOpened(notificationId, timestamp)

        val notification = notificationDao.getNotificationById(notificationId)
        notification?.let {
            appBehaviorDao.incrementOpened(it.packageName, timestamp)
        }
    }

    suspend fun markAsDismissed(notificationId: Long) {
        val timestamp = System.currentTimeMillis()
        notificationDao.markAsDismissed(notificationId, timestamp)

        val notification = notificationDao.getNotificationById(notificationId)
        notification?.let {
            val timeToAction = timestamp - it.receivedTime
            if (timeToAction < 3000) {  // Quick dismiss
                appBehaviorDao.incrementDismissed(it.packageName, timestamp)
            }
        }
    }

    suspend fun deleteNotification(notificationId: Long) {
        notificationDao.softDelete(notificationId)
    }

    suspend fun getCountByCategory(category: String): Int {
        return notificationDao.getCountByCategory(category)
    }

    suspend fun cleanupOldNotifications(days: Int): Int {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return notificationDao.deleteOlderThan(timestamp)
    }

    suspend fun cleanupSilentNotifications(days: Int): Int {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return notificationDao.deleteSilentOlderThan(timestamp)
    }

    // NEW: General cleanup
    suspend fun cleanupOlderThan(days: Int): Int {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return notificationDao.deleteOlderThan(timestamp)
    }

    // ==================== APP BEHAVIOR OPERATIONS ====================

    suspend fun getOrCreateAppBehavior(packageName: String): AppBehaviorEntity {
        return appBehaviorDao.getAppBehavior(packageName) ?: AppBehaviorEntity(
            packageName = packageName
        ).also {
            appBehaviorDao.insertOrUpdate(it)
        }
    }

    suspend fun getAppBehavior(packageName: String): AppBehaviorEntity? {
        return appBehaviorDao.getAppBehavior(packageName)
    }

    fun getAllAppBehaviors(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getAllAppBehaviors()
    }

    suspend fun updateAppBehavior(appBehavior: AppBehaviorEntity) {
        appBehaviorDao.insertOrUpdate(appBehavior)
    }

    suspend fun incrementNotificationReceived(packageName: String) {
        val timestamp = System.currentTimeMillis()
        appBehaviorDao.incrementReceived(packageName, timestamp)
    }

    suspend fun updateFrequencyMetrics(packageName: String, lastHour: Int, lastDay: Int, avgPerDay: Float) {
        appBehaviorDao.updateFrequencyMetrics(packageName, lastHour, lastDay, avgPerDay)
    }

    suspend fun updateBehaviorAdjustment(packageName: String, adjustment: Int) {
        appBehaviorDao.updateBehaviorAdjustment(packageName, adjustment)
    }

    suspend fun lockAppToCategory(packageName: String, category: String) {
        appBehaviorDao.lockToCategory(packageName, category)
    }

    suspend fun unlockApp(packageName: String) {
        appBehaviorDao.unlock(packageName)
    }

    suspend fun resetAppBehavior(packageName: String) {
        appBehaviorDao.resetBehavior(packageName)
    }

    fun getSpammyApps(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getSpammyApps()
    }

    fun getHighEngagementApps(): Flow<List<AppBehaviorEntity>> {
        return appBehaviorDao.getHighEngagementApps()
    }

    // ==================== KEYWORD OPERATIONS ====================

    suspend fun addKeyword(keyword: String, type: KeywordType, scoreModifier: Int): Long {
        val keywordEntity = KeywordEntity(
            keyword = keyword.lowercase().trim(),
            type = type,
            scoreModifier = scoreModifier
        )
        return keywordDao.insertKeyword(keywordEntity)
    }

    fun getAllKeywords(): Flow<List<KeywordEntity>> {
        return keywordDao.getAllKeywords()
    }

    suspend fun getAllKeywordsList(): List<KeywordEntity> {
        return keywordDao.getAllKeywordsList()
    }

    suspend fun deleteKeyword(keyword: KeywordEntity) {
        keywordDao.deleteKeyword(keyword)
    }

    suspend fun keywordExists(keyword: String): Boolean {
        return keywordDao.getKeywordByText(keyword) != null
    }

    // ==================== CONTENT PREFERENCE OPERATIONS (NEW) ====================

    suspend fun getOrCreateContentPreference(
        appPackage: String,
        contentId: String,
        contentType: String
    ): ContentPreferenceEntity {
        return contentPreferenceDao.getPreference(appPackage, contentId) ?: ContentPreferenceEntity(
            appPackage = appPackage,
            contentId = contentId,
            contentType = contentType,
            preferenceScore = 0
        ).also {
            contentPreferenceDao.insertOrUpdate(it)
        }
    }

    suspend fun getContentPreference(appPackage: String, contentId: String): ContentPreferenceEntity? {
        return contentPreferenceDao.getPreference(appPackage, contentId)
    }

    fun getAllContentPreferences(): Flow<List<ContentPreferenceEntity>> {
        return contentPreferenceDao.getAllPreferences()
    }

    fun getContentPreferencesByApp(appPackage: String): Flow<List<ContentPreferenceEntity>> {
        return contentPreferenceDao.getPreferencesByApp(appPackage)
    }

    suspend fun updateContentPreference(id: Long, score: Int) {
        val timestamp = System.currentTimeMillis()
        contentPreferenceDao.updatePreferenceScore(id, score, timestamp)
    }

    suspend fun lockContentPreference(id: Long) {
        contentPreferenceDao.lockPreference(id)
    }

    suspend fun unlockContentPreference(id: Long) {
        contentPreferenceDao.unlockPreference(id)
    }

    suspend fun deleteContentPreference(preference: ContentPreferenceEntity) {
        contentPreferenceDao.deletePreference(preference)
    }

    // ==================== CONTENT BEHAVIOR OPERATIONS (NEW) ====================

    suspend fun getOrCreateContentBehavior(
        appPackage: String,
        contentId: String,
        contentType: String
    ): ContentBehaviorEntity {
        return contentBehaviorDao.getBehavior(appPackage, contentId) ?: ContentBehaviorEntity(
            appPackage = appPackage,
            contentId = contentId,
            contentType = contentType
        ).also {
            contentBehaviorDao.insertOrUpdate(it)
        }
    }

    suspend fun getContentBehavior(appPackage: String, contentId: String): ContentBehaviorEntity? {
        return contentBehaviorDao.getBehavior(appPackage, contentId)
    }

    fun getAllContentBehaviors(): Flow<List<ContentBehaviorEntity>> {
        return contentBehaviorDao.getAllBehaviors()
    }

    fun getContentBehaviorsByApp(appPackage: String): Flow<List<ContentBehaviorEntity>> {
        return contentBehaviorDao.getBehaviorsByApp(appPackage)
    }

    fun getHighEngagementContent(): Flow<List<ContentBehaviorEntity>> {
        return contentBehaviorDao.getHighEngagementContent()
    }

    fun getIgnoredContent(): Flow<List<ContentBehaviorEntity>> {
        return contentBehaviorDao.getIgnoredContent()
    }

    suspend fun incrementContentNotificationReceived(appPackage: String, contentId: String) {
        val timestamp = System.currentTimeMillis()
        contentBehaviorDao.incrementReceived(appPackage, contentId, timestamp)
    }

    suspend fun incrementContentOpened(appPackage: String, contentId: String) {
        val timestamp = System.currentTimeMillis()
        contentBehaviorDao.incrementOpened(appPackage, contentId, timestamp)
    }

    suspend fun incrementContentDismissed(appPackage: String, contentId: String) {
        val timestamp = System.currentTimeMillis()
        contentBehaviorDao.incrementDismissed(appPackage, contentId, timestamp)
    }

    suspend fun updateContentBehaviorScore(appPackage: String, contentId: String, score: Int) {
        contentBehaviorDao.updateBehaviorScore(appPackage, contentId, score)
    }
}