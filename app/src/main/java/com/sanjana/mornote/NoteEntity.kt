package com.sanjana.mornote

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val contentMorse: String,
    val timestamp: Long = Date().time,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList()
)
