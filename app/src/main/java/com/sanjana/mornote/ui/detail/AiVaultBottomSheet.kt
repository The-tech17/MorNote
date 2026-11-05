package com.sanjana.mornote.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class AiAction(val title: String, val icon: ImageVector) {
    object Summarize : AiAction("Summarize", Icons.Default.AutoAwesome)
    object Rewrite : AiAction("Rewrite", Icons.Default.EditNote)
    object GrammarFix : AiAction("Grammar Fix", Icons.Default.CheckCircle)
    object GenerateTitle : AiAction("Generate Title", Icons.Default.Title)
    object ExtractTasks : AiAction("Extract Tasks", Icons.Default.TaskAlt)
    object Translate : AiAction("Translate", Icons.Default.Translate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVaultBottomSheet(
    onActionSelected: (AiAction) -> Unit,
    onDismiss: () -> Unit
) {
    val actions = listOf(
        AiAction.Summarize,
        AiAction.Rewrite,
        AiAction.GrammarFix,
        AiAction.GenerateTitle,
        AiAction.ExtractTasks,
        AiAction.Translate
    )

    var step by remember { mutableIntStateOf(0) } // 0: Security, 1: Actions

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (step == 0) {
            SecurityIntroScreen(onContinue = { step = 1 })
        } else {
            AiActionsList(actions, onActionSelected)
        }
    }
}

@Composable
private fun SecurityIntroScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔒 AI Vault",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SecurityPoint(
                    icon = Icons.Default.Lock,
                    text = "Your selected note will be securely processed."
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecurityPoint(
                    icon = Icons.Default.CloudOff,
                    text = "Cloud AI never stores your data."
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun SecurityPoint(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AiActionsList(
    actions: List<AiAction>,
    onActionSelected: (AiAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "AI Vault",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        actions.forEach { action ->
            ListItem(
                headlineContent = { Text(action.title) },
                leadingContent = { 
                    Icon(
                        action.icon, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                modifier = Modifier.clickable { onActionSelected(action) }
            )
        }
        
        ListItem(
            headlineContent = { Text("Cancel", color = MaterialTheme.colorScheme.error) },
            leadingContent = { 
                Icon(
                    Icons.Default.Close, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                ) 
            },
            modifier = Modifier.clickable { /* Handled by dismiss */ }
        )
    }
}
