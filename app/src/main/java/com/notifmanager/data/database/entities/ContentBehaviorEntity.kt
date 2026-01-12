package com.notifmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for content-level behavior learning
 *
 * Tracks user behavior for specific channels/senders (not just apps)
 * Example: User opens 90% of "NetworkChuck" videos → behaviorScore = +15
 */
@Entity(tableName = "content_behavior")
data class ContentBehaviorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val appPackage: String,  // "com.google.android.youtube"
    val contentId: String,  // "NetworkChuck"
    val contentType: String,  // YOUTUBE_CHANNEL

    // Behavior counters
    val totalReceived: Int = 0,
    val totalOpened: Int = 0,
    val totalDismissed: Int = 0,
    val totalIgnored: Int = 0,

    // Calculated rates
    val openRate: Float = 0f,  // totalOpened / totalReceived
    val dismissRate: Float = 0f,
    val ignoreRate: Float = 0f,

    // Behavior adjustment score (-20 to +20)
    val behaviorScore: Int = 0,

    // Timestamps
    val lastNotificationTime: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Check if user engages with this content
 */
fun ContentBehaviorEntity.hasHighEngagement(): Boolean {
    return openRate > 0.7f && totalReceived > 5
}

/**
 * Check if user ignores this content
 */
fun ContentBehaviorEntity.isIgnored(): Boolean {
    return ignoreRate > 0.8f && totalReceived > 5
}