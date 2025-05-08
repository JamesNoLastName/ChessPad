package com.example.chesspad

import android.app.Application

class ChessPadApplication : Application() {
    // The database instance will be initialized on demand by GameDatabase.getDatabase()

    override fun onCreate() {
        super.onCreate()
        // Any application-wide initialization can go here
    }
}