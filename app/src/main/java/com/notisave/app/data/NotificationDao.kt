package com.notisave.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("""
        UPDATE notifications 
        SET isRemoved = 1 
        WHERE packageName = :pkg 
          AND title = :title 
          AND text = :text 
          AND isRemoved = 0
    """)
    suspend fun markRemoved(pkg: String, title: String?, text: String?)

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :pkg ORDER BY postedAt DESC")
    fun getByApp(pkg: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications 
        WHERE (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR bigText LIKE '%' || :query || '%')
        ORDER BY postedAt DESC
    """)
    fun search(query: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT DISTINCT packageName, appName 
        FROM notifications 
        ORDER BY appName ASC
    """)
    fun getDistinctApps(): Flow<List<AppInfo>>

    @Query("DELETE FROM notifications WHERE postedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM notifications")
    fun getCount(): Flow<Int>
}

/** Projection for distinct app queries. */
data class AppInfo(
    val packageName: String,
    val appName: String
)
