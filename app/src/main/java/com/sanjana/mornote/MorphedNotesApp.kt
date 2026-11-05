package com.sanjana.mornote
// MorphedNotesApp.kt

import android.app.Application
import androidx.room.Room
import com.sanjana.mornote.data.AiSettingsRepository
import com.sanjana.mornote.data.RemoteConfigManager
import com.sanjana.mornote.data.ThemeRepository

class MorphedNotesApp : Application() {

    // Lazy initialization ensures it's created only when first accessed
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "morse_notes_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()
    }

    // Initialize the Repository with the database instance
    val repository by lazy {
        NoteRepository(database)
    }

    val themeRepository by lazy {
        ThemeRepository(applicationContext)
    }

    val aiSettingsRepository by lazy {
        AiSettingsRepository(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize Remote Config here!
        RemoteConfigManager.init()
    }
}