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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun GameNotesScreen(
    games: List<ChessComGame>,
    onDone: () -> Unit,
    navController: NavHostController // Required for navigation
) {
    var notes by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var editingNoteForUrl by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var isSearchSelected by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                "Attach Notes to Your Games",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(games) { game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${game.white} vs ${game.black}")
                            Text("Result: ${game.whiteResult} - ${game.blackResult}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = {
                                editingNoteForUrl = game.url
                                noteText = notes[game.url] ?: ""
                            }) {
                                Text(if (notes[game.url].isNullOrBlank()) "Add Note" else "Edit Note")
                            }
                            if (!notes[game.url].isNullOrBlank()) {
                                Text("Note: ${notes[game.url]}", modifier = Modifier.padding(top = 4.dp))
                            }
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
