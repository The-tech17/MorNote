package com.sanjana.mornote.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanjana.mornote.data.RemoteConfigManager

data class MorNoteUpdate(
    val date: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    var liveNews by remember { mutableStateOf("Loading news...") }

    val updates = remember {
        listOf(
            MorNoteUpdate(
                "Jul 26, 2026",
                "UI Polish and new AI Features",
                "Mornote has a polished look with a brand new AI Vault, which assists users in writing.",
                Icons.Default.NewReleases,
                "Feature"
            ),
            MorNoteUpdate(
                "Jun 05, 2026",
                "Security Update",
                "Notes are accessible by secure fingerprint scans. After three incorrect scan attempts, the note is automatically deleted.",
                Icons.Default.Security,
                "Security"
            ),
            MorNoteUpdate(
                "Dec 30, 2025",
                "Morphed Logic Integrated",
                "Notes are now secured with a rotation cipher for extra protection.",
                Icons.Default.Lock,
                "Feature"
            ),
            MorNoteUpdate(
                "Dec 28, 2025",
                "Google Login Added",
                "Access your diary securely using Google authentication.",
                Icons.AutoMirrored.Filled.Login,
                "Update"
            ),
            MorNoteUpdate(
                "Dec 25, 2025",
                "Welcome to MorNote",
                "Your personal secure diary is officially launched! Start writing securely.",
                Icons.Default.Celebration,
                "Launch"
            )
        )
    }

    LaunchedEffect(Unit) {
        RemoteConfigManager.fetchUpdates { news ->
            liveNews = news
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("News & Updates", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(updates) { update ->
                UpdateCard(update)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mornote • Secure & Private",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateCard(update: MorNoteUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = update.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = update.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = update.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = update.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = update.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
