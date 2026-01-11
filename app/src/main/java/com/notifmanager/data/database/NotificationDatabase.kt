package com.notifmanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notifmanager.data.database.dao.AppBehaviorDao
import com.notifmanager.data.database.dao.KeywordDao
import com.notifmanager.data.database.dao.NotificationDao
import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.data.database.entities.KeywordEntity
import com.notifmanager.data.database.entities.NotificationEntity

/**
 * Main database class - Room Database
 * This is the single source of truth for all app data
 *
 * Room handles all SQL generation automatically
 * Database is accessed through DAOs (Data Access Objects)
 */
@Database(
    entities = [
        NotificationEntity::class,
        AppBehaviorEntity::class,
        KeywordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NotificationDatabase : RoomDatabase() {

    /**
     * Get NotificationDao for notification operations
     */
    abstract fun notificationDao(): NotificationDao

    /**
     * Get AppBehaviorDao for behavior learning operations
     */
    abstract fun appBehaviorDao(): AppBehaviorDao

    /**
     * Get KeywordDao for custom keyword operations
     */
    abstract fun keywordDao(): KeywordDao
}