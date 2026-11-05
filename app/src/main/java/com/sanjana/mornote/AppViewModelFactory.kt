package com.sanjana.mornote
// AppViewModelFactory.kt

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.data.ThemeRepository
import com.sanjana.mornote.ui.settings.SettingsViewModel

class AppViewModelFactory(
    private val application: Application,
    private val repository: NoteRepository,
    private val themeRepository: ThemeRepository,
    private val aiSettingsRepository: AiSettingsRepository
    ) : ViewModelProvider.Factory {

    // This allows us to create different ViewModels by checking the modelClass
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteListViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteDetailViewModel(application, repository, aiSettingsRepository) as T
        } else if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(themeRepository, aiSettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
