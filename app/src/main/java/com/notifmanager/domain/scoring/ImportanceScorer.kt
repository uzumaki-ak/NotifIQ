package com.notifmanager.domain.scoring

import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.data.database.entities.ContentBehaviorEntity
import com.notifmanager.data.database.entities.ContentPreferenceEntity
import com.notifmanager.data.database.entities.KeywordEntity
import com.notifmanager.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * CORE SCORING ENGINE - The brain of the app
 *
 * UPDATED: Now supports content-level scoring (channels/senders)
 */
@Singleton
class ImportanceScorer @Inject constructor() {

    /**
     * Calculate final importance score (UPDATED with content-level)
     */
    fun calculateScore(
        packageName: String,
        appName: String,
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        appBehavior: AppBehaviorEntity?,
        contentPreference: ContentPreferenceEntity?,  // NEW
        contentBehavior: ContentBehaviorEntity?,      // NEW
        customKeywords: List<KeywordEntity>,
        notificationsLastHour: Int
    ): ScoreBreakdown {

        // Step 1: Base score from app
        val baseScore = calculateBaseScore(packageName, appBehavior)

        // Step 2: Content-level adjustment (NEW - channel/sender preference)
        val contentScore = contentPreference?.preferenceScore ?: 0

        // Step 3: Content analysis (keywords)
        val keywordScore = analyzeContent(title, text, subText, bigText, customKeywords)

        // Step 4: Frequency penalty
        val frequencyMultiplier = calculateFrequencyPenalty(notificationsLastHour)

        // Step 5: App-level behavior
        val appBehaviorScore = appBehavior?.behaviorAdjustment ?: 0

        // Step 6: Content-level behavior (NEW)
        val contentBehaviorScore = contentBehavior?.behaviorScore ?: 0

        // Combine all factors (UPDATED formula)
        var finalScore = baseScore + contentScore + keywordScore + appBehaviorScore + contentBehaviorScore
        finalScore = (finalScore * frequencyMultiplier).toInt()

        // Clamp to 0-100
        finalScore = max(0, min(100, finalScore))

        // Determine category
        val category = determineCategory(finalScore)

        return ScoreBreakdown(
            baseScore = baseScore,
            contentPreferenceScore = contentScore,    // NEW
            keywordScore = keywordScore,
            frequencyMultiplier = frequencyMultiplier,
            appBehaviorScore = appBehaviorScore,
            contentBehaviorScore = contentBehaviorScore,  // NEW
            finalScore = finalScore,
            category = category
        )
    }

    /**
     * Calculate base score from app category
     */
    private fun calculateBaseScore(packageName: String, appBehavior: AppBehaviorEntity?): Int {
        appBehavior?.customBaseScore?.let { return it }

        return when {
            Constants.AppCategories.BANKING.any { packageName.contains(it, ignoreCase = true) } -> {
                Constants.BASE_WEIGHT_BANKING
            }
            Constants.AppCategories.MESSAGING.contains(packageName) -> {
                Constants.BASE_WEIGHT_MESSAGING
            }
            Constants.AppCategories.EMAIL.contains(packageName) -> {
                Constants.BASE_WEIGHT_EMAIL
            }
            Constants.AppCategories.SOCIAL.contains(packageName) -> {
                Constants.BASE_WEIGHT_SOCIAL
            }
            Constants.AppCategories.ENTERTAINMENT.contains(packageName) -> {
                Constants.BASE_WEIGHT_ENTERTAINMENT
            }
            packageName.contains("game", ignoreCase = true) -> {
                Constants.BASE_WEIGHT_GAMES
            }
            else -> 30
        }
    }

    /**
     * Analyze content for keywords
     */
    private fun analyzeContent(
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        customKeywords: List<KeywordEntity>
    ): Int {
        val allText = listOfNotNull(title, text, subText, bigText)
            .joinToString(" ")
            .lowercase()

        if (allText.isBlank()) return 0

        var contentBoost = 0

        // Critical keywords
        val criticalMatches = Constants.Keywords.CRITICAL.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += criticalMatches * 20

        // Important keywords
        val importantMatches = Constants.Keywords.IMPORTANT.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += importantMatches * 10

        // Spam keywords
        val spamMatches = Constants.Keywords.SPAM.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost -= spamMatches * 8

        // Financial keywords
        val financialMatches = Constants.Keywords.FINANCIAL.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += financialMatches * 15

        // Custom keywords
        customKeywords.forEach { keyword ->
            if (allText.contains(keyword.keyword, ignoreCase = true)) {
                contentBoost += keyword.scoreModifier
            }
        }

        return max(-30, min(40, contentBoost))
    }

    /**
     * Calculate frequency penalty
     */
    private fun calculateFrequencyPenalty(notificationsLastHour: Int): Float {
        return when {
            notificationsLastHour <= Constants.FREQUENCY_LOW_THRESHOLD -> Constants.PENALTY_NONE
            notificationsLastHour <= Constants.FREQUENCY_MEDIUM_THRESHOLD -> Constants.PENALTY_LOW
            notificationsLastHour <= Constants.FREQUENCY_HIGH_THRESHOLD -> Constants.PENALTY_MEDIUM
            notificationsLastHour <= 20 -> Constants.PENALTY_HIGH
            else -> Constants.PENALTY_SPAM
        }
    }

    /**
     * Determine category from score
     */
    private fun determineCategory(score: Int): Constants.NotificationCategory {
        return when {
            score >= Constants.SCORE_CRITICAL_MIN -> Constants.NotificationCategory.CRITICAL
            score >= Constants.SCORE_IMPORTANT_MIN -> Constants.NotificationCategory.IMPORTANT
            score >= Constants.SCORE_NORMAL_MIN -> Constants.NotificationCategory.NORMAL
            else -> Constants.NotificationCategory.SILENT
        }
    }
}

/**
 * Score breakdown (UPDATED)
 */
data class ScoreBreakdown(
    val baseScore: Int,
    val contentPreferenceScore: Int,  // NEW
    val keywordScore: Int,
    val frequencyMultiplier: Float,
    val appBehaviorScore: Int,
    val contentBehaviorScore: Int,    // NEW
    val finalScore: Int,
    val category: Constants.NotificationCategory
)