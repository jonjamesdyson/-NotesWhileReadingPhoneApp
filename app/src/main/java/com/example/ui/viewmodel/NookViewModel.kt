package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Book
import com.example.data.model.LogEntry
import com.example.data.model.ReadingStats
import com.example.data.remote.OpenLibraryDoc
import com.example.data.repository.NookRepository
import com.example.ui.export.ExportHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ShelfSortOption(val displayName: String) {
    ALL("All"),
    NAME("Name (A-Z)"),
    READING("Reading"),
    FINISHED("Finished")
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<OpenLibraryDoc>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class NookViewModel(
    private val repository: NookRepository
) : ViewModel() {

    val shelfBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _shelfSearchQuery = MutableStateFlow("")
    val shelfSearchQuery: StateFlow<String> = _shelfSearchQuery.asStateFlow()

    private val _shelfSortOption = MutableStateFlow(ShelfSortOption.ALL)
    val shelfSortOption: StateFlow<ShelfSortOption> = _shelfSortOption.asStateFlow()

    val filteredShelfBooks: StateFlow<List<Book>> = combine(
        repository.allBooks,
        _shelfSearchQuery,
        _shelfSortOption
    ) { books, query, sort ->
        var list = if (query.isBlank()) {
            books
        } else {
            val q = query.trim().lowercase()
            books.filter {
                it.title.lowercase().contains(q) || it.author.lowercase().contains(q)
            }
        }

        when (sort) {
            ShelfSortOption.ALL -> list
            ShelfSortOption.NAME -> list.sortedBy { it.title.lowercase() }
            ShelfSortOption.READING -> list.filter { it.status == Book.STATUS_CURRENTLY_READING }
            ShelfSortOption.FINISHED -> list.filter { it.isFinished }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<ReadingStats> = combine(
        repository.allBooks,
        repository.totalEntriesCount
    ) { books, entriesCount ->
        ReadingStats(
            currentlyReadingCount = books.count { it.status == Book.STATUS_CURRENTLY_READING },
            booksFinishedCount = books.count { it.isFinished },
            notesWrittenCount = entriesCount,
            pagesReadSum = books.sumOf { it.pagesRead }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReadingStats()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        seedSampleDataIfEmpty()
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            val existing = repository.getAllBooksOnce()
            if (existing.isEmpty()) {
                val book1Id = repository.insertBook(
                    Book(
                        openLibraryId = "/works/OL262758W",
                        title = "The Hobbit",
                        author = "J.R.R. Tolkien",
                        totalPages = 310,
                        pagesRead = 184,
                        status = Book.STATUS_CURRENTLY_READING,
                        coverImageUrl = "https://covers.openlibrary.org/b/id/12003527-M.jpg"
                    )
                )
                repository.insertLogEntry(
                    LogEntry(
                        bookId = book1Id,
                        content = "The description of Bag End is the epitome of coziness. Tea, seed cake, and warm fireplaces.",
                        type = LogEntry.TYPE_NOTE,
                        timestamp = System.currentTimeMillis() - 86400000L * 2
                    )
                )
                repository.insertLogEntry(
                    LogEntry(
                        bookId = book1Id,
                        content = "Riddles in the dark was thrilling. Gollum's dialogue was so cleverly written.",
                        type = LogEntry.TYPE_REVIEW,
                        timestamp = System.currentTimeMillis() - 3600000L * 4
                    )
                )

                val book2Id = repository.insertBook(
                    Book(
                        openLibraryId = "/works/OL102749W",
                        title = "Pride and Prejudice",
                        author = "Jane Austen",
                        totalPages = 432,
                        pagesRead = 432,
                        status = Book.STATUS_FINISHED,
                        coverImageUrl = "https://covers.openlibrary.org/b/id/8231856-M.jpg"
                    )
                )
                repository.insertLogEntry(
                    LogEntry(
                        bookId = book2Id,
                        content = "Elizabeth Bennet remains one of the sharpest protagonists in classic literature.",
                        type = LogEntry.TYPE_REVIEW,
                        timestamp = System.currentTimeMillis() - 86400000L * 5
                    )
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState.Idle
        }
    }

    fun executeSearch() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val results = repository.searchBooks(query)
                if (results.isEmpty()) {
                    _searchUiState.value = SearchUiState.Success(emptyList())
                } else {
                    _searchUiState.value = SearchUiState.Success(results)
                }
            } catch (e: Exception) {
                _searchUiState.value = SearchUiState.Error(
                    e.localizedMessage ?: "Could not connect to Open Library. Please check your connection."
                )
            }
        }
    }

    fun addBookFromSearch(doc: OpenLibraryDoc, onAdded: () -> Unit) {
        viewModelScope.launch {
            try {
                val newBook = Book(
                    openLibraryId = doc.key ?: "",
                    title = doc.title ?: "Untitled",
                    author = doc.displayAuthor,
                    totalPages = doc.displayPages,
                    pagesRead = 0,
                    status = Book.STATUS_CURRENTLY_READING,
                    coverImageUrl = doc.coverUrl
                )
                repository.insertBook(newBook)
                _userMessage.emit("Added \"${newBook.title}\" to your shelf")
                onAdded()
            } catch (e: Exception) {
                _userMessage.emit("Failed to add book: ${e.message}")
            }
        }
    }

    fun getBook(bookId: Long) = repository.getBook(bookId)

    fun getLogEntries(bookId: Long) = repository.getLogEntries(bookId)

    fun updatePagesRead(book: Book, newPages: Int) {
        viewModelScope.launch {
            val clamped = newPages.coerceIn(0, book.totalPages.coerceAtLeast(1))
            val newStatus = if (clamped >= book.totalPages && book.totalPages > 0) {
                Book.STATUS_FINISHED
            } else if (book.status == Book.STATUS_FINISHED && clamped < book.totalPages) {
                Book.STATUS_CURRENTLY_READING
            } else {
                book.status
            }
            repository.updateProgress(book.id, clamped, newStatus)
        }
    }

    fun updateBookStatus(book: Book, newStatus: String) {
        viewModelScope.launch {
            val updatedPages = if (newStatus == Book.STATUS_FINISHED && book.pagesRead < book.totalPages) {
                book.totalPages
            } else if (newStatus == Book.STATUS_CURRENTLY_READING && book.pagesRead >= book.totalPages) {
                (book.totalPages - 1).coerceAtLeast(0)
            } else {
                book.pagesRead
            }
            repository.updateProgress(book.id, updatedPages, newStatus)
        }
    }

    fun addLogEntry(bookId: Long, content: String, type: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.insertLogEntry(
                LogEntry(
                    bookId = bookId,
                    content = trimmed,
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateLogEntry(entry: LogEntry, newContent: String, newType: String = entry.type) {
        val trimmed = newContent.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.updateLogEntry(entry.copy(content = trimmed, type = newType))
            _userMessage.emit("Updated ${if (newType == LogEntry.TYPE_REVIEW) "review" else "note"}")
        }
    }

    fun deleteLogEntry(entry: LogEntry) {
        viewModelScope.launch {
            repository.deleteLogEntry(entry)
        }
    }

    fun updateBookProperties(
        book: Book,
        newTitle: String,
        newAuthor: String,
        newTotalPages: Int,
        newPagesRead: Int,
        newCoverUrl: String
    ) {
        viewModelScope.launch {
            val total = newTotalPages.coerceAtLeast(1)
            val read = newPagesRead.coerceIn(0, total)
            val newStatus = if (read >= total && total > 0) {
                Book.STATUS_FINISHED
            } else if (book.status == Book.STATUS_FINISHED && read < total) {
                Book.STATUS_CURRENTLY_READING
            } else {
                book.status
            }
            val updated = book.copy(
                title = newTitle.trim().ifBlank { book.title },
                author = newAuthor.trim().ifBlank { book.author },
                totalPages = total,
                pagesRead = read,
                status = newStatus,
                coverImageUrl = newCoverUrl
            )
            repository.updateBook(updated)
            _userMessage.emit("Updated \"${updated.title}\"")
        }
    }

    fun deleteBook(book: Book, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteBook(book)
            _userMessage.emit("Removed \"${book.title}\" from your shelf")
            onComplete()
        }
    }

    fun setShelfSearchQuery(query: String) {
        _shelfSearchQuery.value = query
    }

    fun setShelfSortOption(option: ShelfSortOption) {
        _shelfSortOption.value = option
    }

    fun addManualBook(
        title: String,
        author: String,
        totalPages: Int,
        pagesRead: Int,
        status: String,
        coverImageUrl: String = "",
        onAdded: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val newBook = Book(
                    openLibraryId = "custom_${System.currentTimeMillis()}",
                    title = title,
                    author = author,
                    totalPages = totalPages,
                    pagesRead = pagesRead,
                    status = status,
                    coverImageUrl = coverImageUrl
                )
                repository.insertBook(newBook)
                _userMessage.emit("Added \"${newBook.title}\" to your shelf")
                onAdded?.invoke()
            } catch (e: Exception) {
                _userMessage.emit("Failed to add book: ${e.message}")
            }
        }
    }

    fun prepareBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val books = repository.getAllBooksOnce()
            val entries = repository.getAllEntriesOnce()
            val json = ExportHelper.generateBackupJson(books, entries)
            onReady(json)
        }
    }

    fun importBackupJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val parsed = ExportHelper.parseBackupJson(jsonString)
                if (parsed.isEmpty()) {
                    _userMessage.emit("No valid books or notes found in the backup.")
                    return@launch
                }
                val (booksCount, entriesCount) = repository.importBackup(parsed)
                _userMessage.emit("Imported $booksCount books and $entriesCount notes/reviews successfully!")
            } catch (e: Exception) {
                _userMessage.emit("Import failed: ${e.localizedMessage ?: "Invalid JSON format"}")
            }
        }
    }

    fun postUserMessage(message: String) {
        viewModelScope.launch {
            _userMessage.emit(message)
        }
    }

    fun prepareExportData(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val books = repository.getAllBooksOnce()
            val entries = repository.getAllEntriesOnce()
            val formatted = ExportHelper.formatReadingLog(books, entries)
            onReady(formatted)
        }
    }
}

class NookViewModelFactory(
    private val repository: NookRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NookViewModel::class.java)) {
            return NookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
