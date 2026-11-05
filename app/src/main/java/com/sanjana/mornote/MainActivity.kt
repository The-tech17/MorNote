package com.sanjana.mornote

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.sanjana.mornote.ui.auth.LoginScreen
import com.sanjana.mornote.ui.detail.NoteDetailScreen
import com.sanjana.mornote.ui.notes.NoteListScreen
import com.sanjana.mornote.ui.theme.MorNoteTheme
import kotlinx.coroutines.launch
import com.sanjana.mornote.ui.feed.FeedScreen
import com.sanjana.mornote.ui.settings.SettingsScreen
import com.sanjana.mornote.ui.notes.ArchiveScreen
import com.sanjana.mornote.data.ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

import com.sanjana.mornote.security.SecurityGuard

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as MorphedNotesApp).repository
        val themeRepository = (application as MorphedNotesApp).themeRepository
        val aiSettingsRepository = (application as MorphedNotesApp).aiSettingsRepository
        val viewModelFactory = AppViewModelFactory(application, repository, themeRepository, aiSettingsRepository)

        val isCompromisedDevice = SecurityGuard.isDeviceCompromised(this)

        setContent {
            val themeMode by themeRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MorNoteTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showCompromisedAlertDialog by remember { mutableStateOf(isCompromisedDevice) }
                    if (showCompromisedAlertDialog && SecurityGuard.activePolicy != SecurityGuard.SecurityPolicy.ALLOW) {
                        AlertDialog(
                            onDismissRequest = { showCompromisedAlertDialog = false },
                            title = { Text("Security Alert: Device Compromised") },
                            text = { Text("MorNote detected root binary, custom test-keys, or tamper frameworks (e.g. Frida/Magisk) on this device. Access is restricted under active security policy.") },
                            confirmButton = {
                                TextButton(onClick = { showCompromisedAlertDialog = false }) {
                                    Text("Acknowledge")
                                }
                            }
                        )
                    }

                    val auth = Firebase.auth
                    var currentUser by remember { mutableStateOf(auth.currentUser) }

                    if (currentUser == null) {
                        LoginScreen(onLoginSuccess = {
                            currentUser = auth.currentUser
                        })
                    } else {
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        val scope = rememberCoroutineScope()
                        val navController = rememberNavController()
                        val user = auth.currentUser

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(
                                    modifier = Modifier.width(300.dp),
                                    drawerContainerColor = MaterialTheme.colorScheme.background
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
                                        ) {
                                            AsyncImage(
                                                model = user?.photoUrl,
                                                contentDescription = "User Icon",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = user?.displayName ?: "User",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = user?.email ?: "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                                        DrawerItem(
                                            icon = Icons.Default.Home,
                                            label = "Home",
                                            onClick = {
                                                scope.launch {
                                                    drawerState.close()
                                                    navController.navigate(Screen.NoteList.route) {
                                                        popUpTo(Screen.NoteList.route) { inclusive = true }
                                                    }
                                                }
                                            }
                                        )
                                        DrawerItem(
                                            icon = Icons.Default.Archive,
                                            label = "Archive",
                                            onClick = {
                                                scope.launch {
                                                    drawerState.close()
                                                    navController.navigate(Screen.Archive.route)
                                                }
                                            }
                                        )
                                        DrawerItem(
                                            icon = Icons.Default.Notifications,
                                            label = "News & Updates",
                                            onClick = {
                                                scope.launch {
                                                    drawerState.close()
                                                    navController.navigate(Screen.Feed.route)
                                                }
                                            }
                                        )
                                        DrawerItem(
                                            icon = Icons.Default.Settings,
                                            label = "Settings",
                                            onClick = {
                                                scope.launch {
                                                    drawerState.close()
                                                    navController.navigate(Screen.Settings.route)
                                                }
                                            }
                                        )
                                        
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                        
                                        DrawerItem(icon = Icons.Default.PrivacyTip, label = "Privacy")
                                        DrawerItem(icon = Icons.Default.Star, label = "Rate App")
                                        DrawerItem(icon = Icons.AutoMirrored.Filled.Help, label = "Help")

                                        Spacer(modifier = Modifier.weight(1f))

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                        Text(
                                            text = "Version 1.1.0",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.NoteList.route
                            ) {
                                composable(Screen.NoteList.route) {
                                    NoteListScreen(
                                        viewModelFactory = viewModelFactory,
                                        onNoteClick = { id ->
                                            navController.navigate(Screen.NoteDetail.createRoute(id))
                                        },
                                        onAddNoteClick = {
                                            navController.navigate(Screen.NoteDetail.createRoute(0))
                                        },
                                        onSignOut = {
                                            auth.signOut()
                                            currentUser = null
                                        },
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        }
                                    )
                                }
                                composable(
                                    route = Screen.NoteDetail.route,
                                    arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                                ) { backStackEntry ->
                                    val id = backStackEntry.arguments?.getInt("noteId") ?: 0
                                    NoteDetailScreen(
                                        noteId = id,
                                        viewModelFactory = viewModelFactory,
                                        onSaveSuccess = { navController.popBackStack() },
                                        requestBiometricAuth = { onSuccess, onMismatch, onCancel, onFallback ->
                                            showBiometricPrompt(
                                                onSuccess = onSuccess,
                                                onMismatch = onMismatch,
                                                onCancel = onCancel,
                                                onFallback = onFallback
                                            )
                                        }
                                    )
                                }
                                composable(Screen.Feed.route) {
                                    FeedScreen()
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.Archive.route) {
                                    ArchiveScreen(
                                        viewModelFactory = viewModelFactory,
                                        onNoteClick = { id ->
                                            navController.navigate(Screen.NoteDetail.createRoute(id))
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawerItem(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit = {}
    ) {
        NavigationDrawerItem(
            icon = { Icon(icon, contentDescription = null) },
            label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            onClick = onClick,
            modifier = Modifier.padding(vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onMismatch: () -> Unit,
        onCancel: () -> Unit,
        onFallback: () -> Unit
    ) {
        if (SecurityGuard.isDeviceCompromised(this)) {
            android.util.Log.w("MainActivity", "[SECURITY ALERT] Biometric entry blocked due to device compromise.")
            if (SecurityGuard.activePolicy == SecurityGuard.SecurityPolicy.EMERGENCY_WIPE) {
                com.sanjana.mornote.util.CipherEngine.purgeMasterKey()
            }
            onCancel()
            android.app.AlertDialog.Builder(this)
                .setTitle("Security Restriction")
                .setMessage("Biometric access is disabled because root access or device tampering was detected.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            onCancel()
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            onFallback()
                        }
                        else -> {
                            onFallback()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onMismatch()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Secure Note")
            .setSubtitle("Authenticate using your biometric or device credentials")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
