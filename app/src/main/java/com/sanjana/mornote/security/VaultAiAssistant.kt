package com.sanjana.mornote.security

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sanjana.mornote.BuildConfig
import com.sanjana.mornote.util.CipherEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton client-side AI assistant engine powered by Google GenAI SDK.
 * Operates strictly with local volatile plaintext in zero-knowledge mode.
 */
object VaultAiAssistant {

    private const val MODEL_NAME = "gemini-2.5-flash"

    val SYSTEM_INSTRUCTION = """
        You are the MorNote Vault Copilot, an integrated security and note-structuring assistant.
        Your capabilities:
        1. Summarize user diary entries and generate concise key points.
        2. Suggest 3 to 5 categorization tags for the note.
        3. Help the user draft notes securely without saving or echoing sensitive credentials (passwords, PINs, card numbers).
        4. Guide the user through vault management tasks (e.g., explaining how 3-strike self-destruct works, how to restore from a .mornote backup).

        Operating Rules:
        - You operate strictly on ephemeral in-memory context provided in the prompt.
        - Never ask the user for their Master PIN, encryption keys, or biometric data.
        - Refuse to analyze raw Base64 Morse/GCM blobs; instruct the user to unlock the note locally first.
    """.trimIndent()

    @Volatile
    private var customApiKey: String? = null

    /**
     * Optional custom API key override (e.g., user-provided key).
     */
    fun setApiKey(apiKey: String?) {
        customApiKey = apiKey
    }

    fun getApiKey(): String {
        return customApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
    }

    fun createGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = getApiKey(),
            systemInstruction = content {
                text(SYSTEM_INSTRUCTION)
            }
        )
    }

    /**
     * Executes an AI prompt against the client-side Google GenAI SDK model.
     * Never accepts or transmits raw ciphertext or KeyStore credentials.
     *
     * @param prompt The user's query or action (e.g., "Summarize Note").
     * @param decryptedContent Optional locally-decrypted plaintext of the note.
     * @return Result containing generated Markdown response or failure exception.
     */
    suspend fun processNoteTask(
        prompt: String,
        decryptedContent: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Guardrail: Ensure corrupted or raw ciphertext blobs are never analyzed
            if (decryptedContent == CipherEngine.DECRYPTION_ERROR) {
                return@withContext Result.failure(
                    IllegalStateException("Cannot analyze note: Decryption failed. Please unlock the note locally first.")
                )
            }

            val apiKey = getApiKey()
            if (apiKey.isBlank() || apiKey == "PLACEHOLDER_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key is not configured. Please set 'apiKey' or 'GEMINI_API_KEY' in local.properties.")
                )
            }

            val finalPrompt = if (!decryptedContent.isNullOrBlank()) {
                "$prompt\n\n--- Note Content ---\n$decryptedContent"
            } else {
                prompt
            }

            val model = createGenerativeModel()
            val response = model.generateContent(finalPrompt)
            val text = response.text

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Empty response received from Vault AI Assistant."))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}

