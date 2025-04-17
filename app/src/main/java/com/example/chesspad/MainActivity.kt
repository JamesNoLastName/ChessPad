package com.example.chesspad

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chesspad.database.ChessGame
import com.example.chesspad.ui.theme.ChessPadTheme
import com.example.chesspad.GameViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay

import androidx.compose.ui.window.Dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import no.bakkenbaeck.chessboardeditor.view.board.ChessBoardView

import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Check

import com.example.chesspad.ui.theme.ChessPadTheme

private const val STANDARD_START_FEN =
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

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
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )

    var showSplashScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        showSplashScreen = false
    }

    Crossfade(targetState = showSplashScreen) { isSplash ->
        if (isSplash) {
            SplashScreen()
        } else {
            ChessGamesScreen(viewModel)
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
fun ChessGamesScreen(viewModel: GameViewModel) {
    val games by viewModel.games.collectAsState()

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
            GameDetailsPanel(game = it, onClose = { selectedGame = null }, onDelete = {
                viewModel.deleteGame(it)
                selectedGame = null
            })
        }
    }

    if (isAddGameDialogOpen) {
        AddGameDialog(onDismiss = { isAddGameDialogOpen = false }) { newGame ->
            viewModel.addGame(newGame)
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
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var showMap by remember { mutableStateOf(false) }
    var showBoard by remember { mutableStateOf(false) }

    if (showMap) {
        Dialog(onDismissRequest = { showMap = false }) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                MapScreen(
                    selectedLocation = lat?.let { la -> lng?.let { lo -> LatLng(la, lo) } },
                    onLocationSelected = {
                        lat = it.latitude
                        lng = it.longitude
                        showMap = false
                    }
                )
            }
        }
    }

    if (showBoard) {
        Dialog(
            onDismissRequest = {   },
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // take the full width
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            ) {
                AndroidView(
                    factory = { ctx ->
                        ChessBoardView(ctx).apply {
                            setFen(STANDARD_START_FEN)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()       // fill horizontal, fillMaxSize doesn't work..
                        .aspectRatio(1f)      // keep it square
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = { showBoard = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Game") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Game Name") }
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Game Time") }
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showBoard = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enter Moves")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showMap = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (lat != null && lng != null) "Change Location" else "Set Location")
                }
                lat?.let { la ->
                    lng?.let { lo ->
                        Text("Location: ($la, $lo)")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && date.isNotBlank()) {
                    onGameAdded(ChessGame(0, name, date, "", lat, lng))
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun GameDetailsPanel(game: ChessGame, onClose: () -> Unit, onDelete: () -> Unit) {
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
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDelete) {
                    Text("Delete Game")
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

@Composable
fun InteractiveChessBoard(
    fen: String = STANDARD_START_FEN,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            ChessBoardView(ctx).apply {

                setFen(fen)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}