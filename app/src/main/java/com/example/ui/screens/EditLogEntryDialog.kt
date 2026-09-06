package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogEntry
import com.example.ui.components.MarkdownToolbar
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedGoldContainer
import com.example.ui.theme.SophisticatedSage
import com.example.ui.theme.SophisticatedSageContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary

@Composable
fun EditLogEntryDialog(
    entry: LogEntry,
    onDismiss: () -> Unit,
    onSave: (newContent: String, newType: String) -> Unit
) {
    var content by remember { mutableStateOf(entry.content) }
    var selectedType by remember { mutableStateOf(entry.type) }
    var contentError by remember { mutableStateOf(false) }

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
                    text = if (selectedType == LogEntry.TYPE_REVIEW) "Edit Review" else "Edit Note",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedType == LogEntry.TYPE_NOTE,
                        onClick = { selectedType = LogEntry.TYPE_NOTE },
                        label = {
                            Text(
                                "Note",
                                fontSize = 12.sp,
                                color = if (selectedType == LogEntry.TYPE_NOTE) SophisticatedGold else SophisticatedTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SophisticatedSurfaceVariant,
                            selectedContainerColor = SophisticatedGoldContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == LogEntry.TYPE_NOTE,
                            borderColor = Color.Transparent,
                            selectedBorderColor = SophisticatedGold.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FilterChip(
                        selected = selectedType == LogEntry.TYPE_REVIEW,
                        onClick = { selectedType = LogEntry.TYPE_REVIEW },
                        label = {
                            Text(
                                "Review",
                                fontSize = 12.sp,
                                color = if (selectedType == LogEntry.TYPE_REVIEW) SophisticatedSage else SophisticatedTextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SophisticatedSurfaceVariant,
                            selectedContainerColor = SophisticatedSageContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == LogEntry.TYPE_REVIEW,
                            borderColor = Color.Transparent,
                            selectedBorderColor = SophisticatedSage.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Markdown toolbar
                MarkdownToolbar(
                    currentText = content,
                    onTextChange = {
                        content = it
                        if (contentError && it.isNotBlank()) contentError = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Editable text
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if (contentError && it.isNotBlank()) contentError = false
                    },
                    placeholder = {
                        Text(
                            "Write note or review (Markdown supported)...",
                            color = SophisticatedTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    isError = contentError,
                    supportingText = if (contentError) {
                        { Text("Content cannot be empty", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    minLines = 4,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_log_entry"),
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isBlank()) {
                        contentError = true
                    } else {
                        onSave(content.trim(), selectedType)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedGold,
                    contentColor = SophisticatedBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("btn_save_edit_entry")
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
