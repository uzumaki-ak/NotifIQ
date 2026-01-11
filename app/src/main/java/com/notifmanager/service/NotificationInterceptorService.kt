package com.notifmanager.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notifmanager.domain.usecase.ProcessNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NOTIFICATION LISTENER SERVICE - Core of the app
 *
 * This service runs in background and intercepts ALL notifications
 * Android calls our methods whenever a notification is posted/removed
 *
 * This is how we get access to notification data
 *
 * IMPORTANT: User must grant notification access in settings
 */
@AndroidEntryPoint
class NotificationInterceptorService : NotificationListenerService() {

    @Inject
    lateinit var processNotificationUseCase: ProcessNotificationUseCase

    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when service is connected
     * This means user has granted notification access
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        // Service is ready to receive notifications
    }

    /**
     * Called when a new notification is posted
     * This is where the magic starts!
     *
     * @param sbn The notification that was just posted
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        // Filter out our own notifications (avoid recursion)
        if (sbn.packageName == packageName) return

        // Filter out system UI notifications (status bar, etc.)
        if (sbn.packageName == "android" || sbn.packageName == "com.android.systemui") {
            return
        }

        // Process notification asynchronously
        serviceScope.launch {
            try {
                processNotificationUseCase(sbn)
            } catch (e: Exception) {
                // Log error but don't crash service
                e.printStackTrace()
            }
        }
    }

    /**
     * Called when a notification is removed (dismissed)
     * We can track this for behavior learning
     *
     * @param sbn The notification that was removed
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        if (sbn == null) return

        // TODO: Mark notification as inactive in database
        // This helps track which notifications were dismissed vs kept
    }

    /**
     * Called when service is disconnected
     * Usually happens when user revokes permission
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Service stopped working
    }
}