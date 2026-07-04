package com.notisave.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides a clean API for notification data access.
 * Abstracts the DAO so ViewModels don't depend on Room directly.
 */
class NotificationRepository(context: Context) {

    private val dao: NotificationDao = AppDatabase.getInstance(context).notificationDao()

    fun getAllNotifications(): Flow<List<NotificationEntity>> = dao.getAll()

    fun getNotificationsByApp(packageName: String): Flow<List<NotificationEntity>> =
        dao.getByApp(packageName)

    fun searchNotifications(query: String): Flow<List<NotificationEntity>> =
        dao.search(query)

    fun getDistinctApps(): Flow<List<AppInfo>> = dao.getDistinctApps()

    fun getNotificationCount(): Flow<Int> = dao.getCount()

    suspend fun insert(notification: NotificationEntity): Long =
        dao.insert(notification)

    suspend fun markRemoved(packageName: String, title: String?, text: String?) =
        dao.markRemoved(packageName, title, text)

    suspend fun deleteOlderThan(cutoffMillis: Long) =
        dao.deleteOlderThan(cutoffMillis)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
