package com.example.chesspad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameNote::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameNoteDao(): GameNoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chesspad_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
