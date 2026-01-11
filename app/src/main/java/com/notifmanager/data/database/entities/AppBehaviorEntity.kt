package com.notifmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores learned behavior for each app
 * This is how the app "learns" what's important to the user
 *
 * Example: If user always opens WhatsApp notifications but ignores YouTube,
 * WhatsApp's behaviorAdjustment will be positive, YouTube's will be negative
 */
@Entity(tableName = "app_behavior")
data class AppBehaviorEntity(
    @PrimaryKey
    val packageName: String,  // Unique identifier for the app

    // Counters for user actions
    val totalReceived: Int = 0,  // How many notifications from this app
    val totalOpened: Int = 0,  // How many times user opened notifications
    val totalDismissed: Int = 0,  // How many times dismissed quickly
    val totalIgnored: Int = 0,  // How many times completely ignored

    // Calculated metrics
    val openRate: Float = 0f,  // totalOpened / totalReceived (0.0 to 1.0)
    val dismissRate: Float = 0f,  // totalDismissed / totalReceived
    val ignoreRate: Float = 0f,  // totalIgnored / totalReceived

    // Learning adjustment (-20 to +20 points added to base score)
    val behaviorAdjustment: Int = 0,  // Calculated from open/dismiss/ignore rates

    // Frequency tracking
    val notificationsLastHour: Int = 0,  // How many in last 60 minutes
    val notificationsLastDay: Int = 0,  // How many in last 24 hours
    val averagePerDay: Float = 0f,  // Average notifications per day

    // Timestamps
    val lastNotificationTime: Long = 0,  // When was last notification received
    val lastUpdated: Long = System.currentTimeMillis(),  // When was this record updated

    // User preferences (can be manually set)
    val isLocked: Boolean = false,  // If true, ignore learning (user set fixed priority)
    val lockedCategory: String? = null,  // If locked, force this category
    val customBaseScore: Int? = null  // If set, override default base score
)

/**
 * Extension function to calculate if app is spammy
 * Helps identify apps that send too many notifications
 */
fun AppBehaviorEntity.isSpammy(): Boolean {
    return notificationsLastHour > 10 || averagePerDay > 30
}

/**
 * Extension function to check if user engages with this app
 * High engagement = user opens notifications frequently
 */
fun AppBehaviorEntity.hasHighEngagement(): Boolean {
    return openRate > 0.7f && totalReceived > 5
}