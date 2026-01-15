package com.notifmanager.domain.usecase




import android.content.Context
import com.notifmanager.data.api.LLMClient
import com.notifmanager.data.models.LLMProvider
import com.notifmanager.data.models.LLMRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import android.service.notification.StatusBarNotification
import com.notifmanager.data.database.entities.ContentBehaviorEntity
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.domain.scoring.ImportanceScorer
import com.notifmanager.utils.Constants
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
    private val contentExtractor: ContentExtractor,
    private val llmClient: LLMClient,  // ADD THIS
    @ApplicationContext private val context: Context  // ADD THIS
) {

    suspend operator fun invoke(sbn: StatusBarNotification): Long = withContext(Dispatchers.IO) {
        val packageName = sbn.packageName
        val appName = notificationExtractor.getAppName(sbn)
        val title = notificationExtractor.getTitle(sbn)
        val text = notificationExtractor.getText(sbn)
        val subText = notificationExtractor.getSubText(sbn)
        val bigText = notificationExtractor.getBigText(sbn)
        val postedTime = sbn.postTime
        val receivedTime = System.currentTimeMillis()

        // Extract content (channel/sender)
        val (contentId, contentType) = contentExtractor.extractContent(packageName, title, text)

        // Get behaviors
        val appBehavior = repository.getOrCreateAppBehavior(packageName)

        val contentPreference = if (contentId != null) {
            repository.getOrCreateContentPreference(packageName, contentId, contentType.name)
        } else null

        val contentBehavior = if (contentId != null) {
            repository.getOrCreateContentBehavior(packageName, contentId, contentType.name)
        } else null

        val customKeywords = repository.getAllKeywordsList()
        val notificationsLastHour = appBehavior.notificationsLastHour

        // Calculate score
        val scoreBreakdown = scorer.calculateScore(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            appBehavior = appBehavior,
            contentPreference = contentPreference,
            contentBehavior = contentBehavior,
            customKeywords = customKeywords,
            notificationsLastHour = notificationsLastHour
        )

        // NEW: Use LLM if enabled and configured
        var finalScore = scoreBreakdown.finalScore
        val prefs = context.getSharedPreferences("llm_prefs", Context.MODE_PRIVATE)
        val llmEnabled = prefs.getBoolean("llm_enabled", false)

        if (llmEnabled && contentId != null) {
            try {
                val providerName = prefs.getString("llm_provider", LLMProvider.EURON.name)!!
                val provider = LLMProvider.valueOf(providerName)
                val apiKey = prefs.getString("api_key_${provider.name}", "")

                if (!apiKey.isNullOrBlank()) {
                    // Get user preferences for this app
                    val userPrefs = "User likes: ${contentPreference?.preferenceScore ?: 0} for $contentId"

                    val llmRequest = LLMRequest(
                        notificationText = "$title - $text",
                        channelName = contentId,
                        userPreferences = userPrefs
                    )

                    val llmResponse = llmClient.classifyNotification(llmRequest, provider, apiKey)

                    // Adjust score based on LLM response
                    if (llmResponse.shouldBeImportant && llmResponse.confidence > 0.7f) {
                        finalScore = maxOf(finalScore + 15, 70)  // Boost to at least Important
                    } else if (!llmResponse.shouldBeImportant && llmResponse.confidence > 0.7f) {
                        finalScore = minOf(finalScore - 15, 30)  // Reduce
                    }

                    println("LLM says: ${llmResponse.reason} (confidence: ${llmResponse.confidence})")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Silently fail - use original score
            }
        }

        // Use finalScore (either LLM-adjusted or original)
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
            contentScore = scoreBreakdown.contentPreferenceScore + scoreBreakdown.keywordScore,
            frequencyScore = (scoreBreakdown.frequencyMultiplier * 100).toInt(),
            behaviorScore = scoreBreakdown.appBehaviorScore + scoreBreakdown.contentBehaviorScore,
            finalScore = finalScore,
            category = determineCategory(finalScore).name,
            hasActions = notificationExtractor.hasActions(sbn),
            isOngoing = sbn.isOngoing,
            priority = sbn.notification.priority,
            groupKey = sbn.groupKey,
            contentId = contentId,      // SAVE THIS
            contentType = contentType.name  // SAVE THIS
        )

        val notificationId = repository.insertNotification(notification)

        repository.incrementNotificationReceived(packageName)

        if (contentId != null) {
            repository.incrementContentNotificationReceived(packageName, contentId)
        }

        return@withContext notificationId
    }

    private fun determineCategory(score: Int): Constants.NotificationCategory {
        return when {
            score >= 70 -> Constants.NotificationCategory.CRITICAL
            score >= 40 -> Constants.NotificationCategory.IMPORTANT
            score >= 15 -> Constants.NotificationCategory.NORMAL
            else -> Constants.NotificationCategory.SILENT
        }
    }
}
