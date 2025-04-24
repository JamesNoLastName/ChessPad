package com.example.chesspad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import kotlin.Result

@Composable
fun ChessComSyncScreen(onUsernameEntered: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var games by remember { mutableStateOf<List<ChessComGame>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNextButton by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 20
    val pagedGames = games.take((currentPage + 1) * pageSize)
    val canLoadMore = games.size > pagedGames.size || games.size == (currentPage + 1) * pageSize

    suspend fun fetchChessComGames(username: String, maxGames: Int = 100): Result<List<ChessComGame>> = withContext(Dispatchers.IO) {
        try {
            val now = java.util.Calendar.getInstance()
            val year = now.get(java.util.Calendar.YEAR)
            val month = now.get(java.util.Calendar.MONTH) + 1
            val apiUrl = "https://api.chess.com/pub/player/${username}/games/$year/${"%02d".format(month)}"
            val response = URL(apiUrl).readText()
            val json = JSONObject(response)
            val gamesJson = json.getJSONArray("games")
            val games = mutableListOf<ChessComGame>()
            for (i in 0 until gamesJson.length()) {
                if (games.size >= maxGames) break
                val g = gamesJson.getJSONObject(i)
                games.add(
                    ChessComGame(
                        url = g.getString("url"),
                        white = g.getJSONObject("white").getString("username"),
                        black = g.getJSONObject("black").getString("username"),
                        result = g.optString("result", ""),
                        endTime = g.optLong("end_time", 0L)
                    )
                )
            }
            Result.success(games)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sync Chess.com Games", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Chess.com Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    error = null
                    isLoading = true
                    games = emptyList()
                    showNextButton = false
                    currentPage = 0
                    coroutineScope.launch {
                        val result = fetchChessComGames(username, maxGames = 100)
                        isLoading = false
                        if (result.isSuccess) {
                            games = result.getOrDefault(emptyList())
                            snackbarHostState.showSnackbar("Data fetched successfully!")
                            showNextButton = true
                        } else {
                            error = result.exceptionOrNull()?.message ?: "This user does not exist."
                            snackbarHostState.showSnackbar(error!!)
                        }
                    }
                },
                enabled = username.isNotBlank() && !isLoading
            ) {
                Text("Sync Games")
            }
            if (showNextButton) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onUsernameEntered(username) }) {
                    Text("Go to Next Page")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (games.isNotEmpty()) {
                Text("Recent Games:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(pagedGames) { game ->
                        ChessComGameItem(game)
                    }
                    if (canLoadMore) {
                        item {
                            Button(
                                onClick = { currentPage += 1 },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text("Load More Games")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ChessComGame(
    val url: String,
    val white: String,
    val black: String,
    val result: String,
    val endTime: Long
)

@Composable
fun ChessComGameItem(game: ChessComGame) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("${game.white} vs ${game.black}")
            Text("Result: ${game.result}")
            val uriHandler = LocalUriHandler.current
            TextButton(onClick = { uriHandler.openUri(game.url) }) {
                Text("View on Chess.com", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
