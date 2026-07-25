package com.zafar.ichatai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.PromptFolderWithCount
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import com.zafar.ichatai.viewmodel.PromptSortOrder
import com.zafar.ichatai.viewmodel.PromptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPromptsScreen(
    promptViewModel: PromptViewModel = viewModel(),
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        selectedFolder?.name ?: "Saved Prompts", 
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "Filter")
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
                            Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
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
                Icon(Icons.Default.Add, contentDescription = "Add Prompt")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { promptViewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search prompts...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
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
                                "My Folders",
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
                                onDelete = { promptViewModel.deleteFolder(folderWithCount.folder) },
                                onClick = { promptViewModel.selectFolder(folderWithCount.folder.id) }
                            )
                        }
                    }

                    item {
                        Text(
                            "Recent & Individual Prompts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            "Prompts in ${selectedFolder?.name}",
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
                        onDelete = { promptViewModel.deletePrompt(prompt) },
                        onClick = {
                            promptViewModel.updatePromptUsage(prompt.id)
                            onPromptClick(prompt.content)
                        }
                    )
                }

                if (individualPrompts.isEmpty() && selectedFolderId != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No prompts in this folder", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
}

@Composable
fun FolderItem(
    folderWithCount: PromptFolderWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
            )
 {
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
                    text = "${folderWithCount.promptCount} prompts",
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text = "Last used: ${formatTime(prompt.lastUsed)} | [Tag: ${prompt.tag ?: "General"}]",
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
            tint = if (isDelete) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isDelete) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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
        title = { Text(if (folder == null) "New Folder" else "Edit Folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Color", style = MaterialTheme.typography.labelMedium)
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
                Text(if (folder == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text(if (prompt == null) "New Prompt" else "Edit Prompt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Prompt Content") }, modifier = Modifier.fillMaxWidth().height(120.dp))
                OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("Tag (e.g. Business)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                Text("Assign to Folder", style = MaterialTheme.typography.labelMedium)
                Column {
                    FolderSelectRow("None", selectedFolderId == null) { selectedFolderId = null }
                    folders.forEach { folder ->
                        FolderSelectRow(folder.name, selectedFolderId == folder.id) { selectedFolderId = folder.id }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank() && content.isNotBlank()) onConfirm(title, content, selectedFolderId, tag.ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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

fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} mins ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 172800000 -> "Yesterday"
        else -> "${diff / 86400000} days ago"
    }
}

fun parseColorSafe(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4285F4) // Default Blue
    }
}
