package com.sanjana.mornote

import kotlinx.coroutines.flow.Flow

class NoteRepository(database: AppDatabase) {
    private val noteDao: NoteDao = database.noteDao()

    fun getAllNotes(): Flow<List<NoteEntity>> {
        return noteDao.getAllNotes()
    }

    fun getArchivedNotes(): Flow<List<NoteEntity>> {
        return noteDao.getArchivedNotes()
    }

    suspend fun insertNote(title: String, content: String) {
        val note = NoteEntity(
            title = title,
            contentMorse = content,
            timestamp = System.currentTimeMillis(),
        )
        noteDao.insert(note)
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.update(note)
    }

    suspend fun getNoteEntityById(noteId: Int): NoteEntity? {
        return noteDao.getNoteById(noteId)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
    }

    suspend fun togglePin(note: NoteEntity) {
        noteDao.update(note.copy(isPinned = !note.isPinned))
    }

    suspend fun toggleArchive(note: NoteEntity) {
        noteDao.update(note.copy(isArchived = !note.isArchived))
    }

    suspend fun getAllNotesRaw(): List<NoteEntity> {
        return noteDao.getAllNotesRaw()
    }

    suspend fun insertAll(notes: List<NoteEntity>) {
        noteDao.insertAll(notes)
    }
}
