package com.notifmanager.data.database.dao

import androidx.room.*
import com.notifmanager.data.database.entities.KeywordEntity
import com.notifmanager.data.database.entities.KeywordType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Custom Keywords
 * Manages user-defined important/spam keywords
 */
@Dao
interface KeywordDao {

    /**
     * Insert new keyword
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: KeywordEntity): Long

    /**
     * Update existing keyword
     */
    @Update
    suspend fun updateKeyword(keyword: KeywordEntity)

    /**
     * Delete keyword
     */
    @Delete
    suspend fun deleteKeyword(keyword: KeywordEntity)

    /**
     * Get all active keywords
     */
    @Query("SELECT * FROM custom_keywords WHERE isActive = 1 ORDER BY type, keyword")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    /**
     * Get keywords by type
     */
    @Query("SELECT * FROM custom_keywords WHERE type = :type AND isActive = 1 ORDER BY keyword")
    fun getKeywordsByType(type: KeywordType): Flow<List<KeywordEntity>>

    /**
     * Check if keyword already exists
     */
    @Query("SELECT * FROM custom_keywords WHERE LOWER(keyword) = LOWER(:keyword)")
    suspend fun getKeywordByText(keyword: String): KeywordEntity?

    /**
     * Get all keywords as list (for scoring engine)
     */
    @Query("SELECT * FROM custom_keywords WHERE isActive = 1")
    suspend fun getAllKeywordsList(): List<KeywordEntity>

    /**
     * Disable keyword (soft delete)
     */
    @Query("UPDATE custom_keywords SET isActive = 0 WHERE id = :keywordId")
    suspend fun disableKeyword(keywordId: Long)

    /**
     * Enable keyword
     */
    @Query("UPDATE custom_keywords SET isActive = 1 WHERE id = :keywordId")
    suspend fun enableKeyword(keywordId: Long)

    /**
     * Delete all keywords
     */
    @Query("DELETE FROM custom_keywords")
    suspend fun deleteAllKeywords()
}