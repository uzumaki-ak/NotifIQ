package com.notifmanager.data.database.entities
import com.notifmanager.utils.Constants

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notifmanager.utils.Constants.NotificationCategory

/**
 * Database entity for storing notification data
 * Each row represents one notification that was received
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic notification info
    val packageName: String,  // Which app sent this (e.g., "com.whatsapp")
    val appName: String,  // Human-readable app name (e.g., "WhatsApp")
    val title: String?,  // Notification title
    val text: String?,  // Notification content text
    val subText: String?,  // Additional text (optional)
    val bigText: String?,  // Expanded notification text (optional)

    // Timestamps
    val postedTime: Long,  // When notification was posted (System.currentTimeMillis())
    val receivedTime: Long,  // When our app intercepted it

    // Scoring and categorization
    val baseScore: Int,  // Initial score from app category (0-100)
    val contentScore: Int,  // Score from keyword analysis (0-100)
    val frequencyScore: Int,  // Score after frequency penalty (0-100)
    val behaviorScore: Int,  // Score from user behavior learning (0-100)
    val finalScore: Int,  // Final calculated score (0-100)
    val category: String,  // CRITICAL, IMPORTANT, NORMAL, or SILENT

    // NEW: Content identification
    val contentId: String? = null,        // Channel/sender name
    val contentType: String? = null,      // YOUTUBE_CHANNEL, WHATSAPP_CONTACT, etc.


    // User interaction tracking
    val isOpened: Boolean = false,  // Did user open/click notification?
    val isDismissed: Boolean = false,  // Did user dismiss it?
    val openedAt: Long? = null,  // When was it opened?
    val dismissedAt: Long? = null,  // When was it dismissed?
    val timeToAction: Long? = null,  // Time between received and action (milliseconds)

    // Metadata
    val hasActions: Boolean = false,  // Does notification have action buttons?
    val isOngoing: Boolean = false,  // Is it an ongoing notification (music, download)?
    val priority: Int = 0,  // System priority (PRIORITY_DEFAULT, PRIORITY_HIGH, etc.)
    val groupKey: String? = null,  // If notification is part of a group

    // Status
    val isActive: Boolean = true,  // Is notification still active or was it cleared?
    val isDeleted: Boolean = false  // Soft delete flag
)

/**
 * Extension function to convert category string to enum
 * Makes it easier to work with categories in code
 */
fun NotificationEntity.getCategoryEnum(): Constants.NotificationCategory {
    return when (category) {
        "CRITICAL" -> Constants.NotificationCategory.CRITICAL
        "IMPORTANT" -> Constants.NotificationCategory.IMPORTANT
        "NORMAL" -> Constants.NotificationCategory.NORMAL
        else -> Constants.NotificationCategory.SILENT
    }
}