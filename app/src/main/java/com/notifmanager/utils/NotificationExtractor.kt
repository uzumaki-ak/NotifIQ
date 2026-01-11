package com.notifmanager.utils

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to extract data from Android notifications
 * System notifications are complex - this makes it easier
 */
@Singleton
class NotificationExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Get human-readable app name from package
     */
    fun getAppName(sbn: StatusBarNotification): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName  // Fallback to package name
        }
    }

    /**
     * Extract notification title
     */
    fun getTitle(sbn: StatusBarNotification): String? {
        return sbn.notification.extras.getString(Notification.EXTRA_TITLE)
    }

    /**
     * Extract notification text
     */
    fun getText(sbn: StatusBarNotification): String? {
        return sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
    }

    /**
     * Extract sub text (optional additional text)
     */
    fun getSubText(sbn: StatusBarNotification): String? {
        return sbn.notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
    }

    /**
     * Extract big text (expanded notification content)
     */
    fun getBigText(sbn: StatusBarNotification): String? {
        return sbn.notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
    }

    /**
     * Check if notification has action buttons
     */
    fun hasActions(sbn: StatusBarNotification): Boolean {
        return sbn.notification.actions?.isNotEmpty() == true
    }

    /**
     * Get all text combined (for search/analysis)
     */
    fun getAllText(sbn: StatusBarNotification): String {
        val title = getTitle(sbn)
        val text = getText(sbn)
        val subText = getSubText(sbn)
        val bigText = getBigText(sbn)

        return listOfNotNull(title, text, subText, bigText)
            .joinToString(" ")
    }
}