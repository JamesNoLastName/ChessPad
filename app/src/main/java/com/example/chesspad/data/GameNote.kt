package com.example.chesspad.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_notes")
data class GameNote(
    @PrimaryKey val gameUrl: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)
