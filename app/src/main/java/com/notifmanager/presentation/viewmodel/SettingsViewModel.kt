package com.notifmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifmanager.data.database.entities.AppBehaviorEntity
import com.notifmanager.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL - Settings Screen
 *
 * Manages app settings and app behavior preferences
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    // Get all app behaviors
    val allAppBehaviors: StateFlow<List<AppBehaviorEntity>> = repository
        .getAllAppBehaviors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get spammy apps
    val spammyApps: StateFlow<List<AppBehaviorEntity>> = repository
        .getSpammyApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Lock app to specific category
     */
    fun lockAppToCategory(packageName: String, category: String) {
        viewModelScope.launch {
            repository.lockAppToCategory(packageName, category)
        }
    }

    /**
     * Unlock app (allow learning)
     */
    fun unlockApp(packageName: String) {
        viewModelScope.launch {
            repository.unlockApp(packageName)
        }
    }

    /**
     * Reset app behavior
     */
    fun resetAppBehavior(packageName: String) {
        viewModelScope.launch {
            repository.resetAppBehavior(packageName)
        }
    }
}