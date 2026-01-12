package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.ContentPreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Content Preferences
 * Manages user's manual preferences for channels/senders
 */
@Dao
interface ContentPreferenceDao {

    /**
     * Insert or update preference
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preference: ContentPreferenceEntity): Long

    /**
     * Get preference for specific content
     */
    @Query("SELECT * FROM content_preferences WHERE appPackage = :appPackage AND contentId = :contentId")
    suspend fun getPreference(appPackage: String, contentId: String): ContentPreferenceEntity?

    /**
     * Get all preferences
     */
    @Query("SELECT * FROM content_preferences ORDER BY lastUpdated DESC")
    fun getAllPreferences(): Flow<List<ContentPreferenceEntity>>

    /**
     * Get preferences by app
     */
    @Query("SELECT * FROM content_preferences WHERE appPackage = :appPackage ORDER BY preferenceScore DESC")
    fun getPreferencesByApp(appPackage: String): Flow<List<ContentPreferenceEntity>>

    /**
     * Get preferences by content type
     */
    @Query("SELECT * FROM content_preferences WHERE contentType = :contentType ORDER BY preferenceScore DESC")
    fun getPreferencesByType(contentType: String): Flow<List<ContentPreferenceEntity>>

    /**
     * Update preference score
     */
    @Query("UPDATE content_preferences SET preferenceScore = :score, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updatePreferenceScore(id: Long, score: Int, timestamp: Long)

    /**
     * Lock preference (user manually set)
     */
    @Query("UPDATE content_preferences SET isLocked = 1 WHERE id = :id")
    suspend fun lockPreference(id: Long)

    /**
     * Unlock preference (allow auto-learning)
     */
    @Query("UPDATE content_preferences SET isLocked = 0 WHERE id = :id")
    suspend fun unlockPreference(id: Long)

    /**
     * Delete preference
     */
    @Delete
    suspend fun deletePreference(preference: ContentPreferenceEntity)

    /**
     * Delete all preferences
     */
    @Query("DELETE FROM content_preferences")
    suspend fun deleteAll()
}