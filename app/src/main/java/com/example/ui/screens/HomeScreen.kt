package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Book
import com.example.data.model.ReadingStats
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedGoldContainer
import com.example.ui.theme.SophisticatedSage
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.viewmodel.NookViewModel
import com.example.ui.viewmodel.ShelfSortOption
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NookViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToBookDetails: (Long) -> Unit
) {
    val allBooks by viewModel.shelfBooks.collectAsStateWithLifecycle()
    val books by viewModel.filteredShelfBooks.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val shelfSearchQuery by viewModel.shelfSearchQuery.collectAsStateWithLifecycle()
    val shelfSortOption by viewModel.shelfSortOption.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showBackupDialog by remember { mutableStateOf(false) }
    var showAddManualDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showBackupDialog) {
        BackupDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showAddManualDialog) {
        AddManualBookDialog(
            onDismiss = { showAddManualDialog = false },
            onAddBook = { title, author, totalPages, pagesRead, status, coverUrl ->
                viewModel.addManualBook(title, author, totalPages, pagesRead, status, coverUrl)
                showAddManualDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SophisticatedBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SophisticatedBackground,
                    titleContentColor = SophisticatedTextPrimary
                ),
                title = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "Nook",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Serif,
                                color = SophisticatedGold,
                                fontSize = 34.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "NOTES MAY CONTAIN SPOILERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = SophisticatedTextMuted,
                                fontSize = 9.sp,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                },
                actions = {
                    // Add Custom Book Manually
                    Surface(
                        onClick = { showAddManualDialog = true },
                        modifier = Modifier
                            .testTag("action_add_manual_book")
                            .size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = SophisticatedSurface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.LibraryAdd,
                                contentDescription = "Add Book Manually",
                                tint = SophisticatedGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Backup & Restore (JSON Export & Import)
                    Surface(
                        onClick = { showBackupDialog = true },
                        modifier = Modifier
                            .testTag("action_export_data")
                            .size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = SophisticatedSurface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Backup,
                                contentDescription = "Backup & Restore",
                                tint = SophisticatedGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSearch,
                containerColor = SophisticatedGold,
                contentColor = SophisticatedBackground,
                shape = RoundedCornerShape(28.dp),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .testTag("fab_search")
                    .padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Search & Add Book",
                        tint = SophisticatedBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Add Book",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sophisticated 2x2 Stats Grid
            item {
                StatsSection(stats = stats)
            }

            // Shelf Header, Search and Sort Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shelf Title and Counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(SophisticatedGold)
                            )
                            Text(
                                text = "THE SHELF",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = SophisticatedTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        val countLabel = if (books.size == allBooks.size) {
                            "${allBooks.size} ${if (allBooks.size == 1) "book" else "books"}"
                        } else {
                            "${books.size} of ${allBooks.size} books"
                        }
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = SophisticatedTextMuted.copy(alpha = 0.8f)
                            )
                        )
                    }

                    // Shelf Search Input
                    OutlinedTextField(
                        value = shelfSearchQuery,
                        onValueChange = { viewModel.setShelfSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search shelf",
                                color = SophisticatedTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search Shelf",
                                tint = SophisticatedGold,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (shelfSearchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setShelfSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = SophisticatedTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SophisticatedSurface,
                            unfocusedContainerColor = SophisticatedSurface,
                            focusedBorderColor = SophisticatedGold.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.06f),
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shelf_search")
                    )

                    // Sort Buttons: Name, Reading, Finished, All (horizontally scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShelfSortOption.values().forEach { option ->
                            val isSelected = shelfSortOption == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setShelfSortOption(option) },
                                label = {
                                    Text(
                                        text = option.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SophisticatedGold else SophisticatedTextMuted
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SophisticatedSurfaceVariant,
                                    selectedContainerColor = SophisticatedGoldContainer
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = SophisticatedGold.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("sort_chip_${option.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Book items or Empty State
            if (books.isEmpty()) {
                item {
                    if (shelfSearchQuery.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No books match \"$shelfSearchQuery\"",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        color = SophisticatedTextPrimary
                                    )
                                )
                                Text(
                                    text = "Try adjusting your search query or clear the filter.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SophisticatedTextMuted
                                    )
                                )
                                TextButton(onClick = { viewModel.setShelfSearchQuery("") }) {
                                    Text("Clear Shelf Search", color = SophisticatedGold)
                                }
                            }
                        }
                    } else {
                        EmptyShelfCard(onAddClick = onNavigateToSearch)
                    }
                }
            } else {
                items(books, key = { it.id }) { book ->
                    BookShelfCard(
                        book = book,
                        onClick = { onNavigateToBookDetails(book.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsSection(stats: ReadingStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "READING",
                value = stats.currentlyReadingCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "FINISHED",
                value = stats.booksFinishedCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "NOTES",
                value = stats.notesWrittenCount.toString(),
                modifier = Modifier.weight(1f)
            )
            val pagesDisplay = if (stats.pagesReadSum >= 1000) {
                val formatted = "%.1fk".format(stats.pagesReadSum / 1000f)
                formatted.replace(".0k", "k")
            } else {
                stats.pagesReadSum.toString()
            }
            StatCard(
                title = "PAGES",
                value = pagesDisplay,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = SophisticatedTextMuted
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    color = SophisticatedGold
                )
            )
        }
    }
}

@Composable
private fun BookShelfCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF3A3A39), Color(0xFF1C1C1B))
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover for ${book.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = SophisticatedTextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Book Details & Progress
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            color = SophisticatedTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = SophisticatedTextMuted
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sleek Progress Bar
                LinearProgressIndicator(
                    progress = { book.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (book.isFinished) SophisticatedSage else SophisticatedGold,
                    trackColor = SophisticatedBackground,
                    strokeCap = StrokeCap.Round
                )
            }

            // Percentage or Done indicator
            Box(
                modifier = Modifier.padding(end = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (book.isFinished) "Done" else "${book.progressPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (book.isFinished) SophisticatedSage else SophisticatedGold
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyShelfCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SophisticatedSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = SophisticatedGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = "Your shelf is quiet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = SophisticatedTextPrimary
                )
            )
            Text(
                text = "Search Open Library to add your current book and track your reading journey.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SophisticatedTextMuted,
                    fontSize = 13.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

