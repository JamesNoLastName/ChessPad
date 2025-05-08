package com.example.chesspad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class GameNotesViewModel(application: Application) : AndroidViewModel(application) {

    val repository: GameRepository
    val savedGames: StateFlow<List<ChessComGame>>

    init {
        val gameDao = GameDatabase.getDatabase(application).gameDao()
        repository = GameRepository(gameDao)
        savedGames = repository.allGames.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
    }

    fun addGame(game: ChessComGame) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(game)
        }
    }

    fun addGameWithNote(game: ChessComGame, note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(game, note)
        }
    }

    fun addGameWithVoiceMemo(game: ChessComGame, voiceMemoPath: String?) {
        viewModelScope.launch(Dispatchers.IO) { // Ensure this runs in the background thread
            repository.insert(game, voiceMemoPath = voiceMemoPath)
        }
    }

    fun updateNote(url: String, note: String?) {
        viewModelScope.launch(Dispatchers.IO) { // Ensure this runs in the background thread
            repository.updateNote(url, note)
        }
    }

    fun updateVoiceMemo(url: String, path: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVoiceMemo(url, path)
        }
    }

    fun deleteGame(game: ChessComGame) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(game)
        }
    }

    class GameNotesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameNotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameNotesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
