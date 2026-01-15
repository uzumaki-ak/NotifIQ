package com.notifmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.data.repository.NotificationRepository
import com.notifmanager.presentation.ui.screens.TimeFilter
import com.notifmanager.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL - Home Screen
 *
 * UPDATED: Added time-based filtering
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    // UI State for home screen
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Current time filter
    private val _timeFilter = MutableStateFlow(TimeFilter.ALL_TIME)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    // Get all notifications from database with time filter
    val allNotifications: StateFlow<List<NotificationEntity>> = combine(
        _timeFilter,
        repository.getAllActiveNotifications()
    ) { filter, notifications ->
        filterByTime(notifications, filter)
    }.stateIn(
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
     * Set time filter
     */
    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    /**
     * Filter notifications by time
     */
    private fun filterByTime(notifications: List<NotificationEntity>, filter: TimeFilter): List<NotificationEntity> {
        if (filter.hoursAgo == null) return notifications // ALL_TIME

        val cutoffTime = System.currentTimeMillis() - (filter.hoursAgo * 60 * 60 * 1000L)
        return notifications.filter { it.receivedTime >= cutoffTime }
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

    /**
     * Mark channel/sender as important
     */
    fun markChannelImportant(notificationId: Long) {
        viewModelScope.launch {
            val notification = repository.getNotificationById(notificationId)
            if (notification != null && notification.contentId != null) {
                // Set preference to +20 (Important)
                repository.setContentPreference(
                    notification.packageName,
                    notification.contentId,
                    notification.contentType ?: "GENERIC",
                    20
                )
            }
        }
    }

    /**
     * Mark channel/sender as silent
     */
    fun markChannelSilent(notificationId: Long) {
        viewModelScope.launch {
            val notification = repository.getNotificationById(notificationId)
            if (notification != null && notification.contentId != null) {
                // Set preference to -20 (Silent)
                repository.setContentPreference(
                    notification.packageName,
                    notification.contentId,
                    notification.contentType ?: "GENERIC",
                    -20
                )
            }
        }
    }

    /**
     * Clear all notifications (but keep learning data)
     */
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
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
