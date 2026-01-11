package com.notifmanager.domain.scoring

import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.data.database.entities.KeywordEntity
import com.notifmanager.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * CORE SCORING ENGINE - The brain of the app
 *
 * This calculates how important each notification is (0-100 score)
 * Combines 4 factors:
 * 1. Base app importance (which app is it from?)
 * 2. Content analysis (what does it say?)
 * 3. Frequency penalty (is app spammy?)
 * 4. User behavior (does user care about these?)
 *
 * This is where the "intelligence" happens
 */
@Singleton
class ImportanceScorer @Inject constructor() {

    /**
     * Calculate final importance score for a notification
     *
     * @param packageName Which app sent this
     * @param appName Human-readable app name
     * @param title Notification title
     * @param text Notification content
     * @param appBehavior User's behavior with this app (can be null for new apps)
     * @param customKeywords User-defined keywords
     * @param notificationsLastHour How many from this app in last hour
     *
     * @return ScoreBreakdown with all scores and final category
     */
    fun calculateScore(
        packageName: String,
        appName: String,
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        appBehavior: AppBehaviorEntity?,
        customKeywords: List<KeywordEntity>,
        notificationsLastHour: Int
    ): ScoreBreakdown {

        // Step 1: Calculate base score from app category
        val baseScore = calculateBaseScore(packageName, appBehavior)

        // Step 2: Analyze notification content
        val contentScore = analyzeContent(title, text, subText, bigText, customKeywords)

        // Step 3: Apply frequency penalty
        val frequencyMultiplier = calculateFrequencyPenalty(notificationsLastHour)

        // Step 4: Apply behavior adjustment
        val behaviorAdjustment = appBehavior?.behaviorAdjustment ?: 0

        // Combine all factors
        var finalScore = baseScore + contentScore + behaviorAdjustment
        finalScore = (finalScore * frequencyMultiplier).toInt()

        // Clamp to 0-100 range
        finalScore = max(0, min(100, finalScore))

        // Determine category based on final score
        val category = determineCategory(finalScore)

        return ScoreBreakdown(
            baseScore = baseScore,
            contentScore = contentScore,
            frequencyMultiplier = frequencyMultiplier,
            behaviorAdjustment = behaviorAdjustment,
            finalScore = finalScore,
            category = category
        )
    }

    /**
     * Calculate base importance from app category
     * Different types of apps have different default importance
     */
    private fun calculateBaseScore(packageName: String, appBehavior: AppBehaviorEntity?): Int {
        // If user set custom score, use that
        appBehavior?.customBaseScore?.let { return it }

        // Check app category and assign base weight
        return when {
            // Banking apps = highest priority
            Constants.AppCategories.BANKING.any { packageName.contains(it, ignoreCase = true) } -> {
                Constants.BASE_WEIGHT_BANKING
            }
            // Messaging apps = high priority
            Constants.AppCategories.MESSAGING.contains(packageName) -> {
                Constants.BASE_WEIGHT_MESSAGING
            }
            // Email apps = medium-high priority
            Constants.AppCategories.EMAIL.contains(packageName) -> {
                Constants.BASE_WEIGHT_EMAIL
            }
            // Social media = medium priority
            Constants.AppCategories.SOCIAL.contains(packageName) -> {
                Constants.BASE_WEIGHT_SOCIAL
            }
            // Entertainment = low-medium priority
            Constants.AppCategories.ENTERTAINMENT.contains(packageName) -> {
                Constants.BASE_WEIGHT_ENTERTAINMENT
            }
            // Games = low priority
            packageName.contains("game", ignoreCase = true) -> {
                Constants.BASE_WEIGHT_GAMES
            }
            // Unknown apps = neutral
            else -> 30
        }
    }

    /**
     * Analyze notification content for important keywords
     * This is simple pattern matching, not real AI
     * But it's surprisingly effective!
     */
    private fun analyzeContent(
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        customKeywords: List<KeywordEntity>
    ): Int {
        // Combine all text fields
        val allText = listOfNotNull(title, text, subText, bigText)
            .joinToString(" ")
            .lowercase()

        if (allText.isBlank()) return 0

        var contentBoost = 0

        // Check for critical keywords (OTP, failed, urgent, etc.)
        val criticalMatches = Constants.Keywords.CRITICAL.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += criticalMatches * 20  // +20 per critical keyword

        // Check for important keywords (message, delivery, appointment, etc.)
        val importantMatches = Constants.Keywords.IMPORTANT.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += importantMatches * 10  // +10 per important keyword

        // Check for spam keywords (sale, watch now, new video, etc.)
        val spamMatches = Constants.Keywords.SPAM.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost -= spamMatches * 8  // -8 per spam keyword

        // Check for financial keywords (bank, payment, transaction, etc.)
        val financialMatches = Constants.Keywords.FINANCIAL.count { keyword ->
            allText.contains(keyword, ignoreCase = true)
        }
        contentBoost += financialMatches * 15  // +15 per financial keyword

        // Apply custom keywords from user
        customKeywords.forEach { keyword ->
            if (allText.contains(keyword.keyword, ignoreCase = true)) {
                contentBoost += keyword.scoreModifier
            }
        }

        // Cap content boost to reasonable range (-30 to +40)
        return max(-30, min(40, contentBoost))
    }

    /**
     * Calculate frequency penalty
     * Apps that spam get heavily penalized
     */
    private fun calculateFrequencyPenalty(notificationsLastHour: Int): Float {
        return when {
            notificationsLastHour <= Constants.FREQUENCY_LOW_THRESHOLD -> Constants.PENALTY_NONE
            notificationsLastHour <= Constants.FREQUENCY_MEDIUM_THRESHOLD -> Constants.PENALTY_LOW
            notificationsLastHour <= Constants.FREQUENCY_HIGH_THRESHOLD -> Constants.PENALTY_MEDIUM
            notificationsLastHour <= 20 -> Constants.PENALTY_HIGH
            else -> Constants.PENALTY_SPAM  // 20+ notifications/hour = spam
        }
    }

    /**
     * Convert numeric score to category
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
 * Data class to hold score breakdown
 * This helps explain why a notification got its score
 */
data class ScoreBreakdown(
    val baseScore: Int,  // From app category
    val contentScore: Int,  // From keyword analysis
    val frequencyMultiplier: Float,  // Penalty for spam
    val behaviorAdjustment: Int,  // From user learning
    val finalScore: Int,  // Final calculated score
    val category: Constants.NotificationCategory  // Resulting category
)