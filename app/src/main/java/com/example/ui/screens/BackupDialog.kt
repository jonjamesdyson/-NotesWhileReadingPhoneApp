package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.export.ExportHelper
import com.example.ui.export.sendExportEmail
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedGold
import com.example.ui.theme.SophisticatedSage
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.viewmodel.NookViewModel
import kotlinx.coroutines.launch

@Composable
fun BackupDialog(
    viewModel: NookViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    // File saver launcher for JSON export
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingExportJson != null) {
            val saved = ExportHelper.saveJsonToUri(context, uri, pendingExportJson!!)
            if (saved) {
                viewModel.postUserMessage("Backup saved to device successfully!")
            } else {
                viewModel.postUserMessage("Failed to save backup file.")
            }
            pendingExportJson = null
            onDismiss()
        }
    }

    // File picker launcher for JSON import
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val jsonContent = ExportHelper.readJsonFromUri(context, uri)
                if (jsonContent != null) {
                    viewModel.importBackupJson(jsonContent)
                } else {
                    viewModel.postUserMessage("Unable to read selected file.")
                }
                onDismiss()
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
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = SophisticatedGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Backup & Restore",
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
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Export your library, notes, reviews, and markdown formatted logs as a JSON backup, or import existing files.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SophisticatedTextMuted,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Option 1: Export JSON File (Save to Storage)
                BackupOptionCard(
                    title = "Export JSON File",
                    description = "Save full library backup (.json) with all markdown notes",
                    icon = Icons.Outlined.FileDownload,
                    iconTint = SophisticatedGold,
                    testTag = "btn_export_json_file",
                    onClick = {
                        viewModel.prepareBackupJson { json ->
                            pendingExportJson = json
                            createDocumentLauncher.launch("nook_backup_${System.currentTimeMillis() / 1000}.json")
                        }
                    }
                )

                // Option 2: Share JSON File (via FileProvider)
                BackupOptionCard(
                    title = "Share JSON Backup",
                    description = "Send JSON file via email, cloud drive, or messaging",
                    icon = Icons.Outlined.Share,
                    iconTint = SophisticatedGold,
                    testTag = "btn_share_json_file",
                    onClick = {
                        viewModel.prepareBackupJson { json ->
                            ExportHelper.shareJsonFile(context, json)
                            onDismiss()
                        }
                    }
                )

                // Option 3: Import JSON Backup
                BackupOptionCard(
                    title = "Import Backup File",
                    description = "Restore books, notes, reviews, and markdown logs",
                    icon = Icons.Outlined.FileUpload,
                    iconTint = SophisticatedSage,
                    testTag = "btn_import_json_file",
                    onClick = {
                        openDocumentLauncher.launch(
                            arrayOf("application/json", "text/*", "*/*")
                        )
                    }
                )

                // Option 4: Share formatted text summary
                BackupOptionCard(
                    title = "Email Text Summary",
                    description = "Send readable summary of your shelf and reading stats",
                    icon = Icons.Outlined.Email,
                    iconTint = SophisticatedTextMuted,
                    testTag = "btn_email_text_summary",
                    onClick = {
                        viewModel.prepareExportData { text ->
                            context.sendExportEmail(
                                subject = "Nook Reading Log Export",
                                body = text
                            )
                            onDismiss()
                        }
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SophisticatedTextMuted)
            }
        }
    )
}

@Composable
private fun BackupOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SophisticatedTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SophisticatedTextMuted,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
