package com.notifmanager.domain.usecase

import android.service.notification.StatusBarNotification
import com.notifmanager.data.database.entities.ContentBehaviorEntity
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.domain.scoring.ImportanceScorer
import com.notifmanager.utils.ContentExtractor
import com.notifmanager.utils.NotificationExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * USE CASE: Process incoming notification
 *
 * UPDATED: Now extracts and scores content-level (channel/sender)
 */
class ProcessNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scorer: ImportanceScorer,
    private val notificationExtractor: NotificationExtractor,
    private val contentExtractor: ContentExtractor  // NEW
) {

    suspend operator fun invoke(sbn: StatusBarNotification): Long = withContext(Dispatchers.IO) {
        // Extract basic notification data
        val packageName = sbn.packageName
        val appName = notificationExtractor.getAppName(sbn)
        val title = notificationExtractor.getTitle(sbn)
        val text = notificationExtractor.getText(sbn)
        val subText = notificationExtractor.getSubText(sbn)
        val bigText = notificationExtractor.getBigText(sbn)
        val postedTime = sbn.postTime
        val receivedTime = System.currentTimeMillis()

        // NEW: Extract content ID (channel/sender)
        val (contentId, contentType) = contentExtractor.extractContent(packageName, title, text)

        // Get app-level behavior
        val appBehavior = repository.getOrCreateAppBehavior(packageName)

        // NEW: Get content-level preference
        val contentPreference = if (contentId != null) {
            repository.getOrCreateContentPreference(packageName, contentId, contentType.name)
        } else null

        // NEW: Get content-level behavior
        val contentBehavior = if (contentId != null) {
            repository.getOrCreateContentBehavior(packageName, contentId, contentType.name)
        } else null

        // Get custom keywords
        val customKeywords = repository.getAllKeywordsList()

        // Calculate frequency
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        val notificationsLastHour = appBehavior.notificationsLastHour

        // CALCULATE SCORE (with content-level data)
        val scoreBreakdown = scorer.calculateScore(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            appBehavior = appBehavior,
            contentPreference = contentPreference,  // NEW
            contentBehavior = contentBehavior,      // NEW
            customKeywords = customKeywords,
            notificationsLastHour = notificationsLastHour
        )

        // Create notification entity
        val notification = NotificationEntity(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            postedTime = postedTime,
            receivedTime = receivedTime,
            baseScore = scoreBreakdown.baseScore,
            contentScore = scoreBreakdown.contentPreferenceScore + scoreBreakdown.keywordScore,  // UPDATED
            frequencyScore = (scoreBreakdown.frequencyMultiplier * 100).toInt(),
            behaviorScore = scoreBreakdown.appBehaviorScore + scoreBreakdown.contentBehaviorScore,  // UPDATED
            finalScore = scoreBreakdown.finalScore,
            category = scoreBreakdown.category.name,
            hasActions = notificationExtractor.hasActions(sbn),
            isOngoing = sbn.isOngoing,
            priority = sbn.notification.priority,
            groupKey = sbn.groupKey
        )

        // Save notification
        val notificationId = repository.insertNotification(notification)

        // Update app behavior
        repository.incrementNotificationReceived(packageName)

        // NEW: Update content behavior if we have contentId
        if (contentId != null) {
            repository.incrementContentNotificationReceived(packageName, contentId)
        }

        return@withContext notificationId
    }
}