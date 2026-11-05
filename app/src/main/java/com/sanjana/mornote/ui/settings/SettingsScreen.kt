package com.sanjana.mornote.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanjana.mornote.AppViewModelFactory
import com.sanjana.mornote.MorphedNotesApp
import com.sanjana.mornote.data.AiMode
import com.sanjana.mornote.data.AiProvider
import com.sanjana.mornote.data.ThemeMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = AppViewModelFactory(
            application = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MorphedNotesApp),
            repository = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MorphedNotesApp).repository,
            themeRepository = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MorphedNotesApp).themeRepository,
            aiSettingsRepository = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MorphedNotesApp).aiSettingsRepository
        )
    )
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val aiMode by viewModel.aiMode.collectAsState()
    val aiProvider by viewModel.aiProvider.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ThemeOption(
                title = "System Default",
                subtitle = "Follow your device theme",
                icon = "⚙️",
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.onThemeModeSelected(ThemeMode.SYSTEM) }
            )
            ThemeOption(
                title = "Light",
                subtitle = "Always use light mode",
                icon = "☀️",
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.onThemeModeSelected(ThemeMode.LIGHT) }
            )
            ThemeOption(
                title = "Dark",
                subtitle = "Always use dark mode",
                icon = "🌙",
                selected = themeMode == ThemeMode.DARK,
                onClick = { viewModel.onThemeModeSelected(ThemeMode.DARK) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("AI Vault", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            AiModeOption(
                title = "Private AI",
                selected = aiMode == AiMode.PRIVATE,
                onClick = { viewModel.onAiModeSelected(AiMode.PRIVATE) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Provider", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            AiProviderOption(
                title = "Gemini",
                selected = aiProvider == AiProvider.GEMINI,
                onClick = { viewModel.onAiProviderSelected(AiProvider.GEMINI) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            ListItem(
                headlineContent = { Text("Biometric Lock") },
                supportingContent = { Text("Require fingerprint to open notes") },
                trailingContent = { Text("Enabled") }
            )
        }
    }
}

@Composable
fun AiModeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
fun AiProviderOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
fun ThemeOption(
    title: String,
    subtitle: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.width(40.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}
