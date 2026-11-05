package com.sanjana.mornote.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.sanjana.mornote.AiUiState
import com.sanjana.mornote.AppViewModelFactory
import com.sanjana.mornote.NoteDetailViewModel
import com.sanjana.mornote.security.AuthManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    viewModelFactory: AppViewModelFactory,
    onSaveSuccess: () -> Unit,
    requestBiometricAuth: (
        onSuccess: () -> Unit,
        onMismatch: () -> Unit,
        onCancel: () -> Unit,
        onFallback: () -> Unit
    ) -> Unit
) {
    val viewModel: NoteDetailViewModel = viewModel(factory = viewModelFactory)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var showPinFallbackDialog by remember { mutableStateOf(false) }
    var showAiVault by remember { mutableStateOf(false) }
    var showAskVaultAiSheet by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    val aiResult by viewModel.aiResult.collectAsState()
    val isAiLoadingState by viewModel.isAiLoading.collectAsState()
    val aiUiState by viewModel.aiResponseState.collectAsState()

    fun unlockAndLoadNote() {
        coroutineScope.launch {
            viewModel.resetFailedAttempts()
            val details = viewModel.loadNoteDetails(noteId)
            if (details != null) {
                title = details.first
                content = details.second
            }
            isLoading = false
            showPinFallbackDialog = false
        }
    }

    fun handleFailedAttempt() {
        viewModel.reportFailedUnlock(
            userEmail = Firebase.auth.currentUser?.email,
            onNoteDestructed = {
                showPinFallbackDialog = false
                Toast.makeText(context, "Security Alert: Note self-destructed.", Toast.LENGTH_LONG).show()
                onSaveSuccess()
            }
        )
    }

    LaunchedEffect(noteId) {
        if (noteId > 0) {
            viewModel.currentNoteId = noteId
            isLoading = true
            requestBiometricAuth(
                /* onSuccess = */ {
                    unlockAndLoadNote()
                },
                /* onMismatch = */ {
                    handleFailedAttempt()
                },
                /* onCancel = */ {
                    isLoading = false
                    onSaveSuccess()
                },
                /* onFallback = */ {
                    isLoading = false
                    showPinFallbackDialog = true
                }
            )
        } else {
            viewModel.currentNoteId = 0
            title = ""
            content = ""
        }
    }

    if (showPinFallbackDialog) {
        PinFallbackDialog(
            authManager = authManager,
            failedAttempts = viewModel.failedAttemptsCount,
            maxAttempts = 3,
            onSuccess = {
                unlockAndLoadNote()
            },
            onFailedAttempt = {
                handleFailedAttempt()
            },
            onDismiss = {
                showPinFallbackDialog = false
                onSaveSuccess()
            }
        )
    }

    if (aiResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearAiResult() },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒 AI Vault Complete", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = { 
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("✓ AI generated results ready.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(aiResult as String)
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            content = (aiResult as String)
                            viewModel.clearAiResult()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replace Note")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            content += "\n\n" + (aiResult as String)
                            viewModel.clearAiResult()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Insert Below")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Result", aiResult as String)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            viewModel.clearAiResult()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearAiResult() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == 0) "New MorNote" else "Edit MorNote") },
                actions = {
                    IconButton(onClick = { showAskVaultAiSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Ask Vault AI")
                    }
                    if (noteId > 0) {
                        IconButton(onClick = { viewModel.togglePin { 
                            Toast.makeText(context, "Note pinned/unpinned", Toast.LENGTH_SHORT).show()
                        } }) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin Note")
                        }
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Export")
                        }
                        IconButton(onClick = { viewModel.toggleArchive { onSaveSuccess() } }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive")
                        }
                        IconButton(onClick = { viewModel.deleteNote { onSaveSuccess() } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export as PDF") },
                            onClick = {
                                showExportMenu = false
                                exportToPdf(context, title, content)
                            },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as TXT") },
                            onClick = {
                                showExportMenu = false
                                exportToTxt(context, title, content)
                            },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Write your secret note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                if (isAiLoadingState) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                    Text("AI is processing...", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isAiLoadingState) {
                    TextButton(
                        onClick = { showAskVaultAiSheet = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Ask Vault AI", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    viewModel.saveNote(title, content)
                                    onSaveSuccess()
                                } catch (_: Exception) {
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    },
                    enabled = !isSaving && title.isNotBlank() && content.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Save MorNote")
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showAiVault) {
            AiVaultBottomSheet(
                onActionSelected = { action ->
                    showAiVault = false
                    viewModel.performAiAction(action, content)
                },
                onDismiss = { showAiVault = false }
            )
        }

        if (showAskVaultAiSheet) {
            AskVaultAiBottomSheet(
                uiState = aiUiState,
                onAction = { prompt ->
                    viewModel.askVaultAi(prompt, noteId, content)
                },
                onDismiss = {
                    showAskVaultAiSheet = false
                },
                onInsertBelow = { responseText ->
                    content = if (content.isBlank()) responseText else "$content\n\n$responseText"
                    Toast.makeText(context, "Appended to note", Toast.LENGTH_SHORT).show()
                },
                onReplaceNote = { responseText ->
                    content = responseText
                    Toast.makeText(context, "Note content replaced", Toast.LENGTH_SHORT).show()
                },
                onResetState = {
                    viewModel.resetAiResponseState()
                }
            )
        }
    }
}

private fun exportToPdf(context: Context, title: String, content: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText(title, 50f, 50f, paint)
    
    paint.textSize = 14f
    paint.isFakeBoldText = false
    val lines = content.split("\n")
    var y = 100f
    for (line in lines) {
        if (y > 800) break
        canvas.drawText(line, 50f, y, paint)
        y += 20f
    }
    
    pdfDocument.finishPage(page)
    
    val file = File(context.cacheDir, "${title.filter { it.isLetterOrDigit() }}.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        shareFile(context, file, "application/pdf")
    } catch (_: Exception) {
        Toast.makeText(context, "Error exporting PDF", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

private fun exportToTxt(context: Context, title: String, content: String) {
    val file = File(context.cacheDir, "${title.filter { it.isLetterOrDigit() }}.txt")
    try {
        FileOutputStream(file).use { 
            it.write(content.toByteArray())
        }
        shareFile(context, file, "text/plain")
    } catch (_: Exception) {
        Toast.makeText(context, "Error exporting TXT", Toast.LENGTH_SHORT).show()
    }
}

private fun shareFile(context: Context, file: File, mimeType: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Note"))
    } catch (_: Exception) {
        Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
    }
}
