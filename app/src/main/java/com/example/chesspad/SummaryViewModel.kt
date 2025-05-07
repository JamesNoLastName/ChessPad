package com.example.chesspad

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SummaryViewModel : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _gamesPlayed = MutableStateFlow(0)
    val gamesPlayed: StateFlow<Int> = _gamesPlayed

    private val _winRate = MutableStateFlow(0f)
    val winRate: StateFlow<Float> = _winRate

    private val _favoriteOpening = MutableStateFlow<String?>(null)
    val favoriteOpening: StateFlow<String?> = _favoriteOpening

    private val _topOpponent = MutableStateFlow<String?>(null)
    val topOpponent: StateFlow<String?> = _topOpponent

    fun setSummaryData(
        username: String,
        gamesPlayed: Int,
        winRate: Float,
        favoriteOpening: String?,
        topOpponent: String?
    ) {
        _username.value = username
        _gamesPlayed.value = gamesPlayed
        _winRate.value = winRate
        _favoriteOpening.value = favoriteOpening
        _topOpponent.value = topOpponent
    }

    fun loadUserStats(username: String) {
        viewModelScope.launch {
            val stats = ChessComRepository.fetchStats(username)
            setSummaryData(
                username = stats.username,
                gamesPlayed = stats.gamesPlayed,
                winRate = stats.winRate,
                favoriteOpening = stats.favoriteOpening,
                topOpponent = stats.topOpponent
            )
        }
    }
    fun processGames(username: String, games: List<ChessComGame>) {
        val wins = games.count {
            (it.white == username && it.whiteResult == "win") ||
                    (it.black == username && it.blackResult == "win")
        }
        val totalGames = games.size
        val winRate = if (totalGames > 0) wins.toFloat() / totalGames else 0f

        val opponents = games.map {
            if (it.white == username) it.black else it.white
        }
        val topOpponent = opponents.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

        // ✅ NEW: compute favorite opening
        val favoriteOpening = games
            .mapNotNull { it.opening } // make sure opening is not null
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        setSummaryData(
            username = username,
            gamesPlayed = totalGames,
            winRate = winRate,
            favoriteOpening = favoriteOpening,
            topOpponent = topOpponent
        )
    }
}
