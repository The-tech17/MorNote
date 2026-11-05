package com.sanjana.mornote.security

import com.sanjana.mornote.util.CipherEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VaultAiAssistantTest {

    @Test
    fun testSystemInstructionContent() {
        val instruction = VaultAiAssistant.SYSTEM_INSTRUCTION
        assertTrue("Must define MorNote Vault Copilot role", instruction.contains("You are the MorNote Vault Copilot"))
        assertTrue("Must include capability 1 (summarize)", instruction.contains("Summarize user diary entries"))
        assertTrue("Must include capability 2 (tags)", instruction.contains("Suggest 3 to 5 categorization tags"))
        assertTrue("Must include capability 3 (credentials safety)", instruction.contains("without saving or echoing sensitive credentials"))
        assertTrue("Must include capability 4 (vault guidance)", instruction.contains("Guide the user through vault management tasks"))
        assertTrue("Must forbid asking for PIN/keys", instruction.contains("Never ask the user for their Master PIN, encryption keys, or biometric data"))
        assertTrue("Must refuse raw Morse/GCM blobs", instruction.contains("Refuse to analyze raw Base64 Morse/GCM blobs"))
    }

    @Test
    fun testRefuseDecryptionError() = runBlocking {
        // Vault AI must immediately reject payloads with DECRYPTION_ERROR without calling model
        val result = VaultAiAssistant.processNoteTask(
            prompt = "Summarize Note",
            decryptedContent = CipherEngine.DECRYPTION_ERROR
        )
        assertTrue("Result must be failure when content is DECRYPTION_ERROR", result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Decryption failed") == true)
    }

    @Test
    fun testProcessNoteTask_safelyHandlesFailuresWithoutCrashing() = runBlocking {
        // Using an empty/invalid key should return Result.failure wrapped cleanly
        VaultAiAssistant.setApiKey("INVALID_TEST_KEY")
        val result = VaultAiAssistant.processNoteTask(
            prompt = "Check Grammar",
            decryptedContent = "This are a test note."
        )
        assertTrue("Invalid key / unauthenticated network call must wrap error in Result.failure", result.isFailure)
        assertNotNull(result.exceptionOrNull())
        // Reset key
        VaultAiAssistant.setApiKey(null)
    }
}

