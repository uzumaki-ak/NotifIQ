package com.notifmanager.utils

import android.service.notification.StatusBarNotification
import com.notifmanager.utils.Constants.ContentType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Content Extractor - Extracts channel/sender info from notifications
 *
 * THIS IS THE KEY - Parses notification text to identify specific content sources
 */
@Singleton
class ContentExtractor @Inject constructor() {

    /**
     * Extract content ID and type from notification
     * Returns Pair(contentId, contentType)
     */
    fun extractContent(packageName: String, title: String?, text: String?): Pair<String?, ContentType> {
        if (title == null && text == null) return Pair(null, ContentType.GENERIC)

        return when {
            // YouTube - extract channel name
            packageName == "com.google.android.youtube" -> {
                extractYouTubeChannel(title, text)
            }

            // WhatsApp - extract contact/group name
            packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b" -> {
                extractWhatsAppSender(title)
            }

            // Instagram - extract username
            packageName == "com.instagram.android" -> {
                extractInstagramAccount(title, text)
            }

            // Twitter - extract username
            packageName == "com.twitter.android" -> {
                extractTwitterAccount(title, text)
            }

            // Email - extract sender
            packageName.contains("mail") || packageName.contains("gmail") -> {
                extractEmailSender(title, text)
            }

            // SMS - extract sender
            packageName.contains("messaging") || packageName.contains("sms") -> {
                extractSmsSender(title)
            }

            // Generic - no specific extraction
            else -> Pair(null, ContentType.GENERIC)
        }
    }

    /**
     * Extract YouTube channel name
     * Patterns:
     * - "New video from NetworkChuck"
     * - "NetworkChuck uploaded: Title"
     * - "NetworkChuck posted a video"
     */
    private fun extractYouTubeChannel(title: String?, text: String?): Pair<String?, ContentType> {
        val combined = "$title $text"

        // Pattern 1: "New video from {channel}"
        var regex = "(?:New video from|uploaded by)\\s+(.+?)(?:\\:|$)".toRegex(RegexOption.IGNORE_CASE)
        var match = regex.find(combined)
        if (match != null) {
            val channel = match.groupValues[1].trim()
            return Pair(channel, ContentType.YOUTUBE_CHANNEL)
        }

        // Pattern 2: "{channel} uploaded:" or "{channel} posted"
        regex = "^(.+?)\\s+(?:uploaded|posted|shared)".toRegex(RegexOption.IGNORE_CASE)
        match = regex.find(title ?: "")
        if (match != null) {
            val channel = match.groupValues[1].trim()
            return Pair(channel, ContentType.YOUTUBE_CHANNEL)
        }

        // Fallback: Use first part of title before ":"
        if (title != null && title.contains(":")) {
            val channel = title.split(":")[0].trim()
            if (channel.isNotBlank()) {
                return Pair(channel, ContentType.YOUTUBE_CHANNEL)
            }
        }

        return Pair(null, ContentType.YOUTUBE_CHANNEL)
    }

    /**
     * Extract WhatsApp sender/group name
     * Title format: "Rahul Kumar" or "Tech Group (5 messages)"
     */
    private fun extractWhatsAppSender(title: String?): Pair<String?, ContentType> {
        if (title == null) return Pair(null, ContentType.WHATSAPP_CONTACT)

        // Remove message count: "Tech Group (5 messages)" → "Tech Group"
        val sender = title.split("(")[0].trim()

        // Detect if group (usually longer names or contains "Group")
        val isGroup = sender.contains("group", ignoreCase = true) || sender.length > 20
        val type = if (isGroup) ContentType.WHATSAPP_GROUP else ContentType.WHATSAPP_CONTACT

        return Pair(sender, type)
    }

    /**
     * Extract Instagram account
     * Patterns:
     * - "@username liked your photo"
     * - "username started following you"
     */
    private fun extractInstagramAccount(title: String?, text: String?): Pair<String?, ContentType> {
        val combined = "$title $text"

        // Pattern: @username or username at start
        val regex = "(?:@)?([a-zA-Z0-9._]+)\\s+(?:liked|commented|followed|mentioned|tagged)".toRegex()
        val match = regex.find(combined)

        if (match != null) {
            val username = match.groupValues[1].trim()
            return Pair(username, ContentType.INSTAGRAM_ACCOUNT)
        }

        return Pair(null, ContentType.INSTAGRAM_ACCOUNT)
    }

    /**
     * Extract Twitter account
     * Similar to Instagram
     */
    private fun extractTwitterAccount(title: String?, text: String?): Pair<String?, ContentType> {
        val combined = "$title $text"

        val regex = "(?:@)?([a-zA-Z0-9_]+)\\s+(?:retweeted|liked|replied|mentioned)".toRegex()
        val match = regex.find(combined)

        if (match != null) {
            val username = match.groupValues[1].trim()
            return Pair(username, ContentType.TWITTER_ACCOUNT)
        }

        return Pair(null, ContentType.TWITTER_ACCOUNT)
    }

    /**
     * Extract email sender
     * Title often contains sender name or email
     */
    private fun extractEmailSender(title: String?, text: String?): Pair<String?, ContentType> {
        if (title == null) return Pair(null, ContentType.EMAIL_SENDER)

        // Extract email if present
        val emailRegex = "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})".toRegex()
        val emailMatch = emailRegex.find(title)
        if (emailMatch != null) {
            return Pair(emailMatch.groupValues[1], ContentType.EMAIL_SENDER)
        }

        // Otherwise use title as sender name
        return Pair(title.trim(), ContentType.EMAIL_SENDER)
    }

    /**
     * Extract SMS sender
     * Title usually contains sender name or number
     */
    private fun extractSmsSender(title: String?): Pair<String?, ContentType> {
        if (title == null) return Pair(null, ContentType.SMS_SENDER)

        // Remove "New message from " prefix if present
        var sender = title.replace("New message from ", "", ignoreCase = true)
        sender = sender.replace("SMS from ", "", ignoreCase = true)
        sender = sender.trim()

        return Pair(sender, ContentType.SMS_SENDER)
    }
}