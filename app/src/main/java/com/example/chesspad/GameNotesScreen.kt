package com.example.chesspad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.io.File
import android.media.MediaPlayer
import kotlinx.coroutines.launch

@Composable
fun GameNotesScreen(
    games: List<ChessComGame>,  // This will be unused as we get data from viewModel
    onDone: () -> Unit,
    navController: NavHostController
) {
    // Get application context for ViewModel factory
    val context = LocalContext.current
    val gameNotesViewModel: GameNotesViewModel = viewModel(
        factory = GameNotesViewModel.GameNotesViewModelFactory(context.applicationContext as ChessPadApplication)
    )

    // Collect saved games from database via StateFlow
    val savedGames = gameNotesViewModel.savedGames.collectAsState().value

    var editingNoteForUrl by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var isSearchSelected by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Media player for voice memos
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var playingGameUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FBottomNavBar(
                onSearchClick = {
                    navController.navigate("sync") {
                        popUpTo("notes") { inclusive = true }
                    }
                },
                onFilesClick = { isSearchSelected = false },
                isSearchSelected = isSearchSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Your Saved Games",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (savedGames.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No saved games yet. Go to Sync to add games.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(savedGames) { game ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("${game.white} vs ${game.black}")
                                Text("Result: ${game.whiteResult} - ${game.blackResult}")

                                // Get the note and voice memo for this game
                                var note by remember { mutableStateOf<String?>(null) }
                                var voiceMemoPath by remember { mutableStateOf<String?>(null) }

                                // Load note and voice memo from database
                                LaunchedEffect(game.url) {
                                    val gameEntity = gameNotesViewModel.repository.getGameByUrl(game.url)
                                    note = gameEntity?.note
                                    voiceMemoPath = gameEntity?.voiceMemoPath
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(onClick = {
                                        editingNoteForUrl = game.url
                                        noteText = note ?: ""
                                    }) {
                                        Text(if (note.isNullOrBlank()) "Add Note" else "Edit Note")
                                    }

                                    Button(onClick = {
                                        coroutineScope.launch {
                                            gameNotesViewModel.deleteGame(game)
                                            snackbarHostState.showSnackbar("Game removed from notes")
                                        }
                                    }) {
                                        Text("Delete")
                                    }
                                }

                                if (!note.isNullOrBlank()) {
                                    Text("Note: $note", modifier = Modifier.padding(top = 4.dp))
                                }

                                // Voice memo playback section
                                if (!voiceMemoPath.isNullOrBlank() && File(voiceMemoPath!!).exists()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(onClick = {
                                        if (playingGameUrl == game.url) {
                                            // Stop playback
                                            mediaPlayer?.stop()
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                            playingGameUrl = null
                                        } else {
                                            // Start playback
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(voiceMemoPath)
                                                prepare()
                                                start()
                                                setOnCompletionListener {
                                                    playingGameUrl = null
                                                }
                                            }
                                            playingGameUrl = game.url
                                        }
                                    }) {
                                        Text(if (playingGameUrl == game.url) "Stop" else "Play Voice Memo")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Note editing dialog
    if (editingNoteForUrl != null) {
        AlertDialog(
            onDismissRequest = { editingNoteForUrl = null },
            confirmButton = {
                Button(onClick = {
                    val url = editingNoteForUrl ?: return@Button
                    coroutineScope.launch {
                        gameNotesViewModel.updateNote(url, noteText)
                        snackbarHostState.showSnackbar("Note saved")
                    }
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

    // Clean up media player when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}

// BottomNavBar composable
@Composable
fun FBottomNavBar(
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
