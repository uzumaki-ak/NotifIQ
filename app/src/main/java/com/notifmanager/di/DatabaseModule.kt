package com.notifmanager.di

import android.content.Context
import androidx.room.Room
import com.notifmanager.data.database.NotificationDatabase
import com.notifmanager.data.database.dao.AppBehaviorDao
import com.notifmanager.data.database.dao.KeywordDao
import com.notifmanager.data.database.dao.NotificationDao
import com.notifmanager.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * HILT MODULE - Database dependency injection
 *
 * Tells Hilt how to create database and DAOs
 * Singleton = only one instance throughout app lifetime
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide Room Database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotificationDatabase {
        return Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // Delete and recreate on schema change
            .build()
    }

    /**
     * Provide NotificationDao
     */
    @Provides
    @Singleton
    fun provideNotificationDao(database: NotificationDatabase): NotificationDao {
        return database.notificationDao()
    }

    /**
     * Provide AppBehaviorDao
     */
    @Provides
    @Singleton
    fun provideAppBehaviorDao(database: NotificationDatabase): AppBehaviorDao {
        return database.appBehaviorDao()
    }

    /**
     * Provide KeywordDao
     */
    @Provides
    @Singleton
    fun provideKeywordDao(database: NotificationDatabase): KeywordDao {
        return database.keywordDao()
    }
}