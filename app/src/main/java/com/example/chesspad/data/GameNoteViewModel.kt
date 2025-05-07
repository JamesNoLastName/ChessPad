package com.example.chesspad.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameNoteViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = GameNoteRepository(app)

    val allNotes: StateFlow<List<GameNote>> = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsertNote(gameUrl: String, noteText: String) {
        viewModelScope.launch {
            repo.upsertNote(GameNote(gameUrl = gameUrl, noteText = noteText))
        }
    }

    fun deleteNote(gameUrl: String) {
        viewModelScope.launch {
            repo.getNoteForGame(gameUrl)?.let { repo.deleteNote(it) }
        }
    }
}
