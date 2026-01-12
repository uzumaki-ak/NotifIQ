package com.notifmanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notifmanager.data.database.dao.*
import com.notifmanager.data.database.entities.*

/**
 * Main database class - Room Database
 *
 * UPDATED: Added ContentPreference and ContentBehavior tables
 */
@Database(
    entities = [
        NotificationEntity::class,
        AppBehaviorEntity::class,
        KeywordEntity::class,
        ContentPreferenceEntity::class,  // NEW
        ContentBehaviorEntity::class     // NEW
    ],
    version = 2,  // UPDATED version
    exportSchema = false
)
abstract class NotificationDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun appBehaviorDao(): AppBehaviorDao
    abstract fun keywordDao(): KeywordDao
    abstract fun contentPreferenceDao(): ContentPreferenceDao  // NEW
    abstract fun contentBehaviorDao(): ContentBehaviorDao      // NEW
}