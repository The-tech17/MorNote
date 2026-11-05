package com.sanjana.mornote

import android.app.Application
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.util.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BackupManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testBackupDecryptionFailsWithWrongPassphrase() {
        val originalText = "This is a secret note payload."
        val passphraseA = "PassphraseA"
        val passphraseB = "PassphraseB"

        // Encrypt with Passphrase A
        val encryptedJson = BackupManager.encrypt(originalText, passphraseA)

        // Verify decryption with Passphrase A succeeds
        val decryptedText = BackupManager.decrypt(encryptedJson, passphraseA)
        assertEquals(originalText, decryptedText)

        // Verify decryption with Passphrase B fails and throws AEADBadTagException
        try {
            BackupManager.decrypt(encryptedJson, passphraseB)
            fail("Expected AEADBadTagException to be thrown")
        } catch (e: javax.crypto.AEADBadTagException) {
            // Success
        }
    }

    @Test
    fun testSelfDestructDeletesNoteAndGeneratesSnapshot() = runBlocking {
        // Mock dependencies
        val application = mock<Application>()
        val repository = mock<NoteRepository>()
        val aiSettingsRepository = mock<AiSettingsRepository>()

        // Create dummy NoteEntity
        val noteId = 42
        val userEmail = "owner@example.com"
        val dummyNote = NoteEntity(
            id = noteId,
            title = "Doomed Note Title",
            contentMorse = ".... . .-.. .-.. ---", // Hello
            timestamp = 123456789L,
            isPinned = false,
            isArchived = false,
            tags = listOf("private", "secure")
        )

        // Mock repository behavior
        whenever(repository.getNoteEntityById(noteId)).thenReturn(dummyNote)

        // Initialize NoteDetailViewModel
        val viewModel = NoteDetailViewModel(application, repository, aiSettingsRepository)
        viewModel.currentNoteId = noteId

        // Call reportFailedUnlock 3 times
        var onNoteDestructedCalled = false
        val onNoteDestructed = {
            onNoteDestructedCalled = true
        }

        viewModel.reportFailedUnlock(userEmail, onNoteDestructed)
        assertFalse(onNoteDestructedCalled)
        assertNull(viewModel.lastEmergencySnapshot)

        viewModel.reportFailedUnlock(userEmail, onNoteDestructed)
        assertFalse(onNoteDestructedCalled)
        assertNull(viewModel.lastEmergencySnapshot)

        // Third failure triggers self-destruct
        viewModel.reportFailedUnlock(userEmail, onNoteDestructed)
        
        // Execute pending coroutine tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert self destruct action was called and repo delete was invoked
        assertTrue(onNoteDestructedCalled)
        verify(repository).deleteNote(dummyNote)

        // Verify snapshot was generated
        val snapshot = viewModel.lastEmergencySnapshot
        assertNotNull(snapshot)

        // Verify snapshot can be decrypted with userEmail (Passphrase A)
        val decryptedJsonString = BackupManager.decrypt(JSONObject(snapshot!!), userEmail)
        val decryptedJson = JSONObject(decryptedJsonString)
        assertEquals(noteId, decryptedJson.getInt("id"))
        assertEquals("Doomed Note Title", decryptedJson.getString("title"))
        assertEquals(".... . .-.. .-.. ---", decryptedJson.getString("contentMorse"))
    }
}
