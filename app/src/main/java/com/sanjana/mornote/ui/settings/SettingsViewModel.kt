package com.sanjana.mornote.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanjana.mornote.data.AiMode
import com.sanjana.mornote.data.AiProvider
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.data.ThemeMode
import com.sanjana.mornote.data.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val aiSettingsRepository: AiSettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val aiMode: StateFlow<AiMode> = aiSettingsRepository.aiMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiMode.PRIVATE
        )

    val aiProvider: StateFlow<AiProvider> = aiSettingsRepository.aiProvider
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiProvider.GEMINI
        )

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    fun onAiModeSelected(mode: AiMode) {
        viewModelScope.launch {
            aiSettingsRepository.setAiMode(mode)
        }
    }

    fun onAiProviderSelected(provider: AiProvider) {
        viewModelScope.launch {
            aiSettingsRepository.setAiProvider(provider)
        }
    }
}
