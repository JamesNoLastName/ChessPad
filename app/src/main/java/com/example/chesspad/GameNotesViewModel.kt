package com.example.chesspad

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameNotesViewModel : ViewModel() {
    private val _savedGames = MutableStateFlow<List<ChessComGame>>(emptyList())
    val savedGames: StateFlow<List<ChessComGame>> = _savedGames

    fun addGame(game: ChessComGame) {
        if (game !in _savedGames.value) {
            _savedGames.value = _savedGames.value + game
        }
    }
}
