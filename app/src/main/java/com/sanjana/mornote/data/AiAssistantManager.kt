package com.sanjana.mornote.data

import com.google.ai.client.generativeai.GenerativeModel
import com.sanjana.mornote.security.VaultAiAssistant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiAssistantManager(apiKey: String = VaultAiAssistant.getApiKey()) {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun summarize(text: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Summarize the following text concisely:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun rewrite(text: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Rewrite the following text for better clarity and flow while keeping its original meaning:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun grammarFix(text: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Correct the grammar and spelling in the following text, and only return the corrected version:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun translate(text: String, targetLanguage: String = "English"): String? = withContext(Dispatchers.IO) {
        val prompt = "Translate the following text to $targetLanguage:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun extractTasks(text: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Extract all actionable tasks or to-do items from the following text and list them as bullet points:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun generateTitle(text: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Generate a short, descriptive title for the following text:\n\n$text"
        try {
            val response = model.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }
}
