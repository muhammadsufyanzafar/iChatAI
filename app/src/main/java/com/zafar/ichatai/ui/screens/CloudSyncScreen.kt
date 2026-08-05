package com.zafar.ichatai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.utils.TimeUtils
import com.zafar.ichatai.viewmodel.CloudSyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    viewModel: CloudSyncViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.handleSignInResult(account)
            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.handleSignInResult(null)
            }
        } else {
            viewModel.handleSignInResult(null)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrorLogDialog by remember { mutableStateOf(false) }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Cloud Sync",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = colorScheme.onBackground
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // Google Account Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.googleAccount != null) "Connected as" else "Google Drive Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.googleAccount?.email ?: "Sign in to backup your data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (uiState.googleAccount == null) {
                                Button(
                                    onClick = { signInLauncher.launch(viewModel.getSignInIntent()) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Sign In", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Cloud Sync Status Section
                item {
                    SyncSection(title = "Cloud Sync Status") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Automatic Backup",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    "Periodically backup data to cloud.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = uiState.isAutoSyncEnabled,
                                onCheckedChange = { viewModel.toggleAutoSync(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Sync Over Wi-Fi & Cellular",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    "Sync data across devices. Data usage may apply.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = !uiState.isSyncOverWifiOnly,
                                onCheckedChange = { viewModel.toggleSyncOverWifi(!it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val lastSyncText = if (uiState.lastSyncTime > 0) 
                            "Last synced: ${TimeUtils.formatRelativeTime(uiState.lastSyncTime)}" 
                            else "Never synced"
                        
                        Text(
                            text = lastSyncText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.lastSyncTime > 0) Color(0xFF10B981) else colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.End),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Manual Sync Section
                item {
                    SyncSection(title = "Manual Sync") {
                        Text(
                            "Force an immediate sync of all conversation logs, settings, and history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Button(
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.googleAccount != null && !uiState.isSyncing,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Sync Now", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.importFromCloud() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.googleAccount != null && !uiState.isSyncing,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.2f))
                        ) {
                            Text("Import from Cloud", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Sync Options Section
                item {
                    SyncSection(title = "Sync Options") {
                        SyncOptionItem("Conversations & History", uiState.isSyncHistoryEnabled) { viewModel.toggleSyncHistory(it) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colorScheme.onSurface.copy(alpha = 0.1f))
                        SyncOptionItem("Images & files", uiState.isSyncImagesEnabled) { viewModel.toggleSyncImages(it) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colorScheme.onSurface.copy(alpha = 0.1f))
                        SyncOptionItem("Settings & Preferences", uiState.isSyncSettingsEnabled) { viewModel.toggleSyncSettings(it) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colorScheme.onSurface.copy(alpha = 0.1f))
                        SyncOptionItem("Saved Prompts & Templates", uiState.isSyncPromptsEnabled) { viewModel.toggleSyncPrompts(it) }
                    }
                }

                // Delete Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text(
                                "Delete all data from cloud",
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Sync Error Log",
                            modifier = Modifier.clickable { showErrorLogDialog = true },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurface.copy(alpha = 0.8f),
            title = { Text("Delete Cloud Data?") },
            text = { Text("This will permanently remove all your backup data from Google Drive. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCloudData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }

    if (showErrorLogDialog) {
        AlertDialog(
            onDismissRequest = { showErrorLogDialog = false },
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurface.copy(alpha = 0.8f),
            title = { Text("Sync Error Log") },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (uiState.errorLog.isBlank()) {
                        Text("No errors reported.", color = colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn {
                            item {
                                Text(
                                    uiState.errorLog,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showErrorLogDialog = false }) {
                    Text("Close", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (uiState.errorLog.isNotBlank()) {
                    TextButton(onClick = { viewModel.clearErrorLog() }) {
                        Text("Clear Log", color = Color(0xFFEF4444))
                    }
                }
            }
        )
    }
}

@Composable
fun SyncSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SyncOptionItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = colorScheme.onSurface.copy(alpha = 0.3f),
                checkmarkColor = Color.White
            )
        )
    }
}
