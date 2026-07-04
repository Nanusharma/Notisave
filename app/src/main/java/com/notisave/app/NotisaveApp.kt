package com.notisave.app

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notisave.app.worker.ListenerHealthWorker
import java.util.concurrent.TimeUnit

/**
 * Custom Application class that initializes the periodic health check
 * WorkManager task on app start.
 */
class NotisaveApp : Application() {

    companion object {
        private const val TAG = "NotisaveApp"
    }

    override fun onCreate() {
        super.onCreate()
        scheduleHealthCheck()
        Log.d(TAG, "Notisave application initialized")
    }

    /**
     * Schedules a periodic health check every 6 hours.
     * Uses KEEP policy so it doesn't reset the schedule on every app launch.
     */
    private fun scheduleHealthCheck() {
        val healthCheckRequest = PeriodicWorkRequestBuilder<ListenerHealthWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES // flex interval
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ListenerHealthWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            healthCheckRequest
        )

        Log.d(TAG, "Health check worker scheduled (every 6 hours)")
    }
}
