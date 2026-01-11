package com.notifmanager.domain.usecase

import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.domain.learning.BehaviorLearner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * USE CASE: Update app behavior based on user interaction
 *
 * This runs periodically to recalculate behavior adjustments
 * Makes the app continuously learn from user behavior
 */
class UpdateBehaviorUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val behaviorLearner: BehaviorLearner
) {

    /**
     * Recalculate behavior adjustment for an app
     *
     * @param packageName App to update
     */
    suspend operator fun invoke(packageName: String) = withContext(Dispatchers.IO) {
        val appBehavior = repository.getAppBehavior(packageName) ?: return@withContext

        // Recalculate rates and adjustment
        val updated = behaviorLearner.recalculateRates(appBehavior)

        // Save updated behavior
        repository.updateAppBehavior(updated)
    }

    /**
     * Update all app behaviors
     * Called by background worker
     */
    suspend fun updateAll() = withContext(Dispatchers.IO) {
        // This would get all apps and update each
        // For now, the worker will handle this
    }
}