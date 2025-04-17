package com.example.chesspad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chesspad.database.AppDatabase
import com.example.chesspad.database.ChessGame
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameDao = AppDatabase.getDatabase(application).gameDao()

    val games: StateFlow<List<ChessGame>> = gameDao.getAllGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGame(game: ChessGame) {
        viewModelScope.launch {
            gameDao.insertGame(game)
        }
    }

    fun deleteGame(game: ChessGame) {
        viewModelScope.launch {
            gameDao.deleteGame(game)
        }
    }
}
