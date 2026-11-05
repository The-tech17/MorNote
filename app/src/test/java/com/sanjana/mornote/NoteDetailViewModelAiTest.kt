package com.sanjana.mornote

import android.app.Application
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.util.CipherEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelAiTest {

    private val testDispatcher = StandardTestDispatcher()
    private val application = mock<Application>()
    private val repository = mock<NoteRepository>()
    private val aiSettingsRepository = mock<AiSettingsRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialAiResponseStateIsIdle() {
        val viewModel = NoteDetailViewModel(application, repository, aiSettingsRepository, testDispatcher)
        assertEquals(AiUiState.Idle, viewModel.aiResponseState.value)
    }

    @Test
    fun testAskVaultAi_handlesDecryptionFailureGracefully() = runTest(testDispatcher) {
        val noteId = 99
        val corruptedNote = NoteEntity(
            id = noteId,
            title = "Corrupted Note",
            contentMorse = "INVALID_PAYLOAD",
            timestamp = 1000L,
            isPinned = false,
            isArchived = false,
            tags = emptyList()
        )
        whenever(repository.getNoteEntityById(noteId)).thenReturn(corruptedNote)

        val viewModel = NoteDetailViewModel(application, repository, aiSettingsRepository, testDispatcher)
        viewModel.askVaultAi("Summarize Note", noteId)

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.aiResponseState.value
        assertTrue("State must be Error when decryption fails", state is AiUiState.Error)
        val errorMessage = (state as AiUiState.Error).message
        assertTrue(errorMessage.contains("Decryption failed") || errorMessage.contains("unlock"))
    }

    @Test
    fun testResetAiResponseState() {
        val viewModel = NoteDetailViewModel(application, repository, aiSettingsRepository, testDispatcher)
        viewModel.aiResponseState.value = AiUiState.Success("Some AI summary")
        assertEquals("Some AI summary", (viewModel.aiResponseState.value as AiUiState.Success).response)

        viewModel.resetAiResponseState()
        assertEquals(AiUiState.Idle, viewModel.aiResponseState.value)
    }
}

