package com.sanjana.mornote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

import com.sanjana.mornote.data.AiAssistantManager
import com.sanjana.mornote.data.AiMode
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.data.BackendAiManager
import com.sanjana.mornote.security.VaultAiAssistant
import com.sanjana.mornote.ui.detail.AiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val response: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

class NoteDetailViewModel(
    application: Application, 
    private val repository: NoteRepository,
    private val aiSettingsRepository: AiSettingsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : AndroidViewModel(application) {

    private val aiManager by lazy { AiAssistantManager(VaultAiAssistant.getApiKey()) }
    private val backendAiManager by lazy { BackendAiManager() }

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    val aiResponseState = MutableStateFlow<AiUiState>(AiUiState.Idle)

    var currentNoteId: Int = 0

    fun askVaultAi(userPrompt: String, noteId: Int, draftContent: String? = null) {
        viewModelScope.launch {
            aiResponseState.value = AiUiState.Loading
            try {
                // 1. Retrieves the decrypted plaintext note using the existing authenticated pipeline.
                val plaintext = if (noteId > 0) {
                    val details = loadNoteDetails(noteId)
                    details?.second ?: draftContent
                } else {
                    draftContent
                }

                if (plaintext == CipherEngine.DECRYPTION_ERROR) {
                    aiResponseState.value = AiUiState.Error("Cannot analyze note: Decryption failed. Please unlock the note locally first.")
                    return@launch
                }

                // 2. Set state to AiUiState.Loading (already set above).
                // 3. Calls VaultAiAssistant.processNoteTask(userPrompt, plaintext).
                val result = VaultAiAssistant.processNoteTask(
                    prompt = userPrompt,
                    decryptedContent = plaintext
                )

                // 4. Updates aiResponseState with the markdown response.
                result.fold(
                    onSuccess = { markdown ->
                        aiResponseState.value = AiUiState.Success(markdown)
                    },
                    onFailure = { error ->
                        aiResponseState.value = AiUiState.Error(error.localizedMessage ?: "Failed to generate AI response.")
                    }
                )
            } catch (e: Exception) {
                aiResponseState.value = AiUiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetAiResponseState() {
        aiResponseState.value = AiUiState.Idle
    }

    fun performAiAction(action: AiAction, text: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResult.value = null

            val mode = aiSettingsRepository.aiMode.first()
            val provider = aiSettingsRepository.aiProvider.first()

            val result = if (mode == AiMode.PRIVATE) {
                backendAiManager.processAiAction(action, text, provider)
            } else {
                when (action) {
                    AiAction.Summarize -> aiManager.summarize(text)
                    AiAction.Rewrite -> aiManager.rewrite(text)
                    AiAction.GrammarFix -> aiManager.grammarFix(text)
                    AiAction.ExtractTasks -> aiManager.extractTasks(text)
                    AiAction.GenerateTitle -> aiManager.generateTitle(text)
                    AiAction.Translate -> aiManager.translate(text)
                }
            }
            _aiResult.value = result
            _isAiLoading.value = false
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }

    suspend fun loadNoteDetails(id: Int): Pair<String, String>? = withContext(defaultDispatcher) {
        currentNoteId = id
        val entity = repository.getNoteEntityById(id) ?: return@withContext null
        val plainContent = CipherEngine.unmorph(entity.contentMorse)
        Pair(entity.title, plainContent)
    }

    suspend fun saveNote(title: String, content: String) = withContext(defaultDispatcher) {
        if (title.isBlank() || content.isBlank()) return@withContext
        val morphedContent = CipherEngine.morph(content)
        if (currentNoteId > 0) {
            val entity = repository.getNoteEntityById(currentNoteId)
            if (entity != null) {
                val updatedNote = entity.copy(
                    title = title,
                    contentMorse = morphedContent,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
            }
        } else {
            repository.insertNote(title, morphedContent)
        }
    }

    private var failedAttempts = 0
    val failedAttemptsCount: Int get() = failedAttempts
    var lastEmergencySnapshot: String? = null

    fun reportFailedUnlock(userEmail: String?, onNoteDestructed: () -> Unit) {
        failedAttempts++
        if (failedAttempts >= 3) {
            executeSelfDestruct(userEmail, onNoteDestructed)
        }
    }

    private fun executeSelfDestruct(userEmail: String?, onNoteDestructed: () -> Unit) {
        viewModelScope.launch {
            if (currentNoteId > 0) {
                val entity = repository.getNoteEntityById(currentNoteId)
                if (entity != null) {
                    val noteTitle = entity.title
                    
                    try {
                        val noteJson = JSONObject().apply {
                            put("id", entity.id)
                            put("title", entity.title)
                            put("contentMorse", entity.contentMorse)
                            put("timestamp", entity.timestamp)
                            put("isPinned", entity.isPinned)
                            put("isArchived", entity.isArchived)
                            val tagsArray = JSONArray()
                            entity.tags.forEach { tagsArray.put(it) }
                            put("tags", tagsArray)
                        }
                        val passphrase = userEmail ?: "EmergencyPassphrase"
                        val encryptedSnapshotJson = com.sanjana.mornote.util.BackupManager.encrypt(noteJson.toString(), passphrase)
                        lastEmergencySnapshot = encryptedSnapshotJson.toString()
                    } catch (_: Exception) {
                        lastEmergencySnapshot = null
                    }
                    
                    repository.deleteNote(entity)
                    CipherEngine.purgeMasterKey()
                    if (!userEmail.isNullOrBlank()) {
                        sendSecurityAlertEmail(userEmail, noteTitle, lastEmergencySnapshot)
                    }
                    failedAttempts = 0
                    currentNoteId = 0
                    onNoteDestructed()
                }
            }
        }
    }

    private fun sendSecurityAlertEmail(email: String, noteTitle: String, recoveryBlob: String?) {
        val emailScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)
        emailScope.launch {
            val client = OkHttpClient()
            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val currentTime = java.text.DateFormat.getDateTimeInstance().format(java.util.Date())
            val recoveryBlobJsonEscaped = recoveryBlob?.replace("\"", "\\\"") ?: ""
            val jsonPayload = """
                {
                    "service_id": "service_d4qjpia",
                    "template_id": "template_0wrf1ao",
                    "user_id": "S9RmX612IiEuToYOe",
                    "accessToken": "RqncqYMeuPEs99q7jZEt_",
                    "template_params": {
                        "to_email": "$email",
                        "note_title": "$noteTitle",
                        "time": "$currentTime",
                        "recovery_blob": "$recoveryBlobJsonEscaped"
                    }
                }
            """.trimIndent()
            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(jsonPayload.toRequestBody(jsonMediaType))
                .build()
            try {
                client.newCall(request).execute().use { _ -> }
            } catch (_: Exception) {
            }
        }
    }

    fun resetFailedAttempts() {
        failedAttempts = 0
    }

    fun togglePin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (currentNoteId > 0) {
                val entity = repository.getNoteEntityById(currentNoteId)
                if (entity != null) {
                    repository.togglePin(entity)
                    onSuccess()
                }
            }
        }
    }

    fun toggleArchive(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (currentNoteId > 0) {
                val entity = repository.getNoteEntityById(currentNoteId)
                if (entity != null) {
                    repository.toggleArchive(entity)
                    onSuccess()
                }
            }
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (currentNoteId > 0) {
                val entity = repository.getNoteEntityById(currentNoteId)
                if (entity != null) {
                    repository.deleteNote(entity)
                    onSuccess()
                }
            }
        }
    }
}
