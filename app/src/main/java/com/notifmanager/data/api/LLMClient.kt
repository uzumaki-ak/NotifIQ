package com.notifmanager.data.api

import com.notifmanager.data.models.LLMProvider
import com.notifmanager.data.models.LLMRequest
import com.notifmanager.data.models.LLMResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LLM API Client - Makes calls to different LLM providers
 */
@Singleton
class LLMClient @Inject constructor() {

    /**
     * Call LLM to classify notification
     */
    suspend fun classifyNotification(
        request: LLMRequest,
        provider: LLMProvider,
        apiKey: String
    ): LLMResponse = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(request)

            val response = when (provider) {
                LLMProvider.EURON -> callEuron(prompt, apiKey)
                LLMProvider.GEMINI -> callGemini(prompt, apiKey)
                LLMProvider.CLAUDE -> callClaude(prompt, apiKey)
                LLMProvider.OPENAI -> callOpenAI(prompt, apiKey)
            }

            parseResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            LLMResponse(
                shouldBeImportant = false,
                reason = "Error: ${e.message}",
                confidence = 0f
            )
        }
    }

    private fun buildPrompt(request: LLMRequest): String {
        return """
You are a notification classifier. Analyze this notification and determine if it should be IMPORTANT or SILENT.

Notification: "${request.notificationText}"
${if (request.channelName != null) "Channel/Sender: ${request.channelName}" else ""}

User Preferences: ${request.userPreferences}

Respond ONLY with valid JSON in this format:
{"important": true/false, "reason": "brief explanation", "confidence": 0.0-1.0}
        """.trimIndent()
    }

    private fun callEuron(prompt: String, apiKey: String): String {
        val url = URL("https://api.euron.one/api/v1/euri/chat/completions")
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4.1-nano")
            put("max_tokens", 200)
            put("temperature", 0.3)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun callGemini(prompt: String, apiKey: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    private fun callClaude(prompt: String, apiKey: String): String {
        val url = URL("https://api.anthropic.com/v1/messages")
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("x-api-key", apiKey)
        conn.setRequestProperty("anthropic-version", "2023-06-01")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 200)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("content")
            .getJSONObject(0)
            .getString("text")
    }

    private fun callOpenAI(prompt: String, apiKey: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("max_tokens", 200)
            put("temperature", 0.3)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(response)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun parseResponse(response: String): LLMResponse {
        try {
            // Extract JSON from response (remove markdown if present)
            val jsonStr = response.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(jsonStr)

            return LLMResponse(
                shouldBeImportant = json.getBoolean("important"),
                reason = json.getString("reason"),
                confidence = json.getDouble("confidence").toFloat()
            )
        } catch (e: Exception) {
            // Fallback parsing
            val isImportant = response.contains("important", ignoreCase = true) &&
                    !response.contains("not important", ignoreCase = true)

            return LLMResponse(
                shouldBeImportant = isImportant,
                reason = "Parsed from text response",
                confidence = 0.5f
            )
        }
    }
}