package com.notifmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifmanager.data.database.entities.ContentPreferenceEntity
import com.notifmanager.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Preferences Screen
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val allPreferences: StateFlow<List<ContentPreferenceEntity>> = repository
        .getAllContentPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updatePreferenceScore(id: Long, score: Int) {
        viewModelScope.launch {
            repository.updateContentPreference(id, score)
        }
    }

    fun deletePreference(preference: ContentPreferenceEntity) {
        viewModelScope.launch {
            repository.deleteContentPreference(preference)
        }
    }
}