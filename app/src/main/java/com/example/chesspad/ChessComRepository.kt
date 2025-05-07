package com.example.chesspad

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object ChessComRepository {
    suspend fun fetchStats(username: String): ChessStats {
        return withContext(Dispatchers.IO) {
            val url = "https://api.chess.com/pub/player/$username/stats"
            val response = URL(url).readText()
            val json = JSONObject(response)

            val gamesPlayed = json.getJSONObject("chess_rapid")
                .getJSONObject("record")
                .getInt("win") + json.getJSONObject("chess_rapid")
                .getJSONObject("record")
                .getInt("loss") + json.getJSONObject("chess_rapid")
                .getJSONObject("record")
                .getInt("draw")

            val wins = json.getJSONObject("chess_rapid")
                .getJSONObject("record")
                .getInt("win")

            val winRate = if (gamesPlayed > 0) (wins.toFloat() / gamesPlayed) * 100 else 0f

            ChessStats(username, gamesPlayed, winRate, favoriteOpening = null, topOpponent = null)
        }
    }
}

data class ChessStats(
    val username: String,
    val gamesPlayed: Int,
    val winRate: Float,
    val favoriteOpening: String?,
    val topOpponent: String?
)
