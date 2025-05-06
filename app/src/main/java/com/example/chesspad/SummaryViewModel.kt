package com.example.chesspad

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
}
