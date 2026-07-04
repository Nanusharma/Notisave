package com.notisave.app.worker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notisave.app.data.AppDatabase
import com.notisave.app.service.AppNotificationListenerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Periodic worker that runs every 6 hours to:
 * 1. Check if the NotificationListenerService is still bound and attempt rebind if not
 * 2. Clean up old notifications based on the user's retention preference
 */
class ListenerHealthWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ListenerHealthWorker"
        const val WORK_NAME = "listener_health_check"
        private const val DEFAULT_RETENTION_DAYS = 30
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Running health check")

        // 1. Check listener status and attempt rebind if disconnected
        checkListenerStatus()

        // 2. Clean up old notifications
        cleanupOldNotifications()

        return Result.success()
    }

    private fun checkListenerStatus() {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""

        if (!enabledListeners.contains(context.packageName)) {
            Log.w(TAG, "Listener not in enabled list — user may need to re-enable")
            return
        }

        // Force a component toggle to ensure fresh bind
        try {
            val pm = context.packageManager
            val component = ComponentName(context, AppNotificationListenerService::class.java)

            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.d(TAG, "Listener component toggled for rebind")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle listener component", e)
        }
    }

    private suspend fun cleanupOldNotifications() {
        try {
            val dao = AppDatabase.getInstance(context).notificationDao()

            // Default to 30 days retention
            val retentionDays = DEFAULT_RETENTION_DAYS
            if (retentionDays <= 0) return // 0 or negative means keep forever

            val cutoff = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L
            dao.deleteOlderThan(cutoff)
            Log.d(TAG, "Cleaned up notifications older than $retentionDays days")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old notifications", e)
        }
    }
}
