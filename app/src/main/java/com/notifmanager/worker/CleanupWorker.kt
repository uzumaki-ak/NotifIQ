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
 * Runs daily to:
 * 1. Delete notifications older than 30 days
 * 2. Delete silent notifications older than 7 days
 * 3. Cleanup old app behavior data
 *
 * Keeps database size under control
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NotificationRepository
) : CoroutineWorker(context, params) {

    /**
     * Main work method - executed in background
     */
    override suspend fun doWork(): Result {
        return try {
            // Delete old notifications (30+ days)
            val deletedOld = repository.cleanupOldNotifications(
                Constants.CLEANUP_OLD_NOTIFICATIONS_DAYS
            )

            // Delete old silent notifications (7+ days)
            val deletedSilent = repository.cleanupSilentNotifications(
                Constants.CLEANUP_SILENT_NOTIFICATIONS_DAYS
            )

            // Log results
            println("Cleanup completed: $deletedOld old, $deletedSilent silent notifications deleted")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()  // Retry if cleanup fails
        }
    }
}