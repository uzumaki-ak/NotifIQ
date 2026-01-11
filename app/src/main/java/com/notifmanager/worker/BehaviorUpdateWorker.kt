package com.notifmanager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.domain.learning.BehaviorLearner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * BACKGROUND WORKER - Update behavior learning
 *
 * Runs periodically (every 6 hours) to:
 * 1. Recalculate behavior adjustments for all apps
 * 2. Update frequency metrics
 *
 * This keeps the learning system up to date
 */
@HiltWorker
class BehaviorUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NotificationRepository,
    private val behaviorLearner: BehaviorLearner
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get all app behaviors
            val allBehaviors = repository.getAllAppBehaviors().first()

            // Update each one
            allBehaviors.forEach { appBehavior ->
                val updated = behaviorLearner.recalculateRates(appBehavior)
                repository.updateAppBehavior(updated)
            }

            println("Behavior update completed for ${allBehaviors.size} apps")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}