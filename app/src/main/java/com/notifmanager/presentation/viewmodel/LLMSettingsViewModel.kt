package com.notifmanager.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifmanager.data.models.LLMProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for LLM Settings
 */
@HiltViewModel
class LLMSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("llm_prefs", Context.MODE_PRIVATE)

    private val _isLLMEnabled = MutableStateFlow(prefs.getBoolean("llm_enabled", false))
    val isLLMEnabled: StateFlow<Boolean> = _isLLMEnabled.asStateFlow()

    private val _selectedProvider = MutableStateFlow(
        LLMProvider.valueOf(prefs.getString("llm_provider", LLMProvider.EURON.name)!!)
    )
    val selectedProvider: StateFlow<LLMProvider> = _selectedProvider.asStateFlow()

    private val _apiKeys = MutableStateFlow(loadApiKeys())
    val apiKeys: StateFlow<Map<String, String>> = _apiKeys.asStateFlow()

    private fun loadApiKeys(): Map<String, String> {
        return LLMProvider.values().associate { provider ->
            provider.name to (prefs.getString("api_key_${provider.name}", "") ?: "")
        }
    }

    fun setLLMEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _isLLMEnabled.value = enabled
            prefs.edit().putBoolean("llm_enabled", enabled).apply()
        }
    }

    fun selectProvider(provider: LLMProvider) {
        viewModelScope.launch {
            _selectedProvider.value = provider
            prefs.edit().putString("llm_provider", provider.name).apply()
        }
    }

    fun setApiKey(provider: LLMProvider, key: String) {
        viewModelScope.launch {
            val updated = _apiKeys.value.toMutableMap()
            updated[provider.name] = key
            _apiKeys.value = updated
            prefs.edit().putString("api_key_${provider.name}", key).apply()
        }
    }

    fun getApiKey(provider: LLMProvider): String {
        return prefs.getString("api_key_${provider.name}", "") ?: ""
    }

    fun isLLMConfigured(): Boolean {
        return _isLLMEnabled.value && getApiKey(_selectedProvider.value).isNotBlank()
    }
}