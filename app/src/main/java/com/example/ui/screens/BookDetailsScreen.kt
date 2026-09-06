package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Book
import com.example.data.model.LogEntry
import com.example.ui.components.MarkdownContent
import com.example.ui.components.MarkdownToolbar
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedGoldContainer
import com.example.ui.theme.SophisticatedOnGoldContainer
import com.example.ui.theme.SophisticatedOnSageContainer
import com.example.ui.theme.SophisticatedSage
import com.example.ui.theme.SophisticatedSageContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.viewmodel.NookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: Long,
    viewModel: NookViewModel,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.getBook(bookId).collectAsStateWithLifecycle(initialValue = null)
    val entries by viewModel.getLogEntries(bookId).collectAsStateWithLifecycle(initialValue = emptyList())

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var newEntryText by remember { mutableStateOf("") }
    var selectedEntryType by remember { mutableStateOf(LogEntry.TYPE_NOTE) }
    var filterType by remember { mutableStateOf("All") } // "All", "Note", "Review"
    var showBookMenu by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEntryBoxVisible by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<LogEntry?>(null) }

    if (book == null) {
        Scaffold(
            containerColor = SophisticatedBackground,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SophisticatedBackground,
                        titleContentColor = SophisticatedTextPrimary
                    ),
                    title = { Text("Book Details", color = SophisticatedTextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SophisticatedGold
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Book not found", style = MaterialTheme.typography.bodyLarge, color = SophisticatedTextMuted)
            }
        }
        return
    }

    val currentBook = book!!

    if (showEditBookDialog) {
        EditBookPropertiesDialog(
            book = currentBook,
            onDismiss = { showEditBookDialog = false },
            onSave = { title, author, totalPages, pagesRead, coverUrl ->
                viewModel.updateBookProperties(
                    book = currentBook,
                    newTitle = title,
                    newAuthor = author,
                    newTotalPages = totalPages,
                    newPagesRead = pagesRead,
                    newCoverUrl = coverUrl
                )
                showEditBookDialog = false
            }
        )
    }

    if (entryToEdit != null) {
        EditLogEntryDialog(
            entry = entryToEdit!!,
            onDismiss = { entryToEdit = null },
            onSave = { newContent, newType ->
                viewModel.updateLogEntry(entryToEdit!!, newContent, newType)
                entryToEdit = null
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SophisticatedSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Delete Book?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedTextPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${currentBook.title}\" from your shelf? This will permanently delete this book along with all associated notes and reviews. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SophisticatedTextMuted,
                        lineHeight = 20.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteBook(currentBook) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E3C3C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_confirm_delete_book")
                ) {
                    Text("Delete Book", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = SophisticatedTextMuted)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SophisticatedBackground,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.ime),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SophisticatedBackground,
                    titleContentColor = SophisticatedTextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Shelf",
                            tint = SophisticatedGold
                        )
                    }
                },
                title = {
                    Text(
                        text = currentBook.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = SophisticatedTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showBookMenu = true },
                            modifier = Modifier.testTag("btn_book_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Book options",
                                tint = SophisticatedGold
                            )
                        }

                        DropdownMenu(
                            expanded = showBookMenu,
                            onDismissRequest = { showBookMenu = false },
                            containerColor = SophisticatedSurface,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy", color = SophisticatedTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        tint = SophisticatedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showBookMenu = false
                                    val exportText = buildBookExportString(currentBook, entries, forAi = false)
                                    clipboardManager.setText(AnnotatedString(exportText))
                                    Toast.makeText(context, "Copied all book data, notes, and reviews to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("menu_item_copy_book")
                            )

                            DropdownMenuItem(
                                text = { Text("Copy for AI", color = SophisticatedTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = SophisticatedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showBookMenu = false
                                    val exportText = buildBookExportString(currentBook, entries, forAi = true)
                                    clipboardManager.setText(AnnotatedString(exportText))
                                    Toast.makeText(context, "Copied book data & AI review prompt to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("menu_item_copy_for_ai_book")
                            )

                            DropdownMenuItem(
                                text = { Text("Edit", color = SophisticatedTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = SophisticatedGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showBookMenu = false
                                    showEditBookDialog = true
                                },
                                modifier = Modifier.testTag("menu_item_edit_book")
                            )

                            DropdownMenuItem(
                                text = { Text("Delete", color = Color(0xFFE57373)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFE57373),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showBookMenu = false
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.testTag("menu_item_delete_book")
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isEntryBoxVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { isEntryBoxVisible = true },
                    containerColor = SophisticatedGold,
                    contentColor = SophisticatedBackground,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("fab_add_note_or_review")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note or Review",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Note / Review",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isEntryBoxVisible,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
                    color = SophisticatedSurface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = selectedEntryType == LogEntry.TYPE_NOTE,
                                    onClick = { selectedEntryType = LogEntry.TYPE_NOTE },
                                    label = {
                                        Text(
                                            "Note",
                                            fontSize = 12.sp,
                                            color = if (selectedEntryType == LogEntry.TYPE_NOTE) SophisticatedGold else SophisticatedTextMuted
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SophisticatedSurfaceVariant,
                                        selectedContainerColor = SophisticatedGoldContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedEntryType == LogEntry.TYPE_NOTE,
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = SophisticatedGold.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                FilterChip(
                                    selected = selectedEntryType == LogEntry.TYPE_REVIEW,
                                    onClick = { selectedEntryType = LogEntry.TYPE_REVIEW },
                                    label = {
                                        Text(
                                            "Review",
                                            fontSize = 12.sp,
                                            color = if (selectedEntryType == LogEntry.TYPE_REVIEW) SophisticatedSage else SophisticatedTextMuted
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SophisticatedSurfaceVariant,
                                        selectedContainerColor = SophisticatedSageContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedEntryType == LogEntry.TYPE_REVIEW,
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = SophisticatedSage.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (selectedEntryType == LogEntry.TYPE_NOTE) "Cozy observation" else "Thoughts & critique",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SophisticatedTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                                IconButton(
                                    onClick = { isEntryBoxVisible = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close input box",
                                        tint = SophisticatedTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        MarkdownToolbar(
                            currentText = newEntryText,
                            onTextChange = { newEntryText = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = newEntryText,
                                onValueChange = { newEntryText = it },
                                placeholder = {
                                    Text(
                                        text = if (selectedEntryType == LogEntry.TYPE_NOTE) "Write note (Markdown supported)..." else "Write your review (Markdown supported)...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            color = SophisticatedTextMuted
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_log_entry"),
                                maxLines = 4,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SophisticatedGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                    focusedContainerColor = SophisticatedBackground,
                                    unfocusedContainerColor = SophisticatedBackground,
                                    focusedTextColor = SophisticatedTextPrimary,
                                    unfocusedTextColor = SophisticatedTextPrimary
                                )
                            )

                            IconButton(
                                onClick = {
                                    if (newEntryText.isNotBlank()) {
                                        viewModel.addLogEntry(currentBook.id, newEntryText.trim(), selectedEntryType)
                                        newEntryText = ""
                                        isEntryBoxVisible = false
                                    }
                                },
                                enabled = newEntryText.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (newEntryText.isNotBlank()) SophisticatedGold else SophisticatedSurfaceVariant
                                    )
                                    .testTag("btn_send_log_entry")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Add Entry",
                                    tint = if (newEntryText.isNotBlank()) SophisticatedBackground else SophisticatedTextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = if (!isEntryBoxVisible) 88.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Book Hero Header Card
            item {
                BookHeroCard(book = currentBook)
            }

            // Progress & Status Tracker Card
            item {
                ReadingTrackerCard(
                    book = currentBook,
                    onPagesChanged = { newPages ->
                        viewModel.updatePagesRead(currentBook, newPages)
                    },
                    onStatusChanged = { newStatus ->
                        viewModel.updateBookStatus(currentBook, newStatus)
                    }
                )
            }

            // Notes & Reviews Feed Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Notes & Reviews",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Serif,
                                    color = SophisticatedTextPrimary
                                )
                            )
                            Text(
                                text = "${entries.size} entries",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SophisticatedTextMuted
                                )
                            )
                        }
                    }

                    // Filter chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", LogEntry.TYPE_NOTE, LogEntry.TYPE_REVIEW).forEach { filter ->
                            val isSelected = filterType == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterType = filter },
                                label = {
                                    Text(
                                        text = if (filter == "All") "All Entries" else "${filter}s",
                                        fontSize = 11.sp,
                                        color = if (isSelected) SophisticatedBackground else SophisticatedTextMuted
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SophisticatedSurface,
                                    selectedContainerColor = SophisticatedGold
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.05f),
                                    selectedBorderColor = SophisticatedGold
                                )
                            )
                        }
                    }
                }
            }

            // Filtered entries
            val filteredEntries = entries.filter {
                when (filterType) {
                    LogEntry.TYPE_NOTE -> it.type == LogEntry.TYPE_NOTE
                    LogEntry.TYPE_REVIEW -> it.type == LogEntry.TYPE_REVIEW
                    else -> true
                }
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RateReview,
                                contentDescription = null,
                                tint = SophisticatedGold,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = if (filterType == "All") "No log entries yet" else "No ${filterType.lowercase()}s yet",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedTextPrimary
                                )
                            )
                            Text(
                                text = "Tap the button below or at the bottom corner to record memorable quotes, chapter notes, or your overall critique.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SophisticatedTextMuted,
                                    fontSize = 12.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (!isEntryBoxVisible) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { isEntryBoxVisible = true },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SophisticatedGold,
                                        contentColor = SophisticatedBackground
                                    ),
                                    modifier = Modifier.testTag("btn_empty_add_note_review")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add Note or Review",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { entry ->
                    LogEntryCard(
                        entry = entry,
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(entry.content))
                            Toast.makeText(
                                context,
                                if (entry.type == LogEntry.TYPE_REVIEW) "Copied review to clipboard" else "Copied note to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onEditClick = {
                            entryToEdit = entry
                        },
                        onDeleteClick = {
                            viewModel.deleteLogEntry(entry)
                            Toast.makeText(
                                context,
                                if (entry.type == LogEntry.TYPE_REVIEW) "Deleted review" else "Deleted note",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookHeroCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(124.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SophisticatedSurfaceVariant)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover of ${book.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = SophisticatedTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(124.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = SophisticatedTextPrimary,
                            fontSize = 19.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SophisticatedTextMuted,
                            fontSize = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SophisticatedSurfaceVariant
                    ) {
                        Text(
                            text = "${book.totalPages} pages total",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SophisticatedTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingTrackerCard(
    book: Book,
    onPagesChanged: (Int) -> Unit,
    onStatusChanged: (String) -> Unit
) {
    var sliderValue by remember(book.pagesRead) {
        mutableFloatStateOf(book.pagesRead.toFloat())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Switcher / Segmented control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = SophisticatedTextMuted,
                        fontSize = 10.sp
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isReading = book.status == Book.STATUS_CURRENTLY_READING
                    val isFinished = book.status == Book.STATUS_FINISHED

                    FilterChip(
                        selected = isReading,
                        onClick = { onStatusChanged(Book.STATUS_CURRENTLY_READING) },
                        label = {
                            Text(
                                "Reading",
                                fontSize = 11.sp,
                                color = if (isReading) SophisticatedOnGoldContainer else SophisticatedTextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isReading) SophisticatedGold else SophisticatedTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SophisticatedSurfaceVariant,
                            selectedContainerColor = SophisticatedGoldContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isReading,
                            borderColor = Color.Transparent,
                            selectedBorderColor = SophisticatedGold.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FilterChip(
                        selected = isFinished,
                        onClick = { onStatusChanged(Book.STATUS_FINISHED) },
                        label = {
                            Text(
                                "Finished",
                                fontSize = 11.sp,
                                color = if (isFinished) SophisticatedOnSageContainer else SophisticatedTextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isFinished) SophisticatedSage else SophisticatedTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SophisticatedSurfaceVariant,
                            selectedContainerColor = SophisticatedSageContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isFinished,
                            borderColor = Color.Transparent,
                            selectedBorderColor = SophisticatedSage.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "PAGES READ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = SophisticatedTextMuted
                        )
                    )
                    Text(
                        text = "${sliderValue.toInt()} / ${book.totalPages}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Serif,
                            color = SophisticatedGold
                        )
                    )
                }
                Text(
                    text = "${((sliderValue / book.totalPages.coerceAtLeast(1).toFloat()) * 100).toInt()}% completed",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = if (book.isFinished) SophisticatedSage else SophisticatedGold
                    )
                )
            }

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    onPagesChanged(sliderValue.toInt())
                },
                valueRange = 0f..book.totalPages.coerceAtLeast(1).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pages_read_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = SophisticatedGold,
                    activeTrackColor = SophisticatedGold,
                    inactiveTrackColor = SophisticatedBackground
                )
            )

            // Quick adjustment buttons (-10, -1, +1, +10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-10, -1, 1, 10).forEach { delta ->
                    Surface(
                        onClick = {
                            val target = (book.pagesRead + delta).coerceIn(0, book.totalPages)
                            sliderValue = target.toFloat()
                            onPagesChanged(target)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = SophisticatedSurfaceVariant,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (delta > 0) "+$delta" else "$delta",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    entry: LogEntry,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val isReview = entry.type == LogEntry.TYPE_REVIEW
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isReview) SophisticatedSageContainer else SophisticatedGoldContainer
                    ) {
                        Text(
                            text = entry.type.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = if (isReview) SophisticatedSage else SophisticatedGold
                            )
                        )
                    }

                    Text(
                        text = dateFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SophisticatedTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_entry_menu_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Entry options",
                            tint = SophisticatedTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = SophisticatedSurface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy", color = SophisticatedTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    tint = SophisticatedGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onCopyClick()
                            },
                            modifier = Modifier.testTag("menu_item_copy_entry_${entry.id}")
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", color = SophisticatedTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = SophisticatedGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            modifier = Modifier.testTag("menu_item_edit_entry_${entry.id}")
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFE57373)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            modifier = Modifier.testTag("menu_item_delete_entry_${entry.id}")
                        )
                    }
                }
            }

            MarkdownContent(
                markdown = entry.content,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun buildBookExportString(
    book: Book,
    entries: List<LogEntry>,
    forAi: Boolean
): String {
    val sb = StringBuilder()
    if (forAi) {
        sb.appendLine("You are a professional literary critic and reviewer. Your task is to write a comprehensive, well-structured book review based entirely on the reading notes provided below.")
        sb.appendLine()
        sb.appendLine("Follow these strict constraints when drafting the review:")
        sb.appendLine()
        sb.appendLine("Incorporate Personal Opinions: Base the tone, critique, and overall verdict directly on the personal opinions, reactions, and observations recorded throughout the notes.")
        sb.appendLine()
        sb.appendLine("Integrate Relevant Quotes: Pull directly from any exact quotes highlighted or saved within the notes to support the analysis.")
        sb.appendLine()
        sb.appendLine("Formatting Requirements: Output the entire review inside a single Markdown code block (```markdown ... ```). Use rich Markdown elements throughout (such as bolding, italics, headers, and lists).")
        sb.appendLine()
        sb.appendLine("Punctuation Rules: Do NOT use em dashes (—) anywhere in the review. Use commas, parentheses, or colons for stylistic pauses instead.")
        sb.appendLine()
        sb.appendLine("Fidelity to Input: Ensure every major theme, character commentary, and plot reaction mentioned in the notes is accounted for without adding fabricated plot details not supported by the text.")
        sb.appendLine()
        sb.appendLine("READING NOTES & HIGHLIGHTS:")
        sb.appendLine()
    }
    sb.appendLine("Title: ${book.title}")
    sb.appendLine("Author: ${book.author}")
    sb.appendLine("Status: ${if (book.isFinished) "Finished" else "Currently Reading"}")
    sb.appendLine("Progress: ${book.pagesRead} / ${book.totalPages} pages (${((book.pagesRead.toFloat() / book.totalPages.coerceAtLeast(1).toFloat()) * 100).toInt()}%)")
    if (entries.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("--- Notes & Reviews (${entries.size}) ---")
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        entries.forEach { e ->
            sb.appendLine()
            sb.appendLine("[${e.type.uppercase()} • ${dateFormat.format(Date(e.timestamp))}]")
            sb.appendLine(e.content)
        }
    }
    return sb.toString().trimEnd()
}

