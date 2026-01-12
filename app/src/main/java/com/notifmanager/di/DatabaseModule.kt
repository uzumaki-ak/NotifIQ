package com.notifmanager.di

import android.content.Context
import androidx.room.Room
import com.notifmanager.data.database.NotificationDatabase
import com.notifmanager.data.database.dao.*
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
 * UPDATED: Added ContentPreferenceDao and ContentBehaviorDao
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotificationDatabase {
        return Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: NotificationDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideAppBehaviorDao(database: NotificationDatabase): AppBehaviorDao {
        return database.appBehaviorDao()
    }

    @Provides
    @Singleton
    fun provideKeywordDao(database: NotificationDatabase): KeywordDao {
        return database.keywordDao()
    }

    // NEW DAOs
    @Provides
    @Singleton
    fun provideContentPreferenceDao(database: NotificationDatabase): ContentPreferenceDao {
        return database.contentPreferenceDao()
    }

    @Provides
    @Singleton
    fun provideContentBehaviorDao(database: NotificationDatabase): ContentBehaviorDao {
        return database.contentBehaviorDao()
    }
}