package com.example.chesspad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameNotesScreen(
    games: List<ChessComGame>,
    onDone: () -> Unit
) {
    var notes by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var editingNoteForUrl by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Attach Notes to Your Games", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
        Spacer(modifier = Modifier.height(16.dp))

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
