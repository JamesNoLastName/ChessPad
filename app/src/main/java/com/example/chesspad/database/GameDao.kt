package com.example.chesspad.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY id DESC")
    fun getAllGames(): Flow<List<ChessGame>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: ChessGame)

    @Delete
    suspend fun deleteGame(game: ChessGame)
}
