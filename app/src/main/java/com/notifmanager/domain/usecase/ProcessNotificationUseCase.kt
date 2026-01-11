package com.notifmanager.domain.usecase

import android.service.notification.StatusBarNotification
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.domain.scoring.ImportanceScorer
import com.notifmanager.utils.NotificationExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * USE CASE: Process incoming notification
 *
 * This is the main workflow when a new notification arrives:
 * 1. Extract data from system notification
 * 2. Get app behavior data
 * 3. Calculate importance score
 * 4. Save to database
 * 5. Update frequency metrics
 *
 * Use cases encapsulate business logic - keeps code organized
 */
class ProcessNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val scorer: ImportanceScorer,
    private val extractor: NotificationExtractor
) {

    /**
     * Process a newly received notification
     *
     * @param sbn StatusBarNotification from Android system
     * @return Notification ID (database row ID)
     */
    suspend operator fun invoke(sbn: StatusBarNotification): Long = withContext(Dispatchers.IO) {
        // Extract all data from system notification
        val packageName = sbn.packageName
        val appName = extractor.getAppName(sbn)
        val title = extractor.getTitle(sbn)
        val text = extractor.getText(sbn)
        val subText = extractor.getSubText(sbn)
        val bigText = extractor.getBigText(sbn)
        val postedTime = sbn.postTime
        val receivedTime = System.currentTimeMillis()

        // Get or create behavior data for this app
        val appBehavior = repository.getOrCreateAppBehavior(packageName)

        // Get custom keywords for scoring
        val customKeywords = repository.getAllKeywordsList()

        // Calculate how many notifications from this app in last hour
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        // This would need a DAO query, for now we use behavior data
        val notificationsLastHour = appBehavior.notificationsLastHour

        // CALCULATE IMPORTANCE SCORE (the magic happens here)
        val scoreBreakdown = scorer.calculateScore(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            appBehavior = appBehavior,
            customKeywords = customKeywords,
            notificationsLastHour = notificationsLastHour
        )

        // Create notification entity to save
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
            contentScore = scoreBreakdown.contentScore,
            frequencyScore = (scoreBreakdown.frequencyMultiplier * 100).toInt(),
            behaviorScore = scoreBreakdown.behaviorAdjustment,
            finalScore = scoreBreakdown.finalScore,
            category = scoreBreakdown.category.name,
            hasActions = extractor.hasActions(sbn),
            isOngoing = sbn.isOngoing,
            priority = sbn.notification.priority,
            groupKey = sbn.groupKey
        )

        // Save to database
        val notificationId = repository.insertNotification(notification)

        // Update app behavior - increment received count
        repository.incrementNotificationReceived(packageName)

        return@withContext notificationId
    }
}