package com.zafar.ichatai.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.GsonBuilder
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.StorageUsageState
import com.zafar.ichatai.viewmodel.StorageViewModel
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onBackClick: () -> Unit = {},
    viewModel: StorageViewModel = hiltViewModel()
) {
    val storageState by viewModel.storageState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("JSON") }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var showAutoCleanupDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    val messages = viewModel.getAllMessages()
                    val content = if (exportFormat == "JSON") {
                        GsonBuilder().setPrettyPrinting().create().toJson(messages)
                    } else {
                        // CSV Format
                        val sb = StringBuilder("id,sessionId,content,role,timestamp\n")
                        messages.forEach { msg ->
                            sb.append("${msg.id},${msg.sessionId},\"${msg.content.replace("\"", "\"\"")}\",${msg.role},${msg.timestamp}\n")
                        }
                        sb.toString()
                    }
                    
                    try {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(content)
                            }
                        }
                        Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Data & Storage Management",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Storage Usage Section
                item {
                    StorageUsageCard(storageState)
                }

                // Cleanup Tools Section
                item {
                    CleanupToolsCard(
                        cacheSize = storageState.cacheSizeStr,
                        onClearCache = {
                            viewModel.clearCache {
                                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClearHistory = { showDeleteConfirm = true }
                    )
                }

                // Data Export Section
                item {
                    DataExportCard(
                        onConfigureExport = { showExportFormatDialog = true }
                    )
                }

                // Auto-Cleanup Section
                item {
                    AutoCleanupCard(
                        isEnabled = storageState.isAutoCleanupEnabled,
                        onToggle = { viewModel.toggleAutoCleanup(it) },
                        onConfigure = { showAutoCleanupDialog = true }
                    )
                }
            }
        }
    }

    if (showAutoCleanupDialog) {
        AutoCleanupDialog(
            currentDays = storageState.autoCleanupDays,
            onDismiss = { showAutoCleanupDialog = false },
            onConfirm = { days ->
                viewModel.setAutoCleanupDays(days)
                showAutoCleanupDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    "Clear Chat History?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "This will permanently delete all your conversation logs. This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory {
                            Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = {
                Text(
                    "Select Export Format",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportFormat = "JSON" }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(selected = exportFormat == "JSON", onClick = { exportFormat = "JSON" })
                        Text(
                            "JSON (.json)",
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportFormat = "CSV" }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(selected = exportFormat == "CSV", onClick = { exportFormat = "CSV" })
                        Text(
                            "CSV (.csv)",
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportFormatDialog = false
                        val fileName = "ichatai_backup_${System.currentTimeMillis()}.${exportFormat.lowercase()}"
                        createDocumentLauncher.launch(fileName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Export")
                }
            }
        )
    }
}

@Composable
fun StorageUsageCard(state: StorageUsageState) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Storage Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Vertical Bars Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val max = maxOf(state.conversationsSize, state.mediaSize, state.cacheSize, 1L).toFloat()
                
                VerticalStorageBar(Color(0xFF60A5FA), 1.0f, Modifier.weight(1f)) // Largest
                VerticalStorageBar(Color(0xFFC084FC), (state.mediaSize.toFloat() / max).coerceAtLeast(0.2f), Modifier.weight(1f))
                VerticalStorageBar(Color(0xFF4ADE80), (state.cacheSize.toFloat() / max).coerceAtLeast(0.15f), Modifier.weight(1f))
                VerticalStorageBar(Color(0xFF94A3B8).copy(alpha = 0.3f), 0.1f, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageLegendItem(Color(0xFF60A5FA), "Conversations", state.conversationsSizeStr)
                StorageLegendItem(Color(0xFFC084FC), "Media", state.mediaSizeStr)
                StorageLegendItem(Color(0xFF4ADE80), "Cache", state.cacheSizeStr)
                StorageLegendItem(Color(0xFF94A3B8).copy(alpha = 0.5f), "Free Space", "(rest)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            StorageProgressBar(state)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${state.usedSpaceStr} Used",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${state.totalSpaceStr} Total",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun VerticalStorageBar(color: Color, heightFraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight(heightFraction)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
    )
}

@Composable
fun StorageProgressBar(state: StorageUsageState) {
    val total = state.totalSpace.toFloat()
    if (total == 0f) return

    val convRatio = state.conversationsSize.toFloat() / total
    val mediaRatio = state.mediaSize.toFloat() / total
    val cacheRatio = state.cacheSize.toFloat() / total

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        if (convRatio > 0f) Box(Modifier.fillMaxHeight().weight(convRatio, fill = false).background(Color(0xFF60A5FA)))
        if (mediaRatio > 0f) Box(Modifier.fillMaxHeight().weight(mediaRatio, fill = false).background(Color(0xFFC084FC)))
        if (cacheRatio > 0f) Box(Modifier.fillMaxHeight().weight(cacheRatio, fill = false).background(Color(0xFF4ADE80)))
    }
}

@Composable
fun StorageLegendItem(color: Color, label: String, size: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(
                size,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CleanupToolsCard(
    cacheSize: String,
    onClearCache: () -> Unit,
    onClearHistory: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Cleanup Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            CleanupItem(
                icon = Icons.Default.DeleteOutline,
                title = "Clear App Cache",
                subtitle = "Temp files and images. Frees up ~$cacheSize.",
                buttonText = "Clear Cache",
                onButtonClick = onClearCache
            )

            Spacer(modifier = Modifier.height(24.dp))

            CleanupItem(
                icon = Icons.Default.ChatBubbleOutline,
                title = "Clear Chat History",
                subtitle = "Permanently delete all conversation logs.",
                buttonText = "Clear History",
                onButtonClick = onClearHistory
            )
        }
    }
}

@Composable
fun CleanupItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
        ) {
            Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
        }
    }
}

@Composable
fun DataExportCard(onConfigureExport: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Data Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Export Chat History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Download all messages as JSON or CSV.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfigureExport,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                ) {
                    Text("Configure Export", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                }
            }
        }
    }
}

@Composable
fun AutoCleanupCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Auto-Cleanup Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF8B5CF6),
                        checkedTrackColor = Color(0xFF8B5CF6).copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isEnabled) "Status: Active & Working" else "Status: Inactive",
                color = if (isEnabled) Color(0xFF4ADE80) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Maintenance Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Automatically delete messages older than selected days.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "Configure",
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onConfigure() }
                )
            }
        }
    }
}

@Composable
fun AutoCleanupDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var days by remember { mutableStateOf(currentDays.toFloat()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Auto-Cleanup Policy",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    "Delete messages older than ${days.toInt()} days.",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = days,
                    onValueChange = { days = it },
                    valueRange = 7f..90f,
                    steps = 11, // 7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77, 84, 91 approx
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF8B5CF6),
                        activeTrackColor = Color(0xFF8B5CF6)
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "7 Days",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "90 Days",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(days.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
