package com.example.chesspad.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class ChessGame(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val moves: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
