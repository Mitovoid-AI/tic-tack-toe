package com.tictactoe.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tictactoe.config.AppConfig
import com.tictactoe.util.UpdateInfo
import com.tictactoe.util.UpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(updateInfo: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primary = AppConfig.primaryColor()
    val textPrimary = AppConfig.textPrimaryColor()
    val textSecondary = AppConfig.textSecondaryColor()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var error by remember { mutableStateOf<String?>(null) }

    // Simulate progress while downloading
    LaunchedEffect(isDownloading) {
        if (isDownloading) {
            downloadProgress = 0f
            while (downloadProgress < 0.9f) {
                delay(300)
                downloadProgress = (downloadProgress + 0.05f).coerceAtMost(0.9f)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        containerColor = AppConfig.surfaceColor(),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = "Update Available",
                    color = primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = updateInfo.version,
                    color = textSecondary,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column {
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Text(
                        text = updateInfo.releaseNotes.take(300),
                        color = textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isDownloading) {
                    Text(
                        text = "Downloading...",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primary,
                        trackColor = primary.copy(alpha = 0.2f)
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = AppConfig.secondaryColor(),
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isDownloading) return@TextButton
                    isDownloading = true
                    error = null
                    scope.launch {
                        try {
                            val uri = UpdateManager.downloadApk(context, updateInfo.downloadUrl)
                            if (uri != null) {
                                delay(500)
                                UpdateManager.installApk(context, uri)
                                onDismiss()
                            } else {
                                error = "Download failed. Try again."
                                isDownloading = false
                            }
                        } catch (e: Exception) {
                            error = "Error: ${e.message ?: "Unknown"}"
                            isDownloading = false
                        }
                    }
                },
                enabled = !isDownloading
            ) {
                Text(
                    text = if (isDownloading) "DOWNLOADING..." else "UPDATE",
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("LATER", color = textSecondary)
                }
            }
        }
    )
}
