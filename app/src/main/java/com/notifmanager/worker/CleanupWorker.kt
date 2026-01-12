package com.notifmanager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * BACKGROUND WORKER - Cleanup old notifications
 *
 * UPDATED: Auto-delete after 2 days (configurable)
 * Runs daily to:
 * 1. Delete notifications older than 30 days
 * 2. Delete silent notifications older than 2 days (NEW)
 * 3. Cleanup old app behavior data
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get auto-delete setting (default 2 days)
            val autoDeleteDays = getAutoDeleteDays()

            // Delete old notifications (30+ days)
            val deletedOld = repository.cleanupOldNotifications(
                Constants.CLEANUP_OLD_NOTIFICATIONS_DAYS
            )

            // Delete old silent notifications (2+ days by default)
            val deletedSilent = repository.cleanupSilentNotifications(autoDeleteDays)

            // Delete normal/important notifications older than auto-delete setting
            val deletedAuto = repository.cleanupOlderThan(autoDeleteDays)

            // Log results
            println("Cleanup completed: $deletedOld old, $deletedSilent silent, $deletedAuto auto-deleted")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Get auto-delete days from SharedPreferences
     */
    private fun getAutoDeleteDays(): Int {
        val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getInt(Constants.PrefsKeys.AUTO_DELETE_DAYS, 2) // Default 2 days
    }
}