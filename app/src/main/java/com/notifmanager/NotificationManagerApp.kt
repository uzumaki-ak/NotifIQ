package com.notifmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class - Entry point of the app
 * Initializes Hilt dependency injection and WorkManager
 */
@HiltAndroidApp
class NotificationManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // App initialization happens here
        // Hilt will automatically inject dependencies
    }

    /**
     * Provides WorkManager configuration for background tasks
     * Uses Hilt's worker factory for dependency injection in workers
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}