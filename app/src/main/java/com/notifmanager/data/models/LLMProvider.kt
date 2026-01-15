package com.notifmanager.data.models

/**
 * LLM Provider enum
 */
enum class LLMProvider(val displayName: String, val apiUrl: String) {
    EURON("Euron.one (Cheapest)", "https://api.euron.one/api/v1/euri/chat/completions"),
    GEMINI("Google Gemini Flash", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"),
    CLAUDE("Anthropic Claude", "https://api.anthropic.com/v1/messages"),
    OPENAI("OpenAI GPT-4o-mini", "https://api.openai.com/v1/chat/completions")
}

/**
 * LLM Request/Response models
 */
data class LLMRequest(
    val notificationText: String,
    val channelName: String?,
    val userPreferences: String
)

data class LLMResponse(
    val shouldBeImportant: Boolean,
    val reason: String,
    val confidence: Float
)