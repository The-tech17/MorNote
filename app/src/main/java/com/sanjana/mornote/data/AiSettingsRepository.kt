package com.sanjana.mornote.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_settings")

class AiSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val AI_MODE = stringPreferencesKey("ai_mode")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
    }

    val aiMode: Flow<AiMode> = context.aiDataStore.data.map { preferences ->
        val modeName = preferences[PreferencesKeys.AI_MODE] ?: AiMode.PRIVATE.name
        try {
            AiMode.valueOf(modeName)
        } catch (e: Exception) {
            AiMode.PRIVATE
        }
    }

    val aiProvider: Flow<AiProvider> = context.aiDataStore.data.map { preferences ->
        val providerName = preferences[PreferencesKeys.AI_PROVIDER] ?: AiProvider.GEMINI.name
        try {
            AiProvider.valueOf(providerName)
        } catch (e: Exception) {
            AiProvider.GEMINI
        }
    }

    suspend fun setAiMode(mode: AiMode) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_MODE] = mode.name
        }
    }

    suspend fun setAiProvider(provider: AiProvider) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] = provider.name
        }
    }
}
