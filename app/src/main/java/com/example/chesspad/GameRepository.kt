package com.example.chesspad

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {

    val allGames: Flow<List<ChessComGame>> = gameDao.getAllGames().map { entities ->
        entities.map { it.toChessComGame() }
    }

    suspend fun insert(game: ChessComGame, note: String? = null, voiceMemoPath: String? = null) {
        gameDao.insert(game.toGameEntity(note, voiceMemoPath))
    }

    suspend fun update(gameEntity: GameEntity) {
        gameDao.update(gameEntity)
    }

    suspend fun delete(game: ChessComGame) {
        val entity = gameDao.getGameByUrl(game.url) ?: return
        gameDao.delete(entity)
    }

    suspend fun updateNote(url: String, note: String?) {
        gameDao.updateNote(url, note)
    }

    suspend fun updateVoiceMemo(url: String, voiceMemoPath: String?) {
        gameDao.updateVoiceMemo(url, voiceMemoPath)
    }

    suspend fun getGameByUrl(url: String): GameEntity? {
        return gameDao.getGameByUrl(url)
    }

    suspend fun deleteAllGames() {
        gameDao.deleteAllGames()
    }
}