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

data class GameDetails(
    val note: String? = null,
    val voiceMemoPath: String? = null
)

class GameNotesViewModel(application: Application) : AndroidViewModel(application) {

    val repository: GameRepository
    val savedGames: StateFlow<List<ChessComGame>>

    // Store game details (notes and voice memos) for immediate UI updates
    private val _gameDetails = MutableStateFlow<Map<String, GameDetails>>(emptyMap())
    val gameDetails: StateFlow<Map<String, GameDetails>> = _gameDetails

    init {
        val gameDao = GameDatabase.getDatabase(application).gameDao()
        repository = GameRepository(gameDao)
        savedGames = repository.allGames.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
    }

    fun loadGameDetails(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val gameEntity = repository.getGameByUrl(url)
            if (gameEntity != null) {
                val currentDetails = _gameDetails.value.toMutableMap()
                currentDetails[url] = GameDetails(
                    note = gameEntity.note,
                    voiceMemoPath = gameEntity.voiceMemoPath
                )
                _gameDetails.value = currentDetails
            }
        }
    }

    fun addGame(game: ChessComGame) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(game)
            updateGameDetailsInMemory(game.url, null, null)
        }
    }

    fun addGameWithNote(game: ChessComGame, note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(game, note)
            updateGameDetailsInMemory(game.url, note, null)
        }
    }

    fun addGameWithVoiceMemo(game: ChessComGame, voiceMemoPath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(game, voiceMemoPath = voiceMemoPath)
            updateGameDetailsInMemory(game.url, null, voiceMemoPath)
        }
    }

    fun updateNote(url: String, note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(url, note)

            // Update the in-memory state immediately
            val currentDetails = _gameDetails.value.toMutableMap()
            val currentGameDetails = currentDetails[url] ?: GameDetails()
            currentDetails[url] = currentGameDetails.copy(note = note)
            _gameDetails.value = currentDetails
        }
    }

    fun updateVoiceMemo(url: String, path: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVoiceMemo(url, path)

            // Update the in-memory state immediately
            val currentDetails = _gameDetails.value.toMutableMap()
            val currentGameDetails = currentDetails[url] ?: GameDetails()
            currentDetails[url] = currentGameDetails.copy(voiceMemoPath = path)
            _gameDetails.value = currentDetails
        }
    }

    fun deleteGame(game: ChessComGame) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(game)

            // Remove the game details from in-memory state
            val currentDetails = _gameDetails.value.toMutableMap()
            currentDetails.remove(game.url)
            _gameDetails.value = currentDetails
        }
    }

    private fun updateGameDetailsInMemory(url: String, note: String?, voiceMemoPath: String?) {
        val currentDetails = _gameDetails.value.toMutableMap()
        val currentGameDetails = currentDetails[url] ?: GameDetails()

        currentDetails[url] = GameDetails(
            note = note ?: currentGameDetails.note,
            voiceMemoPath = voiceMemoPath ?: currentGameDetails.voiceMemoPath
        )

        _gameDetails.value = currentDetails
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