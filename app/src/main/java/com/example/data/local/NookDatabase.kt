package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Book
import com.example.data.model.LogEntry

@Database(entities = [Book::class, LogEntry::class], version = 1, exportSchema = false)
abstract class NookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile
        private var INSTANCE: NookDatabase? = null

        fun getDatabase(context: Context): NookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NookDatabase::class.java,
                    "nook_reading_log.db"
                ).fallbackToDestructiveMigration(false)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
