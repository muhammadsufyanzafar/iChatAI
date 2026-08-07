package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.PromptFolderWithCount
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.PromptSortOrder
import com.zafar.ichatai.viewmodel.PromptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPromptsScreen(
    promptViewModel: PromptViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPromptClick: (String) -> Unit
) {
    val folders by promptViewModel.folders.collectAsState()
    val individualPrompts by promptViewModel.individualPrompts.collectAsState()
    val searchQuery by promptViewModel.searchQuery.collectAsState()
    val sortOrder by promptViewModel.sortOrder.collectAsState()
    val selectedFolderId by promptViewModel.selectedFolderId.collectAsState()

    val selectedFolder = remember(selectedFolderId, folders) {
        folders.find { it.folder.id == selectedFolderId }?.folder
    }

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var showAddPromptDialog by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<PromptFolderEntity?>(null) }
    var editingPrompt by remember { mutableStateOf<SavedPromptEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            selectedFolder?.name ?: stringResource(R.string.saved_prompts), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 24.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedFolderId != null) {
                                promptViewModel.selectFolder(null)
                            } else {
                                onBackClick()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Rounded.FilterList, contentDescription = stringResource(R.string.browse_categories))
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                PromptSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            promptViewModel.onSortOrderChange(order)
                                            showSortMenu = false
                                        },
                                        trailingIcon = { if (sortOrder == order) Icon(Icons.Default.Check, null) }
                                    )
                                }
                            }
                        }
                        if (selectedFolderId == null) {
                            IconButton(onClick = { showAddFolderDialog = true }) {
                                Icon(Icons.Rounded.CreateNewFolder, contentDescription = stringResource(R.string.new_folder))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddPromptDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_prompt))
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { promptViewModel.onSearchQueryChange(it) },
                    placeholder = { Text(stringResource(R.string.search_prompts), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    if (selectedFolderId == null) {
                        if (folders.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.my_folders),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(folders) { folderWithCount ->
                                FolderItem(
                                    folderWithCount = folderWithCount,
                                    onEdit = { editingFolder = folderWithCount.folder },
                                    onDelete = { itemToDelete = folderWithCount.folder },
                                    onClick = { promptViewModel.selectFolder(folderWithCount.folder.id) }
                                )
                            }
                        }

                        item {
                            Text(
                                stringResource(R.string.recent_individual_prompts),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                    } else {
                        item {
                            Text(
                                stringResource(R.string.prompts_in_folder, selectedFolder?.name ?: ""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    items(individualPrompts) { prompt ->
                        PromptItem(
                            prompt = prompt,
                            onEdit = { editingPrompt = prompt },
                            onDelete = { itemToDelete = prompt },
                            onClick = {
                                promptViewModel.updatePromptUsage(prompt.id)
                                onPromptClick(prompt.content)
                            }
                        )
                    }

                    if (individualPrompts.isEmpty() && selectedFolderId != null) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_prompts_in_folder), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddFolderDialog || editingFolder != null) {
        FolderDialog(
            folder = editingFolder,
            onDismiss = { 
                showAddFolderDialog = false
                editingFolder = null
            },
            onConfirm = { name, color ->
                if (editingFolder != null) {
                    promptViewModel.updateFolder(editingFolder!!.copy(name = name, colorHex = color))
                } else {
                    promptViewModel.addFolder(name, color)
                }
                showAddFolderDialog = false
                editingFolder = null
            }
        )
    }

    if (showAddPromptDialog || editingPrompt != null) {
        PromptDialog(
            prompt = editingPrompt,
            folders = folders.map { it.folder },
            initialFolderId = selectedFolderId,
            onDismiss = {
                showAddPromptDialog = false
                editingPrompt = null
            },
            onConfirm = { title, content, folderId, tag ->
                if (editingPrompt != null) {
                    promptViewModel.updatePrompt(editingPrompt!!.copy(title = title, content = content, folderId = folderId, tag = tag))
                } else {
                    promptViewModel.addPrompt(title, content, folderId, tag)
                }
                showAddPromptDialog = false
                editingPrompt = null
            }
        )
    }

    if (itemToDelete != null) {
        val isFolder = itemToDelete is PromptFolderEntity
        val itemName = if (isFolder) (itemToDelete as PromptFolderEntity).name else (itemToDelete as SavedPromptEntity).title
        
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_item_title, if (isFolder) "Folder" else "Prompt")) },
            text = { 
                Text(
                    if (isFolder) 
                        stringResource(R.string.delete_folder_msg, itemName)
                    else 
                        stringResource(R.string.delete_prompt_msg, itemName)
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isFolder) {
                            promptViewModel.deleteFolder(itemToDelete as PromptFolderEntity)
                        } else {
                            promptViewModel.deletePrompt(itemToDelete as SavedPromptEntity)
                        }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun FolderItem(
    folderWithCount: PromptFolderWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(parseColorSafe(folderWithCount.folder.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folderWithCount.folder.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.prompts_count, folderWithCount.promptCount),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PromptActionIcon(Icons.Default.Edit, "", onEdit)
                PromptActionIcon(Icons.Default.Delete, "", onDelete, isDelete = true)
            }
        }
    }
}

@Composable
fun PromptItem(
    prompt: SavedPromptEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prompt.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.last_used_format, 
                        formatTime(prompt.lastUsed, context), 
                        prompt.tag ?: stringResource(R.string.general)
                    ),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PromptActionIcon(Icons.Default.Edit, "", onEdit)
                PromptActionIcon(Icons.Default.Delete, "", onDelete, isDelete = true)
            }
        }
    }
}

