package com.notifmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores custom keywords that user can add
 * Users can teach the app new important/spam keywords
 *
 * Example: User adds "interview" as IMPORTANT keyword
 * Now any notification with "interview" gets boosted priority
 */
@Entity(tableName = "custom_keywords")
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val keyword: String,  // The actual keyword (lowercase, trimmed)
    val type: KeywordType,  // Is it CRITICAL, IMPORTANT, or SPAM?
    val scoreModifier: Int,  // How many points to add/subtract (-30 to +30)

    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true  // Can be disabled without deleting
)

/**
 * Keyword types that user can create
 */
enum class KeywordType {
    CRITICAL,  // Boosts to critical priority
    IMPORTANT,  // Boosts to important priority
    SPAM  // Reduces to silent/spam
}