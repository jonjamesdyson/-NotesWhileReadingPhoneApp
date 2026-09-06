package com.example.data.repository

import com.example.data.local.BookDao
import com.example.data.local.LogEntryDao
import com.example.data.model.Book
import com.example.data.model.LogEntry
import com.example.data.remote.OpenLibraryApi
import com.example.data.remote.OpenLibraryDoc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NookRepository(
    private val bookDao: BookDao,
    private val logEntryDao: LogEntryDao,
    private val openLibraryApi: OpenLibraryApi
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    val totalEntriesCount: Flow<Int> = logEntryDao.getTotalEntriesCount()

    fun getBook(id: Long): Flow<Book?> = bookDao.getBookById(id)

    fun getLogEntries(bookId: Long): Flow<List<LogEntry>> = logEntryDao.getEntriesForBook(bookId)

    suspend fun getBookOnce(id: Long): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByIdOnce(id)
    }

    suspend fun getAllBooksOnce(): List<Book> = withContext(Dispatchers.IO) {
        bookDao.getAllBooksOnce()
    }

    suspend fun getAllEntriesOnce(): List<LogEntry> = withContext(Dispatchers.IO) {
        logEntryDao.getAllEntriesOnce()
    }

    suspend fun insertBook(book: Book): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    suspend fun updateProgress(bookId: Long, pagesRead: Int, status: String) = withContext(Dispatchers.IO) {
        bookDao.updateProgress(bookId, pagesRead, status)
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
    }

    suspend fun insertLogEntry(entry: LogEntry): Long = withContext(Dispatchers.IO) {
        logEntryDao.insertEntry(entry)
    }

    suspend fun updateLogEntry(entry: LogEntry) = withContext(Dispatchers.IO) {
        logEntryDao.updateEntry(entry)
    }

    suspend fun deleteLogEntry(entry: LogEntry) = withContext(Dispatchers.IO) {
        logEntryDao.deleteEntry(entry)
    }

    suspend fun importBackup(parsedBooks: List<com.example.ui.export.ParsedBackupBook>): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var booksImported = 0
        var entriesImported = 0

        val existingBooks = bookDao.getAllBooksOnce()

        for (item in parsedBooks) {
            val incomingBook = item.book
            val matchedBook = existingBooks.find { existing ->
                (existing.openLibraryId.isNotBlank() && existing.openLibraryId == incomingBook.openLibraryId) ||
                (existing.title.equals(incomingBook.title, ignoreCase = true) && existing.author.equals(incomingBook.author, ignoreCase = true))
            }

            val targetBookId = if (matchedBook != null) {
                val updatedBook = matchedBook.copy(
                    pagesRead = maxOf(matchedBook.pagesRead, incomingBook.pagesRead),
                    status = if (incomingBook.status == Book.STATUS_FINISHED) Book.STATUS_FINISHED else matchedBook.status
                )
                bookDao.updateBook(updatedBook)
                matchedBook.id
            } else {
                val newId = bookDao.insertBook(incomingBook.copy(id = 0))
                booksImported++
                newId
            }

            for (entry in item.entries) {
                logEntryDao.insertEntry(
                    entry.copy(
                        id = 0,
                        bookId = targetBookId
                    )
                )
                entriesImported++
            }
        }

        Pair(booksImported, entriesImported)
    }

    suspend fun searchBooks(query: String): List<OpenLibraryDoc> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()
        val response = openLibraryApi.searchBooks(trimmed)
        response.docs ?: emptyList()
    }
}
