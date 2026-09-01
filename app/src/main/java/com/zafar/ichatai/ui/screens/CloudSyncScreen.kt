package com.zafar.ichatai.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.CloudSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    viewModel: CloudSyncViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    LaunchedEffect(uiState.syncError) {
        uiState.syncError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearSyncError()
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.handleSignInResult(account)
            } catch (e: ApiException) {
                viewModel.handleSignInResult(null, e)
                Toast.makeText(context, "Sign-in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                viewModel.handleSignInResult(null)
                Toast.makeText(context, "An unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.handleSignInResult(null)
        }
    }

    var showErrorLogDialog by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Cloud Backup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // Account Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Google Account",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.googleAccount?.email ?: "Not Connected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            
                            if (uiState.googleAccount == null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { signInLauncher.launch(viewModel.getSignInIntent()) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Connect Google Account")
                                }
                            } else if (!uiState.isDriveAuthorized) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { signInLauncher.launch(viewModel.getSignInIntent()) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                                ) {
                                    Text("Grant Drive Permission")
                                }
                            }
                        }
                    }
                }

                if (uiState.isDriveAuthorized) {
                    // Progress Section (Visible only when syncing)
                    item {
                        AnimatedVisibility(
                            visible = uiState.isSyncing,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                alpha = 0.6f
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.cancelSync() },
                                            modifier = Modifier.size(32.dp).background(colorScheme.surfaceVariant, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Stop",
                                                modifier = Modifier.size(16.dp),
                                                tint = colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = uiState.syncStage,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${(uiState.syncProgress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { uiState.syncProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                        color = colorScheme.primary,
                                        trackColor = colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }

                    // Status Section
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                StatusRow("Backup enabled", true)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.onSurface.copy(alpha = 0.05f))
                                
                                val lastBackup = if (uiState.lastSyncTime > 0) {
                                    val isToday = android.text.format.DateUtils.isToday(uiState.lastSyncTime)
                                    val pattern = if (isToday) "'Today', h:mm a" else "MMM dd, h:mm a"
                                    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(uiState.lastSyncTime))
                                } else "Never"
                                
                                StatusRow("Last backup", lastBackup)
                                StatusRow("Backup size", uiState.backupSize)
                                
                                val statusColor = when(uiState.syncStatus) {
                                    "Synced" -> Color(0xFF10B981)
                                    "Error" -> Color(0xFFEF4444)
                                    else -> colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurface.copy(alpha = 0.6f))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (uiState.syncStatus == "Synced") Icons.Default.CloudDone else Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(uiState.syncStatus, fontWeight = FontWeight.Bold, color = statusColor)
                                    }
                                }

                                if (uiState.lastSyncTime > 0) {
                                    Text(
                                        text = "Last successful backup: ${SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(uiState.lastSyncTime))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.syncNow() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !uiState.isSyncing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Backup Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.importFromCloud() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !uiState.isSyncing,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.2f))
                            ) {
                                Text("Restore Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    // Settings Section
                    item {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = colorScheme.onBackground
                        )
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SettingSwitch("Automatic Backup", uiState.isAutoSyncEnabled) { viewModel.toggleAutoSync(it) }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.onSurface.copy(alpha = 0.05f))
                                
                                SettingInfo("Backup frequency", "Every 24 hours")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.onSurface.copy(alpha = 0.05f))
                                
                                SettingSwitch("Wi-Fi only", uiState.isSyncOverWifiOnly) { viewModel.toggleSyncOverWifi(it) }
                            }
                        }
                    }

                    // Footer Actions
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextButton(onClick = { showDisconnectDialog = true }) {
                                Text("Disconnect Google Account", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                            
                            Text(
                                text = "View Sync Error Log",
                                modifier = Modifier.clickable { showErrorLogDialog = true }.padding(top = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }

    if (showErrorLogDialog) {
        AlertDialog(
            onDismissRequest = { showErrorLogDialog = false },
            title = { Text("Sync Error Log") },
            text = {
                Text(
                    text = uiState.errorLog.ifBlank { "No errors reported." },
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showErrorLogDialog = false }) { Text("Close") }
            },
            dismissButton = {
                if (uiState.errorLog.isNotBlank()) {
                    TextButton(onClick = { viewModel.clearErrorLog() }) {
                        Text("Clear", color = Color(0xFFEF4444))
                    }
                }
            }
        )
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect Account?") },
            text = { Text("This will stop all cloud backups and sign you out of your Google account for this app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showDisconnectDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Disconnect", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusRow(label: String, value: Any) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (value is Boolean) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (value) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (value) "Enabled" else "Disabled", 
                    fontWeight = FontWeight.Bold, 
                    color = colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = value.toString(), 
                fontWeight = FontWeight.Bold, 
                color = colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingInfo(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
