package com.example.chesspad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesspad.ui.theme.ChessPadTheme
import kotlinx.coroutines.delay

// Google Maps Compose imports
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

// Updated data model including location (latitude & longitude)
data class ChessGame(
    val title: String,
    val date: String,
    val moves: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessPadTheme {
                ChessPadApp()
            }
        }
    }
}


@Composable
fun ChessPadApp() {
    var showSplashScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        showSplashScreen = false
    }

    Crossfade(targetState = showSplashScreen) { isSplash ->
        if (isSplash) {
            SplashScreen()
        } else {
            ChessGamesScreen()
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chesspadlogo),
            contentDescription = "ChessPad Logo",
            modifier = Modifier.size(250.dp)
        )
    }
}


@Composable
fun ChessGamesScreen() {
    var games by remember { mutableStateOf(sampleGames()) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<ChessGame?>(null) }
    var isAddGameDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddGameDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Game")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ChessPad",
                    fontSize = 24.sp,
                    color = Color.White
                )
                IconButton(
                    onClick = { isSettingsOpen = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            ChessGamesGrid(
                games = games,
                onGameClick = { selectedGame = it },
                modifier = Modifier.padding(innerPadding)
            )
        }

        if (isSettingsOpen) {
            SettingsPanel(onClose = { isSettingsOpen = false })
        }

        selectedGame?.let {
            GameDetailsPanel(game = it, onClose = { selectedGame = null })
        }
    }

    if (isAddGameDialogOpen) {
        AddGameDialog(onDismiss = { isAddGameDialogOpen = false }) { newGame ->
            games = games + newGame
            isAddGameDialogOpen = false
        }
    }
}


@Composable
fun ChessGamesGrid(games: List<ChessGame>, onGameClick: (ChessGame) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.padding(start = 16.dp, end = 16.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(games) { game ->
            ChessGameCard(game, onClick = { onGameClick(game) })
        }
    }
}


@Composable
fun ChessGameCard(game: ChessGame, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = game.date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun AddGameDialog(onDismiss: () -> Unit, onGameAdded: (ChessGame) -> Unit) {
    var gameName by remember { mutableStateOf("") }
    var gameTime by remember { mutableStateOf("") }
    var gameLat by remember { mutableStateOf<Double?>(null) }
    var gameLng by remember { mutableStateOf<Double?>(null) }
    var showMap by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Game") },
        text = {
            Column {
                TextField(
                    value = gameName,
                    onValueChange = { gameName = it },
                    label = { Text("Game Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = gameTime,
                    onValueChange = { gameTime = it },
                    label = { Text("Game Time") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showMap = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (gameLat != null && gameLng != null)
                            "Change Game Location" else "Set Game Location"
                    )
                }
                if (gameLat != null && gameLng != null) {
                    Text(text = "Location: ($gameLat, $gameLng)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (gameName.isNotEmpty() && gameTime.isNotEmpty()) {
                        onGameAdded(ChessGame(gameName, gameTime, "", gameLat, gameLng))
                    }
                }
            ) {
                Text("Add Game")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )


    if (showMap) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { /* Consume clicks to prevent dismissal */ },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                MapScreen(
                    selectedLocation = if (gameLat != null && gameLng != null)
                        LatLng(gameLat!!, gameLng!!) else null,
                    onLocationSelected = { latLng ->
                        gameLat = latLng.latitude
                        gameLng = latLng.longitude
                        showMap = false
                    }
                )
            }
        }
    }
}


@Composable
fun GameDetailsPanel(game: ChessGame, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.6f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = game.title,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Date: ${game.date}",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Moves:\n${game.moves}",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left
                )
                // If location is saved, show it
                if (game.latitude != null && game.longitude != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Location: (${game.latitude}, ${game.longitude})",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun SettingsPanel(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settings (Add this later!)", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Okay.")
                }
            }
        }
    }
}


fun sampleGames(): List<ChessGame> {
    return listOf(
        ChessGame("Archived Game 1", "Month Day Year", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6"),
        ChessGame("Archived Game 2", "Month Day Year", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6"),
        ChessGame("Archived Game 3", "Month Day Year", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6"),
        ChessGame("Archived Game 4", "Month Day Year", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6"),
        ChessGame("Archived Game 5", "Month Day Year", "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6")

    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChessPadTheme {
        ChessPadApp()
    }
}

@Composable
fun MapScreen(
    initialLocation: LatLng = LatLng(37.7749, -122.4194),
    onLocationSelected: (LatLng) -> Unit,
    selectedLocation: LatLng? = null
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLongClick = { latLng -> onLocationSelected(latLng) }
    ) {
        selectedLocation?.let {
            Marker(
                state = rememberMarkerState(position = it),
                title = "Game Location",
                snippet = "Chosen location"
            )
        }
    }
}