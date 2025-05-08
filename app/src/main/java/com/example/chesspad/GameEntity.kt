package com.example.chesspad

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "game_table")
data class GameEntity(
    @PrimaryKey
    val url: String,
    val white: String,
    val whiteResult: String,
    val black: String,
    val blackResult: String,
    val endTime: Long,
    val pgn: String,
    val opening: String?,
    val note: String? = null,
    val voiceMemoPath: String? = null
)

// Extension functions to convert between ChessComGame and GameEntity
fun ChessComGame.toGameEntity(note: String? = null, voiceMemoPath: String? = null): GameEntity {
    return GameEntity(
        url = this.url,
        white = this.white,
        whiteResult = this.whiteResult,
        black = this.black,
        blackResult = this.blackResult,
        endTime = this.endTime,
        pgn = this.pgn,
        opening = this.opening,
        note = note,
        voiceMemoPath = voiceMemoPath
    )
}

fun GameEntity.toChessComGame(): ChessComGame {
    return ChessComGame(
        url = this.url,
        white = this.white,
        whiteResult = this.whiteResult,
        black = this.black,
        blackResult = this.blackResult,
        endTime = this.endTime,
        pgn = this.pgn,
        opening = this.opening
    )
}