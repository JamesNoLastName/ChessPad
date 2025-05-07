package com.example.chesspad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameNoteDao {
    @Query("SELECT * FROM game_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<GameNote>>

    @Query("SELECT * FROM game_notes WHERE gameUrl = :gameUrl LIMIT 1")
    suspend fun getNoteForGame(gameUrl: String): GameNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: GameNote)

    @Delete
    suspend fun deleteNote(note: GameNote)
}
