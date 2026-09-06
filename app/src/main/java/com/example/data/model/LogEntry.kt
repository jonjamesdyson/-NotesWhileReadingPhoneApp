package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val content: String,
    val type: String = TYPE_NOTE,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_NOTE = "Note"
        const val TYPE_REVIEW = "Review"
    }
}
