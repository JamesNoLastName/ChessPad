package com.example.chesspad

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_table ORDER BY endTime DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameEntity: GameEntity)

    @Update
    suspend fun update(gameEntity: GameEntity)

    @Delete
    suspend fun delete(gameEntity: GameEntity)

    @Query("DELETE FROM game_table")
    suspend fun deleteAllGames()

    @Query("SELECT * FROM game_table WHERE url = :url")
    suspend fun getGameByUrl(url: String): GameEntity?

    @Query("UPDATE game_table SET note = :note WHERE url = :url")
    suspend fun updateNote(url: String, note: String?)

    @Query("UPDATE game_table SET voiceMemoPath = :voiceMemoPath WHERE url = :url")
    suspend fun updateVoiceMemo(url: String, voiceMemoPath: String?)
}