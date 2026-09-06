package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Book
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
import java.io.File

@Composable
fun AddManualBookDialog(
    initialTitle: String = "",
    onDismiss: () -> Unit,
    onAddBook: (title: String, author: String, totalPages: Int, pagesRead: Int, status: String, coverImageUrl: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var author by remember { mutableStateOf("") }
    var totalPagesText by remember { mutableStateOf("300") }
    var pagesReadText by remember { mutableStateOf("0") }
    var selectedStatus by remember { mutableStateOf(Book.STATUS_CURRENTLY_READING) }
    var titleError by remember { mutableStateOf(false) }
    var coverImageUrl by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val coversDir = File(context.filesDir, "custom_covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val destFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                coverImageUrl = destFile.absolutePath
            } catch (e: Exception) {
                coverImageUrl = uri.toString()
            }
        }
    }

    val isReading = selectedStatus == Book.STATUS_CURRENTLY_READING

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SophisticatedSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = SophisticatedGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add Custom Book",
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Manually record books that aren't listed on Open Library.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SophisticatedTextMuted,
                        fontSize = 12.sp
                    )
                )

                // Custom Cover Upload Space
                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = SophisticatedBackground,
                    border = BorderStroke(
                        1.dp,
                        if (coverImageUrl.isNotBlank()) SophisticatedGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_upload_custom_cover")
                ) {
                    if (coverImageUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, SophisticatedGold.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(coverImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Custom Cover Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Cover Added",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SophisticatedGold,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "Tap to change image from gallery",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SophisticatedTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = { coverImageUrl = "" },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Cover",
                                    tint = SophisticatedTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SophisticatedSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Upload Cover",
                                    tint = SophisticatedGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Cover Image",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = SophisticatedTextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "Upload from gallery (optional)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SophisticatedTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SophisticatedSurfaceVariant,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                            ) {
                                Text(
                                    text = "Browse",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SophisticatedGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Book Title *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = if (titleError) Color(0xFFE57373) else SophisticatedGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required", color = Color(0xFFE57373)) }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = SophisticatedBackground,
                        unfocusedContainerColor = SophisticatedBackground,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_book_title")
                )

                // Author field
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    placeholder = { Text("e.g. Neil Gaiman", color = SophisticatedTextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SophisticatedGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = SophisticatedBackground,
                        unfocusedContainerColor = SophisticatedBackground,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_book_author")
                )

                // Pages Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalPagesText,
                        onValueChange = { totalPagesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Total Pages") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = SophisticatedGold,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = SophisticatedBackground,
                            unfocusedContainerColor = SophisticatedBackground,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_manual_total_pages")
                    )

                    OutlinedTextField(
                        value = pagesReadText,
                        onValueChange = { pagesReadText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Pages Read") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = SophisticatedBackground,
                            unfocusedContainerColor = SophisticatedBackground,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_manual_pages_read")
                    )
                }

                // Status selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = SophisticatedTextMuted,
                            fontSize = 10.sp
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isReading,
                            onClick = { selectedStatus = Book.STATUS_CURRENTLY_READING },
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
                                    modifier = Modifier.size(13.dp),
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = !isReading,
                            onClick = { selectedStatus = Book.STATUS_FINISHED },
                            label = {
                                Text(
                                    "Finished",
                                    fontSize = 11.sp,
                                    color = if (!isReading) SophisticatedOnSageContainer else SophisticatedTextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (!isReading) SophisticatedSage else SophisticatedTextMuted
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SophisticatedSurfaceVariant,
                                selectedContainerColor = SophisticatedSageContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = !isReading,
                                borderColor = Color.Transparent,
                                selectedBorderColor = SophisticatedSage.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val total = totalPagesText.toIntOrNull()?.coerceAtLeast(1) ?: 100
                    val read = if (selectedStatus == Book.STATUS_FINISHED) {
                        total
                    } else {
                        pagesReadText.toIntOrNull()?.coerceIn(0, total) ?: 0
                    }
                    val finalAuthor = author.trim().ifBlank { "Unknown Author" }
                    onAddBook(title.trim(), finalAuthor, total, read, selectedStatus, coverImageUrl)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedGold,
                    contentColor = SophisticatedBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("btn_confirm_add_manual_book")
            ) {
                Text("Add to Shelf", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SophisticatedTextMuted)
            }
        }
    )
}
