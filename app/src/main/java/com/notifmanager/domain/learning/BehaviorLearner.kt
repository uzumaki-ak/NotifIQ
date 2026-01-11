package com.notifmanager.domain.learning

import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * BEHAVIOR LEARNING ENGINE
 *
 * This makes the app "learn" from user behavior
 * Over time, it figures out which apps the user actually cares about
 *
 * The "ML" part (it's not real ML, just smart math)
 */
@Singleton
class BehaviorLearner @Inject constructor() {

    /**
     * Calculate behavior adjustment score for an app
     * This gets added to the base score to personalize importance
     *
     * Logic:
     * - High open rate = user cares, boost score
     * - High dismiss rate = user doesn't care, reduce score
     * - High ignore rate = user doesn't care, reduce score
     *
     * @param appBehavior Current behavior data for the app
     * @return Adjustment score (-20 to +20)
     */
    fun calculateBehaviorAdjustment(appBehavior: AppBehaviorEntity): Int {
        // Don't adjust if not enough data (need at least 5 notifications)
        if (appBehavior.totalReceived < 5) return 0

        val openRate = appBehavior.openRate
        val dismissRate = appBehavior.dismissRate
        val ignoreRate = appBehavior.ignoreRate

        var adjustment = 0

        // Positive adjustments (user engages with these notifications)
        when {
            openRate > 0.8f -> adjustment += 15  // Opens almost always
            openRate > 0.6f -> adjustment += 10  // Opens frequently
            openRate > 0.4f -> adjustment += 5   // Opens sometimes
        }

        // Negative adjustments (user ignores/dismisses)
        when {
            dismissRate > 0.6f -> adjustment -= 12  // Dismisses frequently
            dismissRate > 0.4f -> adjustment -= 8   // Dismisses sometimes
        }

        when {
            ignoreRate > 0.7f -> adjustment -= 15  // Ignores most
            ignoreRate > 0.5f -> adjustment -= 10  // Ignores many
            ignoreRate > 0.3f -> adjustment -= 5   // Ignores some
        }

        // Clamp to valid range
        adjustment = max(-Constants.BEHAVIOR_MAX_ADJUSTMENT, min(Constants.BEHAVIOR_MAX_ADJUSTMENT, adjustment))

        return adjustment
    }

    /**
     * Recalculate all rates for an app
     * Called periodically to update learning
     */
    fun recalculateRates(appBehavior: AppBehaviorEntity): AppBehaviorEntity {
        val total = appBehavior.totalReceived

        if (total == 0) return appBehavior

        val newOpenRate = appBehavior.totalOpened.toFloat() / total
        val newDismissRate = appBehavior.totalDismissed.toFloat() / total
        val newIgnoreRate = appBehavior.totalIgnored.toFloat() / total

        // Calculate new behavior adjustment
        val newAdjustment = calculateBehaviorAdjustment(
            appBehavior.copy(
                openRate = newOpenRate,
                dismissRate = newDismissRate,
                ignoreRate = newIgnoreRate
            )
        )

        return appBehavior.copy(
            openRate = newOpenRate,
            dismissRate = newDismissRate,
            ignoreRate = newIgnoreRate,
            behaviorAdjustment = newAdjustment,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Check if behavior should trigger auto-silence
     * If user consistently ignores an app, suggest silencing it
     */
    fun shouldSuggestAutoSilence(appBehavior: AppBehaviorEntity): Boolean {
        // Need enough data to be confident
        if (appBehavior.totalReceived < 10) return false

        // If user ignores 80%+ and never opens, suggest silence
        return appBehavior.ignoreRate > 0.8f && appBehavior.openRate < 0.1f
    }

    /**
     * Check if behavior shows high importance
     * Used to suggest upgrading app priority
     */
    fun shouldSuggestUpgrade(appBehavior: AppBehaviorEntity): Boolean {
        // Need enough data
        if (appBehavior.totalReceived < 10) return false

        // If user opens 70%+ of notifications, they care
        return appBehavior.openRate > 0.7f
    }

    /**
     * Generate explanation for why app has its current adjustment
     * This is shown to user for transparency
     */
    fun explainAdjustment(appBehavior: AppBehaviorEntity): String {
        if (appBehavior.totalReceived < 5) {
            return "Not enough data yet to learn your preferences"
        }

        return when {
            appBehavior.openRate > 0.7f -> {
                "You open these notifications frequently (${(appBehavior.openRate * 100).toInt()}%)"
            }
            appBehavior.dismissRate > 0.6f -> {
                "You quickly dismiss these notifications (${(appBehavior.dismissRate * 100).toInt()}%)"
            }
            appBehavior.ignoreRate > 0.7f -> {
                "You rarely interact with these notifications (${(appBehavior.ignoreRate * 100).toInt()}% ignored)"
            }
            appBehavior.behaviorAdjustment > 0 -> {
                "You seem to find these notifications useful"
            }
            appBehavior.behaviorAdjustment < 0 -> {
                "You seem to find these notifications less important"
            }
            else -> {
                "Still learning your preferences for this app"
            }
        }
    }
}