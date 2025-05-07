package com.example.chesspad

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import org.json.JSONObject
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import java.net.URL
import java.util.Calendar
import kotlin.Result

suspend fun fetchChessComGames(username: String, startYear: Int, startMonth: Int, endYear: Int, endMonth: Int, maxGames: Int = 100): Result<List<ChessComGame>> = withContext(Dispatchers.IO) {
    try {
        val urls = mutableListOf<String>()
        var year = startYear
        var month = startMonth
        while (year < endYear || (year == endYear && month <= endMonth)) {
            urls.add("https://api.chess.com/pub/player/${username}/games/$year/${"%02d".format(month)}")
            if (month == 12) {
                month = 1
                year += 1
            } else {
                month += 1
            }
        }
        val allGames = mutableListOf<ChessComGame>()
        for (apiUrl in urls) {
            try {
                val response = URL(apiUrl).readText()
                val json = JSONObject(response)
                val gamesJson = json.getJSONArray("games")
                for (i in 0 until gamesJson.length()) {
                    if (allGames.size >= maxGames) break
                    val g = gamesJson.getJSONObject(i)
                    val whiteObj = g.getJSONObject("white")
                    val blackObj = g.getJSONObject("black")
                    val pgn = g.optString("pgn", "")
                    allGames.add(
                        ChessComGame(
                            url = g.getString("url"),
                            white = whiteObj.getString("username"),
                            whiteResult = whiteObj.optString("result", ""),
                            black = blackObj.getString("username"),
                            blackResult = blackObj.optString("result", ""),
                            endTime = g.optLong("end_time", 0L),
                            pgn = pgn,
                            opening = g.optString("opening", null)
                        )
                    )
                }
            } catch (_: Exception) {
                // Ignore errors for missing months
            }
        }
        Result.success(allGames)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

data class ChessComGame(
    val url: String,
    val white: String,
    val whiteResult: String,
    val black: String,
    val blackResult: String,
    val endTime: Long,
    val pgn: String,
    val opening: String?
)

@Composable
fun ChessComSyncScreen(
    gameNotesViewModel: GameNotesViewModel,
    onUsernameEntered: (String, Int, Int, Int, Int, List<ChessComGame>) -> Unit,
    onGoToSummary: () -> Unit,
    onFilesClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var games by remember { mutableStateOf<List<ChessComGame>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNextButton by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 20

    // Date filter state
    val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) + 1
    var startYear by remember { mutableStateOf(currentYear) }
    var startMonth by remember { mutableStateOf(currentMonth) }
    var endYear by remember { mutableStateOf(currentYear) }
    var endMonth by remember { mutableStateOf(currentMonth) }

    // Notes state: Map game.url -> note
    var notes by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var editingNoteForUrl by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }

    // Voice memo state: Map game.url -> file path
    var voiceMemos by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var recordingForUrl by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var playingForUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    val pagedGames = games.take((currentPage + 1) * pageSize)
    val canLoadMore = games.size > pagedGames.size || games.size == (currentPage + 1) * pageSize

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                onSearchClick = { /* Handle Search click */ },
                onFilesClick = onFilesClick,
                isSearchSelected = currentPage == 1 // for example
            )
        }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("From:", modifier = Modifier.padding(end = 8.dp))
                YearMonthDropdown(year = startYear, month = startMonth,
                    onYearChange = { startYear = it },
                    onMonthChange = { startMonth = it })
                Spacer(modifier = Modifier.width(16.dp))
                Text("To:", modifier = Modifier.padding(end = 8.dp))
                YearMonthDropdown(year = endYear, month = endMonth,
                    onYearChange = { endYear = it },
                    onMonthChange = { endMonth = it })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        error = null
                        isLoading = true
                        games = emptyList()
                        showNextButton = false
                        currentPage = 0
                        coroutineScope.launch {
                            val result = fetchChessComGames(username, startYear, startMonth, endYear, endMonth, maxGames = 100)
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
                    enabled = username.isNotBlank() && !isLoading,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Sync Games")
                }

                Button(
                    onClick = {
                        onUsernameEntered(username, startYear, startMonth, endYear, endMonth, games)
                    },
                    enabled = showNextButton,
                    modifier = Modifier.padding(8.dp)
                ) {
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
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(pagedGames) { game ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("${game.white} vs ${game.black}")
                                Text("Result: ${game.whiteResult} - ${game.blackResult}")
                                val uriHandler = LocalUriHandler.current
                                TextButton(onClick = { uriHandler.openUri(game.url) }) {
                                    Text("View on Chess.com", color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween // To space them apart
                                ) {
                                    Button(onClick = {
                                        editingNoteForUrl = game.url
                                        noteText = notes[game.url] ?: ""
                                    }) {
                                        Text(if (notes[game.url].isNullOrBlank()) "Add Note" else "Edit Note")
                                    }
                                    Button(onClick = {
                                        gameNotesViewModel.addGame(game)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Game added to notes!")
                                        }
                                    }) {
                                        Text("+")
                                    }
                                }
                                if (!notes[game.url].isNullOrBlank()) {
                                    Text("Note: ${notes[game.url]}", modifier = Modifier.padding(top = 4.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Voice Memo Buttons
                                Row {
                                    Button(onClick = {
                                        if (isRecording) {
                                            recorder?.apply {
                                                stop()
                                                release()
                                            }
                                            recorder = null
                                            isRecording = false
                                            recordingForUrl?.let { url ->
                                                // Save file path
                                                voiceMemos = voiceMemos.toMutableMap().apply {
                                                    put(url, getVoiceMemoFilePath(context, url))
                                                }
                                            }
                                            recordingForUrl = null
                                        } else {
                                            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                                                val filePath = getVoiceMemoFilePath(context, game.url)
                                                recorder = MediaRecorder().apply {
                                                    setAudioSource(MediaRecorder.AudioSource.MIC)
                                                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                                    setOutputFile(filePath)
                                                    prepare()
                                                    start()
                                                }
                                                recordingForUrl = game.url
                                                isRecording = true
                                            } else {
                                                // Show error or request permission
                                            }
                                        }
                                    }) {
                                        Text(if (isRecording && recordingForUrl == game.url) "Stop Recording" else "Record Voice Memo")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        val filePath = voiceMemos[game.url]
                                        if (!filePath.isNullOrBlank() && File(filePath).exists()) {
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(filePath)
                                                prepare()
                                                start()
                                                setOnCompletionListener {
                                                    playingForUrl = null
                                                }
                                            }
                                            playingForUrl = game.url
                                        }
                                    }, enabled = voiceMemos[game.url]?.let { File(it).exists() } == true) {
                                        Text(if (playingForUrl == game.url) "Playing..." else "Play Voice Memo")
                                    }
                                }
                                if (voiceMemos[game.url]?.let { File(it).exists() } == true) {
                                    Text("Voice memo attached", modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                    if (canLoadMore) {
                        item {
                            Button(
                                onClick = { onGoToSummary() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text("Go to Summary")
                            }
                        }
                    }
                }
            }
        }

        // Note dialog
        if (editingNoteForUrl != null) {
            AlertDialog(
                onDismissRequest = { editingNoteForUrl = null },
                confirmButton = {
                    Button(onClick = {
                        notes = notes.toMutableMap().apply { put(editingNoteForUrl!!, noteText) }
                        editingNoteForUrl = null
                    }) { Text("Save") }
                },
                dismissButton = {
                    Button(onClick = { editingNoteForUrl = null }) { Text("Cancel") }
                },
                title = { Text("Game Note") },
                text = {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Enter your note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    }
}


@Composable
fun BottomNavBar(
    onSearchClick: () -> Unit,
    onFilesClick: () -> Unit,
    isSearchSelected: Boolean // Pass this from parent to highlight the selected tab
) {
    Surface(
        color = Color(0xFF332B50), // Darker purple for the navbar
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp), // Increased height
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Section (highlighted if selected)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSearchSelected) Color(0xFF4B3F74) else Color.Transparent) // Highlight search tab
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mag),
                    contentDescription = "Search",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Divider between Search and Files
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Black)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (!isSearchSelected) Color(0xFF4B3F74) else Color.Transparent) // Highlight files tab when search is not selected
                    .clickable { onFilesClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.notes),
                    contentDescription = "Files",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}



@Composable
fun YearMonthDropdown(year: Int, month: Int, onYearChange: (Int) -> Unit, onMonthChange: (Int) -> Unit) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 10..currentYear).toList().reversed()
    val months = (1..12).toList()
    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    Box {
        Row {
            Box {
                TextButton(onClick = { expandedYear = true }) {
                    Text(year.toString())
                }
                DropdownMenu(expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
                    years.forEach {
                        DropdownMenuItem(text = { Text(it.toString()) }, onClick = {
                            onYearChange(it)
                            expandedYear = false
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box {
                TextButton(onClick = { expandedMonth = true }) {
                    Text(month.toString().padStart(2, '0'))
                }
                DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                    months.forEach {
                        DropdownMenuItem(text = { Text(it.toString().padStart(2, '0')) }, onClick = {
                            onMonthChange(it)
                            expandedMonth = false
                        })
                    }
                }
            }
        }
    }
}

fun getVoiceMemoFilePath(context: Context, url: String): String {
    val fileName = url.hashCode().toString() + ".3gp"
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    return File(dir, fileName).absolutePath
}
