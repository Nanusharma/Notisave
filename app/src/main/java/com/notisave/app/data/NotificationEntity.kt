package com.notisave.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a captured notification.
 *
 * Indexed on [postedAt] for fast chronological queries and on
 * [packageName] for app-filtered views.
 */
@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["postedAt"]),
        Index(value = ["packageName"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Package name of the source app (e.g. "com.whatsapp") */
    val packageName: String,

    /** Human-readable app label (e.g. "WhatsApp") */
    val appName: String,

    /** Notification title (sender name, subject, etc.) */
    val title: String?,

    /** Short notification text */
    val text: String?,

    /** Expanded big-text content, if available */
    val bigText: String?,

    /** Epoch millis when the notification was posted */
    val postedAt: Long,

    /** True if the notification was dismissed/removed from the status bar */
    val isRemoved: Boolean = false,

    /** Optional category hint (e.g. "msg", "call", "email") */
    val category: String? = null
)
