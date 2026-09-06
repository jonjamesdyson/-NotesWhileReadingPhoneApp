package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM log_entries WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getEntriesForBook(bookId: Long): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesOnce(): List<LogEntry>

    @Query("SELECT COUNT(*) FROM log_entries")
    fun getTotalEntriesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LogEntry): Long

    @Update
    suspend fun updateEntry(entry: LogEntry)

    @Delete
    suspend fun deleteEntry(entry: LogEntry)
}
