package com.notifmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notifmanager.utils.Constants

/**
 * Database entity for content-level preferences
 *
 * Stores user's manual preferences for specific channels/senders
 * Example: "NetworkChuck" channel → Important (+20)
 */
@Entity(tableName = "content_preferences")
data class ContentPreferenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val appPackage: String,  // "com.google.android.youtube"
    val contentId: String,  // "NetworkChuck", "Rahul Kumar", etc.
    val contentType: String,  // YOUTUBE_CHANNEL, WHATSAPP_CONTACT, etc.

    // User preference (-20 to +20)
    val preferenceScore: Int = 0,  // -20 = Silent, 0 = Neutral, +20 = Important

    // Is this manually locked by user?
    val isLocked: Boolean = false,  // If true, don't auto-adjust

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Extension to get content type enum
 */
fun ContentPreferenceEntity.getContentTypeEnum(): Constants.ContentType {
    return try {
        Constants.ContentType.valueOf(contentType)
    } catch (e: Exception) {
        Constants.ContentType.GENERIC
    }
}