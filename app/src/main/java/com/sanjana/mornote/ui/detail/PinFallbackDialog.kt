package com.sanjana.mornote.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sanjana.mornote.security.AuthManager

enum class FallbackAuthMode {
    PIN_ENTRY,
    MNEMONIC_RECOVERY,
    SETUP_PIN
}

@Composable
fun PinFallbackDialog(
    authManager: AuthManager,
    failedAttempts: Int,
    maxAttempts: Int = 3,
    onSuccess: () -> Unit,
    onFailedAttempt: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPinConfigured = remember { authManager.isMasterPinSet() }
    var currentMode by remember {
        mutableStateOf(if (isPinConfigured) FallbackAuthMode.PIN_ENTRY else FallbackAuthMode.SETUP_PIN)
    }

    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var mnemonicInput by remember { mutableStateOf("") }
    var generatedMnemonic by remember { mutableStateOf<List<String>?>(null) }
    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (failedAttempts > 0) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentMode) {
                            FallbackAuthMode.PIN_ENTRY -> Icons.Default.Pin
                            FallbackAuthMode.MNEMONIC_RECOVERY -> Icons.Default.Key
                            FallbackAuthMode.SETUP_PIN -> Icons.Default.LockReset
                        },
                        contentDescription = null,
                        tint = if (failedAttempts > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = when (currentMode) {
                        FallbackAuthMode.PIN_ENTRY -> "Master PIN Verification"
                        FallbackAuthMode.MNEMONIC_RECOVERY -> "Recovery Phrase"
                        FallbackAuthMode.SETUP_PIN -> if (generatedMnemonic != null) "Backup Recovery Key" else "Set Up Master PIN"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle / Threat Notice
                if (currentMode != FallbackAuthMode.SETUP_PIN) {
                    val remaining = (maxAttempts - failedAttempts).coerceAtLeast(0)
                    Text(
                        text = if (failedAttempts == 0) {
                            "Enter your 6-digit Master PIN or 12-word recovery phrase."
                        } else {
                            "⚠️ $remaining attempt(s) remaining before automatic self-destruct."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (failedAttempts > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else if (generatedMnemonic == null) {
                    Text(
                        text = "Create a 6-digit Master PIN to secure your notes as an app-level fallback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mode-specific Content
                when (currentMode) {
                    FallbackAuthMode.PIN_ENTRY -> {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                    pinInput = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("6-Digit Master PIN") },
                            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            trailingIcon = {
                                IconButton(onClick = { showPin = !showPin }) {
                                    Icon(
                                        imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            singleLine = true,
                            isError = errorMessage != null || failedAttempts > 0,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                currentMode = FallbackAuthMode.MNEMONIC_RECOVERY
                                errorMessage = null
                                pinInput = ""
                            }
                        ) {
                            Text("Use 12-Word Recovery Phrase", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    FallbackAuthMode.MNEMONIC_RECOVERY -> {
                        OutlinedTextField(
                            value = mnemonicInput,
                            onValueChange = {
                                mnemonicInput = it
                                errorMessage = null
                            },
                            label = { Text("12-Word Recovery Phrase") },
                            placeholder = { Text("word1 word2 ... word12") },
                            minLines = 3,
                            maxLines = 4,
                            isError = errorMessage != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                currentMode = FallbackAuthMode.PIN_ENTRY
                                errorMessage = null
                                mnemonicInput = ""
                            }
                        ) {
                            Text("Use Master PIN Instead", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    FallbackAuthMode.SETUP_PIN -> {
                        if (generatedMnemonic == null) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        pinInput = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("Enter 6-Digit PIN") },
                                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                trailingIcon = {
                                    IconButton(onClick = { showPin = !showPin }) {
                                        Icon(
                                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        confirmPinInput = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("Confirm 6-Digit PIN") },
                                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            // Show the 12 words to the user
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Write down these 12 words in order. You will need them to recover access if you forget your PIN:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = generatedMnemonic?.chunked(3)?.joinToString("\n") { row ->
                                            row.joinToString("   ")
                                        } ?: "",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Error message display
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            when (currentMode) {
                                FallbackAuthMode.PIN_ENTRY -> {
                                    if (pinInput.length != 6) {
                                        errorMessage = "Please enter all 6 digits"
                                        return@Button
                                    }
                                    if (authManager.verifyMasterPin(pinInput)) {
                                        onSuccess()
                                    } else {
                                        pinInput = ""
                                        errorMessage = "Invalid Master PIN"
                                        onFailedAttempt()
                                    }
                                }

                                FallbackAuthMode.MNEMONIC_RECOVERY -> {
                                    val words = mnemonicInput.trim().split("\\s+".toRegex())
                                    if (words.size != 12) {
                                        errorMessage = "Please enter exactly 12 words (found ${words.size})"
                                        return@Button
                                    }
                                    if (authManager.recoverWithMnemonic(words)) {
                                        onSuccess()
                                    } else {
                                        errorMessage = "Invalid recovery phrase"
                                        onFailedAttempt()
                                    }
                                }

                                FallbackAuthMode.SETUP_PIN -> {
                                    if (generatedMnemonic == null) {
                                        if (pinInput.length != 6) {
                                            errorMessage = "PIN must be exactly 6 digits"
                                            return@Button
                                        }
                                        if (pinInput != confirmPinInput) {
                                            errorMessage = "PINs do not match"
                                            return@Button
                                        }
                                        val mnemonic = authManager.setupMasterPin(pinInput)
                                        generatedMnemonic = mnemonic
                                    } else {
                                        // User confirmed they saved the mnemonic
                                        onSuccess()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            when (currentMode) {
                                FallbackAuthMode.PIN_ENTRY -> "Unlock"
                                FallbackAuthMode.MNEMONIC_RECOVERY -> "Recover"
                                FallbackAuthMode.SETUP_PIN -> if (generatedMnemonic != null) "Done" else "Generate Key"
                            }
                        )
                    }
                }
            }
        }
    }
}
