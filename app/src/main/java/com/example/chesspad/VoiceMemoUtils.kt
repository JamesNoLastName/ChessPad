package com.example.chesspad

import android.content.Context
import java.io.File

/**
 * Utility helpers for handling voice memo file locations.
 */
fun getVoiceMemoFilePath(context: Context, gameUrl: String): String {
    val dir = File(context.filesDir, "voice_memos")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    // Sanitize URL to a valid filename
    val sanitized = gameUrl.replace("[^A-Za-z0-9]".toRegex(), "_")
    return File(dir, "$sanitized.3gp").absolutePath
}
