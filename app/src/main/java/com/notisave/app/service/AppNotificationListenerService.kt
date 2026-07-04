package com.notisave.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.notisave.app.R
import com.notisave.app.data.AppDatabase
import com.notisave.app.data.NotificationDao
import com.notisave.app.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Core service that intercepts all device notifications and persists them to Room.
 *
 * This service is bound by the Android system when the user grants
 * Notification Access in Settings. It runs as a foreground service
 * with a minimal-priority notification to prevent being killed.
 *
 * Key behaviors:
 * - onNotificationPosted(): instantly saves notification content to the database
 * - onNotificationRemoved(): marks the DB entry as removed (does NOT delete it)
 * - onListenerConnected(): backfills currently active notifications
 */
class AppNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotisaveListener"
        private const val CHANNEL_ID = "notisave_status_channel"
        private const val FOREGROUND_ID = 1
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dao: NotificationDao

    override fun onCreate() {
        super.onCreate()
        dao = AppDatabase.getInstance(applicationContext).notificationDao()
        startForegroundWithNotification()
        Log.d(TAG, "NotificationListenerService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // Ignore our own foreground notification to avoid infinite loops
        if (sbn.packageName == packageName) return

        val extras = sbn.notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        // Skip empty or summary-only notifications (common with grouped messages)
        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        // Resolve the human-readable app name
        val appName = resolveAppName(sbn.packageName)

        // Determine category
        val category = sbn.notification.category

        val entity = NotificationEntity(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            bigText = bigText,
            postedAt = sbn.postTime,
            isRemoved = false,
            category = category
        )

        serviceScope.launch {
            try {
                dao.insert(entity)
                Log.d(TAG, "Saved notification from ${sbn.packageName}: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName == packageName) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        serviceScope.launch {
            try {
                dao.markRemoved(sbn.packageName, title, text)
                Log.d(TAG, "Marked notification removed from ${sbn.packageName}: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark notification removed", e)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener connected — backfilling active notifications")

        // Backfill any currently active notifications on connect/reconnect
        try {
            activeNotifications?.forEach { sbn ->
                onNotificationPosted(sbn)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error backfilling active notifications", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Listener disconnected — requesting rebind")
        // Request the system to rebind this listener
        requestRebind(null)
    }

    /**
     * Starts a foreground notification with minimum priority so the service
     * stays alive without being intrusive to the user.
     */
    private fun startForegroundWithNotification() {
        val manager = getSystemService(NotificationManager::class.java)

        // Create the notification channel (required for API 26+)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .build()

        startForeground(FOREGROUND_ID, notification)
    }

    /**
     * Resolves a package name to its human-readable application label.
     * Falls back to the package name itself if resolution fails.
     */
    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "NotificationListenerService destroyed")
    }
}
