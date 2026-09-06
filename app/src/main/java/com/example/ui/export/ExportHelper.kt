package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Book
import com.example.data.model.LogEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedBackupBook(
    val book: Book,
    val entries: List<LogEntry>
)

data class ImportResult(
    val success: Boolean,
    val booksCount: Int = 0,
    val entriesCount: Int = 0,
    val errorMessage: String? = null
)

object ExportHelper {

    /**
     * Generates a complete JSON backup containing all books, notes, and reviews,
     * fully preserving all markdown formatting notations.
     */
    fun generateBackupJson(books: List<Book>, entries: List<LogEntry>): String {
        val root = JSONObject()
        root.put("app", "Nook")
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("booksCount", books.size)
        root.put("entriesCount", entries.size)

        val entriesByBook = entries.groupBy { it.bookId }

        val booksArray = JSONArray()
        books.forEach { book ->
            val bookObj = JSONObject()
            bookObj.put("id", book.id)
            bookObj.put("openLibraryId", book.openLibraryId)
            bookObj.put("title", book.title)
            bookObj.put("author", book.author)
            bookObj.put("totalPages", book.totalPages)
            bookObj.put("pagesRead", book.pagesRead)
            bookObj.put("status", book.status)
            bookObj.put("coverImageUrl", book.coverImageUrl)

            val bookEntries = entriesByBook[book.id] ?: emptyList()
            val entriesArray = JSONArray()
            bookEntries.forEach { entry ->
                val entryObj = JSONObject()
                entryObj.put("id", entry.id)
                entryObj.put("bookId", entry.bookId)
                // Raw markdown notation preserved
                entryObj.put("content", entry.content)
                entryObj.put("type", entry.type)
                entryObj.put("timestamp", entry.timestamp)
                entriesArray.put(entryObj)
            }
            bookObj.put("entries", entriesArray)
            booksArray.put(bookObj)
        }

        root.put("books", booksArray)
        return root.toString(2)
    }

    /**
     * Parses a JSON backup string into books and their associated log entries (notes & reviews).
     */
    fun parseBackupJson(jsonString: String): List<ParsedBackupBook> {
        val root = JSONObject(jsonString)
        val booksArray = if (root.has("books")) {
            root.getJSONArray("books")
        } else {
            // Check if top level is array
            JSONArray(jsonString)
        }

        val result = mutableListOf<ParsedBackupBook>()

        for (i in 0 until booksArray.length()) {
            val bObj = booksArray.getJSONObject(i)
            val title = bObj.optString("title", "Untitled Book")
            val author = bObj.optString("author", "Unknown Author")
            val totalPages = bObj.optInt("totalPages", 100)
            val pagesRead = bObj.optInt("pagesRead", 0)
            val status = bObj.optString("status", Book.STATUS_CURRENTLY_READING)
            val coverImageUrl = bObj.optString("coverImageUrl", "")
            val openLibraryId = bObj.optString("openLibraryId", "imported_${System.currentTimeMillis()}_$i")

            val book = Book(
                id = 0, // Auto-generate on insert
                openLibraryId = openLibraryId,
                title = title,
                author = author,
                totalPages = totalPages,
                pagesRead = pagesRead,
                status = status,
                coverImageUrl = coverImageUrl
            )

            val entriesList = mutableListOf<LogEntry>()
            if (bObj.has("entries")) {
                val entriesArray = bObj.getJSONArray("entries")
                for (j in 0 until entriesArray.length()) {
                    val eObj = entriesArray.getJSONObject(j)
                    val content = eObj.optString("content", "")
                    val type = eObj.optString("type", LogEntry.TYPE_NOTE)
                    val timestamp = eObj.optLong("timestamp", System.currentTimeMillis())

                    if (content.isNotBlank()) {
                        entriesList.add(
                            LogEntry(
                                id = 0,
                                bookId = 0, // Assigned after book insertion
                                content = content,
                                type = type,
                                timestamp = timestamp
                            )
                        )
                    }
                }
            }

            result.add(ParsedBackupBook(book, entriesList))
        }

        return result
    }

    /**
     * Writes JSON content to an OutputStream from a Storage Access Framework Uri.
     */
    fun saveJsonToUri(context: Context, uri: Uri, jsonContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(jsonContent.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads JSON content from an InputStream from a Storage Access Framework Uri.
     */
    fun readJsonFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares a .json file via Android Share sheet using FileProvider.
     */
    fun shareJsonFile(context: Context, jsonContent: String, fileName: String = "nook_backup.json") {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { fos ->
                fos.write(jsonContent.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Nook Reading Log Backup (JSON)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Export Nook Backup JSON")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share backup file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatReadingLog(books: List<Book>, entries: List<LogEntry>): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("        NOOK - READING LOG EXPORT       ")
        sb.appendLine("========================================")
        sb.appendLine("Export Date: $exportDate")
        sb.appendLine("Total Books: ${books.size}")
        val finishedCount = books.count { it.isFinished }
        val readingCount = books.count { !it.isFinished }
        val pagesSum = books.sumOf { it.pagesRead }
        sb.appendLine("Finished: $finishedCount | Currently Reading: $readingCount")
        sb.appendLine("Total Pages Read: $pagesSum")
        sb.appendLine("Total Notes & Reviews: ${entries.size}")
        sb.appendLine("========================================\n")

        val entriesByBook = entries.groupBy { it.bookId }

        books.forEachIndexed { index, book ->
            sb.appendLine("----------------------------------------")
            sb.appendLine("[${index + 1}] ${book.title.uppercase()}")
            sb.appendLine("Author: ${book.author}")
            sb.appendLine("Status: ${book.status}")
            sb.appendLine("Progress: ${book.pagesRead} / ${book.totalPages} pages (${book.progressPercent}%)")
            if (book.openLibraryId.isNotBlank()) {
                sb.appendLine("Open Library ID: ${book.openLibraryId}")
            }

            val bookEntries = entriesByBook[book.id] ?: emptyList()
            if (bookEntries.isNotEmpty()) {
                sb.appendLine("\n  Notes & Reviews (${bookEntries.size}):")
                bookEntries.sortedBy { it.timestamp }.forEach { entry ->
                    val dateStr = dateFormat.format(Date(entry.timestamp))
                    sb.appendLine("  • [${entry.type.uppercase()}] ($dateStr):")
                    sb.appendLine("    \"${entry.content}\"")
                }
            } else {
                sb.appendLine("  (No notes or reviews recorded yet)")
            }
            sb.appendLine()
        }

        sb.appendLine("========================================")
        sb.appendLine("Exported from Nook • Cozy Reading Log")
        sb.appendLine("========================================")

        return sb.toString()
    }
}

/**
 * Android Context extension function to launch an email share intent specifically via Email.
 */
fun Context.sendExportEmail(
    subject: String = "Nook Reading Log Export",
    body: String,
    recipientEmail: String? = null
) {
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        if (!recipientEmail.isNullOrBlank()) {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
        }
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        val chooserIntent = Intent.createChooser(emailIntent, "Export Reading Log via Email")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(chooserIntent)
    } catch (e: Exception) {
        try {
            val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(mailtoIntent)
        } catch (fallbackError: Exception) {
            Toast.makeText(this, "No email client found on device", Toast.LENGTH_SHORT).show()
        }
    }
}
