package com.notifmanager.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.work.*
import com.notifmanager.presentation.ui.navigation.AppNavigation
import com.notifmanager.presentation.ui.navigation.Screen
import com.notifmanager.presentation.ui.theme.IntelligentNotificationManagerTheme
import com.notifmanager.utils.Constants
import com.notifmanager.worker.BehaviorUpdateWorker
import com.notifmanager.worker.CleanupWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * MAIN ACTIVITY - App entry point
 *
 * Sets up:
 * 1. UI theme
 * 2. Navigation
 * 3. Background workers
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Setup background workers
        setupBackgroundWorkers()

        // Set content
        setContent {
            IntelligentNotificationManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Check if first run
                    val startDestination = if (isFirstRun()) {
                        Screen.Onboarding.route
                    } else {
                        Screen.Home.route
                    }

                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }

    /**
     * Setup periodic background workers
     */
    private fun setupBackgroundWorkers() {
        // Daily cleanup worker
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(Constants.WORK_TAG_CLEANUP)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_TAG_CLEANUP,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )

        // Behavior update worker (every 6 hours)
        val behaviorRequest = PeriodicWorkRequestBuilder<BehaviorUpdateWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(Constants.WORK_TAG_BEHAVIOR_UPDATE)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_TAG_BEHAVIOR_UPDATE,
            ExistingPeriodicWorkPolicy.KEEP,
            behaviorRequest
        )
    }

    /**
     * Check if this is first app run
     */
    private fun isFirstRun(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean(Constants.PrefsKeys.FIRST_RUN, true)

        if (isFirstRun) {
            prefs.edit().putBoolean(Constants.PrefsKeys.FIRST_RUN, false).apply()
        }

        return isFirstRun
    }
}