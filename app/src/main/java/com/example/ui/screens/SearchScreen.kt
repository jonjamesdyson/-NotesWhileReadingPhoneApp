package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.OpenLibraryDoc
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.viewmodel.NookViewModel
import com.example.ui.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: NookViewModel,
    onNavigateBack: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    val quickTopics = listOf("Dune", "Tolkien", "Virginia Woolf", "Murakami", "Cozy Mystery", "Steinbeck")
    var showAddManualDialog by remember { mutableStateOf(false) }

    if (showAddManualDialog) {
        AddManualBookDialog(
            initialTitle = query.takeIf { it.isNotBlank() } ?: "",
            onDismiss = { showAddManualDialog = false },
            onAddBook = { title, author, totalPages, pagesRead, status, coverUrl ->
                viewModel.addManualBook(title, author, totalPages, pagesRead, status, coverUrl) {
                    onNavigateBack()
                }
                showAddManualDialog = false
            }
        )
    }

    Scaffold(
        containerColor = SophisticatedBackground,
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
                            contentDescription = "Navigate back",
                            tint = SophisticatedGold
                        )
                    }
                },
                title = {
                    Text(
                        text = "Search & Add Book",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = SophisticatedTextPrimary
                        )
                    )
                },
                actions = {
                    TextButton(
                        onClick = { showAddManualDialog = true },
                        modifier = Modifier.testTag("button_add_manual_topbar")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LibraryAdd,
                            contentDescription = "Add Manually",
                            tint = SophisticatedGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Manual",
                            color = SophisticatedGold,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Search Bar Input
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_field"),
                placeholder = {
                    Text(
                        text = "Search by title, author, or keyword...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SophisticatedTextMuted
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = SophisticatedGold
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = SophisticatedTextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SophisticatedGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = SophisticatedSurface,
                    unfocusedContainerColor = SophisticatedSurface,
                    focusedTextColor = SophisticatedTextPrimary,
                    unfocusedTextColor = SophisticatedTextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    viewModel.executeSearch()
                })
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick search tags
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(quickTopics) { topic ->
                    FilterChip(
                        selected = query == topic,
                        onClick = {
                            viewModel.onSearchQueryChanged(topic)
                            viewModel.executeSearch()
                        },
                        label = {
                            Text(
                                text = topic,
                                fontSize = 12.sp,
                                color = if (query == topic) SophisticatedBackground else SophisticatedTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SophisticatedSurface,
                            selectedContainerColor = SophisticatedGold
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = query == topic,
                            borderColor = Color.White.copy(alpha = 0.06f),
                            selectedBorderColor = SophisticatedGold
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Content / Results
            when (val state = searchState) {
                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SophisticatedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = SophisticatedGold,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = "Open Library Search",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Serif,
                                    color = SophisticatedTextPrimary
                                )
                            )
                            Text(
                                text = "Type a book title or author above to find millions of books across history.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SophisticatedTextMuted,
                                    fontSize = 13.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = { showAddManualDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SophisticatedGold
                                ),
                                border = BorderStroke(1.dp, SophisticatedGold.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("button_add_manually_idle")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LibraryAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Can't find your book? Add it manually", fontSize = 12.sp)
                            }
                        }
                    }
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = SophisticatedGold,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Searching Open Library...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SophisticatedTextMuted
                                )
                            )
                        }
                    }
                }

                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Unable to fetch books",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SophisticatedTextPrimary
                                )
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SophisticatedTextMuted
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.executeSearch() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SophisticatedGold,
                                    contentColor = SophisticatedBackground
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    text = "No books found matching \"$query\".",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SophisticatedTextMuted
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = { showAddManualDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SophisticatedGold,
                                        contentColor = SophisticatedBackground
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("button_add_custom_book_empty")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LibraryAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add \"$query\" Manually", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(state.results) { doc ->
                                SearchResultItem(
                                    doc = doc,
                                    onAddClick = {
                                        viewModel.addBookFromSearch(doc) {
                                            onNavigateBack()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    doc: OpenLibraryDoc,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(78.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SophisticatedSurfaceVariant)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (doc.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(doc.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover for ${doc.title}",
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

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = doc.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        color = SophisticatedTextPrimary,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = doc.displayAuthor,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SophisticatedTextMuted,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (doc.firstPublishYear != null) {
                        Text(
                            text = "${doc.firstPublishYear}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SophisticatedTextMuted.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SophisticatedTextMuted.copy(alpha = 0.4f)
                            )
                        )
                    }
                    Text(
                        text = "~${doc.displayPages} pages",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SophisticatedTextMuted.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Add Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedGold,
                    contentColor = SophisticatedBackground
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = SophisticatedBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

