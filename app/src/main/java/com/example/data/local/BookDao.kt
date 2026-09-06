package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY id DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdOnce(id: Long): Book?

    @Query("SELECT * FROM books ORDER BY id DESC")
    suspend fun getAllBooksOnce(): List<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Query("UPDATE books SET pagesRead = :pagesRead, status = :status WHERE id = :bookId")
    suspend fun updateProgress(bookId: Long, pagesRead: Int, status: String)

    @Delete
    suspend fun deleteBook(book: Book)
}