@Composable
fun PromptActionIcon(icon: ImageVector, label: String, onClick: () -> Unit, isDelete: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .width(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDelete) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isDelete) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun FolderDialog(
    folder: PromptFolderEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(folder?.name ?: "") }
    var selectedColor by remember { mutableStateOf(folder?.colorHex ?: "#4285F4") }
    
    val colors = listOf("#4285F4", "#34A853", "#EA4335", "#FBBC05", "#9C27B0", "#607D8B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (folder == null) stringResource(R.string.new_folder) else stringResource(R.string.edit_folder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.select_color), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }) {
                Text(if (folder == null) stringResource(R.string.create) else stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun PromptDialog(
    prompt: SavedPromptEntity? = null,
    folders: List<PromptFolderEntity>,
    initialFolderId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, String?) -> Unit
) {
    var title by remember { mutableStateOf(prompt?.title ?: "") }
    var content by remember { mutableStateOf(prompt?.content ?: "") }
    var selectedFolderId by remember { mutableStateOf(prompt?.folderId ?: initialFolderId) }
    var tag by remember { mutableStateOf(prompt?.tag ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (prompt == null) stringResource(R.string.new_prompt) else stringResource(R.string.edit_prompt)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.prompt_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text(stringResource(R.string.prompt_content)) }, modifier = Modifier.fillMaxWidth().height(120.dp))
                OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text(stringResource(R.string.prompt_tag)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                Text(stringResource(R.string.assign_to_folder), style = MaterialTheme.typography.labelMedium)
                Column {
                    FolderSelectRow(stringResource(R.string.none), selectedFolderId == null) { selectedFolderId = null }
                    folders.forEach { folder ->
                        FolderSelectRow(folder.name, selectedFolderId == folder.id) { selectedFolderId = folder.id }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank() && content.isNotBlank()) onConfirm(title, content, selectedFolderId, tag.ifBlank { null }) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun FolderSelectRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(name, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
    }
}

fun formatTime(timestamp: Long, context: android.content.Context): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> context.getString(R.string.just_now)
        diff < 3600000 -> context.getString(R.string.mins_ago, diff / 60000)
        diff < 86400000 -> context.getString(R.string.hours_ago, diff / 3600000)
        diff < 172800000 -> context.getString(R.string.yesterday)
        else -> context.getString(R.string.days_ago, diff / 86400000)
    }
}

fun parseColorSafe(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4285F4) // Default Blue
    }
}
