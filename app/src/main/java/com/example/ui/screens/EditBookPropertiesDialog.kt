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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import java.io.File

@Composable
fun EditBookPropertiesDialog(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (title: String, author: String, totalPages: Int, pagesRead: Int, coverUrl: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var totalPagesText by remember { mutableStateOf(book.totalPages.toString()) }
    var pagesReadText by remember { mutableStateOf(book.pagesRead.toString()) }
    var coverImageUrl by remember { mutableStateOf(book.coverImageUrl) }
    var titleError by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = SophisticatedGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Edit Book Properties",
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cover Image Section
                Text(
                    text = "BOOK COVER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = SophisticatedTextMuted
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(98.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SophisticatedSurfaceVariant)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(coverImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Cover preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = SophisticatedTextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedSurfaceVariant,
                                contentColor = SophisticatedGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SophisticatedGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_change_cover_image")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (coverImageUrl.isBlank()) "Choose Photo" else "Change Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (coverImageUrl.isNotBlank()) {
                            TextButton(
                                onClick = { coverImageUrl = "" },
                                colors = ButtonDefaults.textButtonColors(contentColor = SophisticatedTextMuted),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Cover", fontSize = 12.sp)
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
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = SophisticatedGold
                        )
                    },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title cannot be empty", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_book_title"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = SophisticatedBackground,
                        unfocusedContainerColor = SophisticatedBackground,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    )
                )

                // Author field
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SophisticatedTextMuted
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_book_author"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = SophisticatedBackground,
                        unfocusedContainerColor = SophisticatedBackground,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    )
                )

                // Pages Row (Total Pages + Pages Read)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalPagesText,
                        onValueChange = {
                            if (it.isEmpty() || it.all { ch -> ch.isDigit() }) {
                                totalPagesText = it
                            }
                        },
                        label = { Text("Total Pages") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = SophisticatedTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_edit_total_pages"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = SophisticatedBackground,
                            unfocusedContainerColor = SophisticatedBackground,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = pagesReadText,
                        onValueChange = {
                            if (it.isEmpty() || it.all { ch -> ch.isDigit() }) {
                                pagesReadText = it
                            }
                        },
                        label = { Text("Pages Read") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = SophisticatedTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_edit_pages_read"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = SophisticatedBackground,
                            unfocusedContainerColor = SophisticatedBackground,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val total = totalPagesText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val read = pagesReadText.toIntOrNull()?.coerceIn(0, total) ?: 0
                    val finalAuthor = author.trim().ifBlank { "Unknown Author" }
                    onSave(title.trim(), finalAuthor, total, read, coverImageUrl)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedGold,
                    contentColor = SophisticatedBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("btn_save_edit_book_properties")
            ) {
                Text("Save Changes", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = SophisticatedTextMuted)
            ) {
                Text("Cancel")
            }
        }
    )
}
