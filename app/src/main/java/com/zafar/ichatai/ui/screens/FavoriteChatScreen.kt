package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.zafar.ichatai.viewmodel.SortOrder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteChatScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onChatClick: (Long) -> Unit
) {
    val favoriteHistory by viewModel.favoriteHistory.collectAsState()
    val searchQuery by viewModel.favoriteSearchQuery.collectAsState()
    val currentSort by viewModel.favoriteSortOrder.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }
    
    var showSortMenu by remember { mutableStateOf(false) }

    if (showDeleteDialog && sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                sessionToDelete = null
            },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this favorite chat? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { viewModel.deleteChat(it.id) }
                        showDeleteDialog = false
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    sessionToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    GlowBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Favourites", 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            viewModel.onFavoriteSortChange(order)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (currentSort == order) Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onFavoriteSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    placeholder = { 
                        Text(
                            "Search favourites...", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                val topPinned = favoriteHistory.filter { it.session.isTopPinned }
                val regular = favoriteHistory.filter { !it.session.isTopPinned }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (favoriteHistory.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No favorites found", color = Color.Gray)
                            }
                        }
                    } else {
                        if (topPinned.isNotEmpty()) {
                            item {
                                SectionHeader("Top Pinned Favourites")
                            }
                            items(topPinned, key = { it.session.id }) { item ->
                                FavoriteItemCard(
                                    item = item,
                                    isTopPinned = true,
                                    onClick = { onChatClick(item.session.id) },
                                    onToggleTopPin = { viewModel.toggleTopPinnedChat(item.session.id, item.session.isTopPinned) },
                                    onUnstar = { viewModel.togglePinChat(item.session.id, item.session.isPinned) },
                                    onDelete = { 
                                        sessionToDelete = item.session
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }

                        if (regular.isNotEmpty()) {
                            item {
                                SectionHeader("Regular Favourites")
                            }
                            items(regular, key = { it.session.id }) { item ->
                                FavoriteItemCard(
                                    item = item,
                                    isTopPinned = false,
                                    onClick = { onChatClick(item.session.id) },
                                    onToggleTopPin = { viewModel.toggleTopPinnedChat(item.session.id, item.session.isTopPinned) },
                                    onUnstar = { viewModel.togglePinChat(item.session.id, item.session.isPinned) },
                                    onDelete = { 
                                        sessionToDelete = item.session
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun FavoriteItemCard(
    item: ChatSessionWithCount,
    isTopPinned: Boolean,
    onClick: () -> Unit,
    onToggleTopPin: () -> Unit,
    onUnstar: () -> Unit,
    onDelete: () -> Unit
) {
    val isFavorite = item.session.isPinned
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isTopPinned) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .clickable { onToggleTopPin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTopPinned) Icons.Default.Star else Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.session.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "Last accessed: ${TimeUtils.formatRelativeTime(item.session.timestamp)} | ${item.messageCount} messages",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onUnstar) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Unstar",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
