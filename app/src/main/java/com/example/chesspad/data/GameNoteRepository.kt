package com.example.chesspad.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class GameNoteRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).gameNoteDao()

    fun getAllNotes(): Flow<List<GameNote>> = dao.getAllNotes()

    suspend fun getNoteForGame(gameUrl: String): GameNote? = dao.getNoteForGame(gameUrl)

    suspend fun upsertNote(note: GameNote) = dao.upsertNote(note)

    suspend fun deleteNote(note: GameNote) = dao.deleteNote(note)
}
