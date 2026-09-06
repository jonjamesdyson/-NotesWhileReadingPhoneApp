package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val openLibraryId: String,
    val title: String,
    val author: String,
    val totalPages: Int,
    val pagesRead: Int = 0,
    val status: String = STATUS_CURRENTLY_READING,
    val coverImageUrl: String = ""
) {
    val progress: Float
        get() = if (totalPages > 0) (pagesRead.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()

    val isFinished: Boolean
        get() = status == STATUS_FINISHED || (totalPages > 0 && pagesRead >= totalPages)

    companion object {
        const val STATUS_CURRENTLY_READING = "Currently Reading"
        const val STATUS_FINISHED = "Finished"
    }
}
