package com.notifmanager.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
 * FIXED: Permission redirect - now properly detects when user grants permission
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workManager: WorkManager

    // Permission launcher - handles result from settings
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // User returned from settings - check if permission granted
        if (isNotificationPermissionGranted()) {
            // Permission granted - restart activity to show home screen
            recreate()
        }
    }

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
                    // Check permission status
                    val hasPermission = remember { mutableStateOf(isNotificationPermissionGranted()) }

                    // Re-check permission when activity resumes
                    LaunchedEffect(Unit) {
                        hasPermission.value = isNotificationPermissionGranted()
                    }

                    // Determine start destination
                    val startDestination = when {
                        !hasPermission.value && isFirstRun() -> Screen.Onboarding.route
                        !hasPermission.value -> Screen.Onboarding.route
                        else -> Screen.Home.route
                    }

                    AppNavigation(
                        startDestination = startDestination,
                        onRequestPermission = { openNotificationSettings() }
                    )
                }
            }
        }
    }

    /**
     * Open notification listener settings and wait for result
     */
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        permissionLauncher.launch(intent)
    }

    /**
     * Check if notification listener permission is granted
     */
    private fun isNotificationPermissionGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
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