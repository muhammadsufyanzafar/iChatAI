package com.zafar.ichatai.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.zafar.ichatai.model.UpdateInfo
import com.zafar.ichatai.viewmodel.UpdateUIState
import com.zafar.ichatai.viewmodel.UpdateViewModel

@Composable
fun AppVersionItem(
    updateViewModel: UpdateViewModel,
    currentDisplayVersion: String = "2.0.0"
) {
    val context = LocalContext.current
    val updateState by updateViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // Side effects listener for tracking completion notifications
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateUIState.UpToDate -> {
                Toast.makeText(context, "Your app is up to date!", Toast.LENGTH_SHORT).show()
                updateViewModel.resetState()
            }
            is UpdateUIState.Error -> {
                val errorMsg = (updateState as UpdateUIState.Error).message
                Toast.makeText(context, "Update Check Error: $errorMsg", Toast.LENGTH_LONG).show()
                updateViewModel.resetState()
            }
            else -> {}
        }
    }

    // Modal Display Trigger logic
    if (updateState is UpdateUIState.UpdateAvailable) {
        val info = (updateState as UpdateUIState.UpdateAvailable).updateInfo
        UpdatePromptDialog(
            updateInfo = info,
            onDismiss = {
                if (!info.isForceUpdate) updateViewModel.resetState()
            },
            onUpdateClick = { targetUrl ->
                handleUpdateRedirect(context, targetUrl)
            }
        )
    }

    // Interactive System Card Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "App Version",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = currentDisplayVersion,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Button(
            onClick = { updateViewModel.checkForUpdates() },
            enabled = updateState !is UpdateUIState.Checking,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary.copy(alpha = 0.1f),
                contentColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            if (updateState is UpdateUIState.Checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colorScheme.primary
                )
            } else {
                Text(
                    text = "Check",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun UpdatePromptDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onUpdateClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = { onUpdateClick(updateInfo.seeMoreUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Update Now")
            }
        },
        dismissButton = {
            if (!updateInfo.isForceUpdate) {
                TextButton(onClick = { onDismiss() }) {
                    Text("Later", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        },
        title = {
            Text(
                text = "New Update Available! (v${updateInfo.latestVersionName})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (updateInfo.platforms.isNotEmpty()) {
                    Text(
                        text = "Available On: ${updateInfo.platforms.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Text(
                    text = "What's New:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                updateInfo.changelog.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "•", color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                if (updateInfo.releaseDate.isNotEmpty()) {
                    Text(
                        text = "Released: ${updateInfo.releaseDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate,
            dismissOnClickOutside = !updateInfo.isForceUpdate
        )
    )
}

private fun handleUpdateRedirect(context: Context, url: String) {
    if (url.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No browser setup found to open update link.", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Download link is unavailable.", Toast.LENGTH_SHORT).show()
    }
}
