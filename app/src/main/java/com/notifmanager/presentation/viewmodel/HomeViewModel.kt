package com.notifmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL - Home Screen
 *
 * Manages UI state and business logic for home screen
 * ViewModels survive configuration changes (screen rotation)
 *
 * This is the bridge between UI and data layer
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    // UI State for home screen
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Get all notifications from database
    val allNotifications: StateFlow<List<NotificationEntity>> = repository
        .getAllActiveNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Group notifications by category
    val groupedNotifications: StateFlow<Map<Constants.NotificationCategory, List<NotificationEntity>>> =
        allNotifications.map { notifications ->
            notifications.groupBy { notification ->
                when (notification.category) {
                    "CRITICAL" -> Constants.NotificationCategory.CRITICAL
                    "IMPORTANT" -> Constants.NotificationCategory.IMPORTANT
                    "NORMAL" -> Constants.NotificationCategory.NORMAL
                    else -> Constants.NotificationCategory.SILENT
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Count by category
    val categoryCounts: StateFlow<Map<String, Int>> = allNotifications.map { notifications ->
        mapOf(
            "CRITICAL" to notifications.count { it.category == "CRITICAL" },
            "IMPORTANT" to notifications.count { it.category == "IMPORTANT" },
            "NORMAL" to notifications.count { it.category == "NORMAL" },
            "SILENT" to notifications.count { it.category == "SILENT" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    init {
        // Load initial data
        loadNotifications()
    }

    /**
     * Load notifications
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Notifications automatically loaded via Flow
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Mark notification as opened
     */
    fun markAsOpened(notificationId: Long) {
        viewModelScope.launch {
            repository.markAsOpened(notificationId)
        }
    }

    /**
     * Dismiss notification
     */
    fun dismissNotification(notificationId: Long) {
        viewModelScope.launch {
            repository.markAsDismissed(notificationId)
            repository.deleteNotification(notificationId)
        }
    }

    /**
     * Delete notification permanently
     */
    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    /**
     * Set selected category filter
     */
    fun setSelectedCategory(category: Constants.NotificationCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Set search query
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedCategory: Constants.NotificationCategory? = null,
    val searchQuery: String = "",
    val error: String? = null
)