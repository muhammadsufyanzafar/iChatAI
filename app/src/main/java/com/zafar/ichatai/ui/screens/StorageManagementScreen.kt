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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.GsonBuilder
import com.zafar.ichatai.R
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
                        Toast.makeText(context, context.getString(R.string.export_successful), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.export_failed, e.message), Toast.LENGTH_SHORT).show()
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
                            stringResource(R.string.storage_management_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                                Toast.makeText(context, context.getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
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
                    stringResource(R.string.clear_chat_history_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.clear_chat_history_msg),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory {
                            Toast.makeText(context, context.getString(R.string.chat_history_cleared), Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = {
                Text(
                    stringResource(R.string.select_export_format),
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
                            stringResource(R.string.json_format),
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
                            stringResource(R.string.csv_format),
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.export))
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
                stringResource(R.string.storage_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Vertical Bars Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val total = state.totalSpace.coerceAtLeast(1L).toFloat()
                
                // We use a non-linear scale (Math.pow(ratio, 0.4)) to make smaller categories visible 
                // while still allowing them to grow in real-time.
                val convHeight by animateFloatAsState(
                    targetValue = if (state.conversationsSize > 0) Math.pow(state.conversationsSize / total.toDouble(), 0.4).toFloat().coerceIn(0.15f, 1f) else 0.05f,
                    label = "convHeight"
                )
                val mediaHeight by animateFloatAsState(
                    targetValue = if (state.mediaSize > 0) Math.pow(state.mediaSize / total.toDouble(), 0.4).toFloat().coerceIn(0.15f, 1f) else 0.05f,
                    label = "mediaHeight"
                )
                val cacheHeight by animateFloatAsState(
                    targetValue = if (state.cacheSize > 0) Math.pow(state.cacheSize / total.toDouble(), 0.4).toFloat().coerceIn(0.15f, 1f) else 0.05f,
                    label = "cacheHeight"
                )
                val freeHeight by animateFloatAsState(
                    targetValue = if (state.freeSpace > 0) Math.pow(state.freeSpace / total.toDouble(), 0.4).toFloat().coerceIn(0.15f, 1f) else 0.05f,
                    label = "freeHeight"
                )

                VerticalStorageBar(MaterialTheme.colorScheme.primary, convHeight, Modifier.weight(1f))
                VerticalStorageBar(MaterialTheme.colorScheme.secondary, mediaHeight, Modifier.weight(1f))
                VerticalStorageBar(MaterialTheme.colorScheme.tertiary, cacheHeight, Modifier.weight(1f))
                VerticalStorageBar(Color(0xFF94A3B8).copy(alpha = 0.3f), freeHeight, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageLegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.conversations), state.conversationsSizeStr)
                StorageLegendItem(MaterialTheme.colorScheme.secondary, stringResource(R.string.media), state.mediaSizeStr)
                StorageLegendItem(MaterialTheme.colorScheme.tertiary, stringResource(R.string.cache), state.cacheSizeStr)
                StorageLegendItem(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), stringResource(R.string.free_space), "(rest)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            StorageProgressBar(state)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.used_format, state.usedSpaceStr),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.total_format, state.totalSpaceStr),
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
        if (convRatio > 0f) Box(Modifier.fillMaxHeight().weight(convRatio, fill = false).background(MaterialTheme.colorScheme.primary))
        if (mediaRatio > 0f) Box(Modifier.fillMaxHeight().weight(mediaRatio, fill = false).background(MaterialTheme.colorScheme.secondary))
        if (cacheRatio > 0f) Box(Modifier.fillMaxHeight().weight(cacheRatio, fill = false).background(MaterialTheme.colorScheme.tertiary))
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
                stringResource(R.string.cleanup_tools),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            CleanupItem(
                icon = Icons.Default.DeleteOutline,
                title = stringResource(R.string.clear_app_cache),
                subtitle = stringResource(R.string.clear_cache_desc, cacheSize),
                buttonText = stringResource(R.string.clear_cache),
                onButtonClick = onClearCache
            )

            Spacer(modifier = Modifier.height(24.dp))

            CleanupItem(
                icon = Icons.Default.ChatBubbleOutline,
                title = stringResource(R.string.clear_chat_history),
                subtitle = stringResource(R.string.clear_history_desc),
                buttonText = stringResource(R.string.clear_history),
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun DataExportCard(onConfigureExport: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.data_export),
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
                        stringResource(R.string.export_chat_history),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.export_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfigureExport,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.configure_export), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                    stringResource(R.string.auto_cleanup_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isEnabled) stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
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
                        stringResource(R.string.maintenance_policy),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.maintenance_policy_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = stringResource(R.string.configure),
                    color = MaterialTheme.colorScheme.primary,
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
                stringResource(R.string.auto_cleanup_policy),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.delete_older_than, days.toInt()),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = days,
                    onValueChange = { days = it },
                    valueRange = 7f..90f,
                    steps = 11, // 7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77, 84, 91 approx
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.days_7),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        stringResource(R.string.days_90),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(days.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
