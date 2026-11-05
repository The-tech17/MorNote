package com.sanjana.mornote.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = Firebase.auth
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- ADDED HEADER ---
            Text(
                text = "MorNote",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your Secure Diary",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("811365862358-46il8imaetaao5j4d0p57flpl2hp614p.apps.googleusercontent.com")
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        try {
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                                // Sign in to Firebase
                                auth.signInWithCredential(firebaseCredential).await()

                                // CRITICAL: Call the success callback to update MainActivity
                                onLoginSuccess()
                            }
                        } catch (_: NoCredentialException) {
                            snackbarHostState.showSnackbar("No Google accounts found on this device.")
                        } catch (_: GetCredentialCancellationException) {
                            // User cancelled, usually no need to show a snackbar unless you want to
                        } catch (e: GetCredentialException) {
                            snackbarHostState.showSnackbar("Sign-in failed: ${e.message}")
                            e.printStackTrace()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("An unexpected error occurred.")
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.height(56.dp).padding(horizontal = 32.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Sign in with Google")
            }
        }
    }
}
