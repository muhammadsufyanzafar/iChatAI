package com.zafar.ichatai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val contentDescription: String
)

@Composable
fun NavDrawerContent(
    isOnline: Boolean = true,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    chatHistory: List<ChatSessionWithCount> = emptyList(),
    onChatClick: (ChatSessionEntity) -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val drawerBg = colorScheme.surface
    val textColor = colorScheme.onSurface
    val sectionHeaderColor = colorScheme.onSurfaceVariant

    val generalItems = listOf(
        NavItem(Icons.Rounded.History, "Chat History", "history", "View chat history"),
        NavItem(Icons.Rounded.StarOutline, "Favorites", "favorites", "View favorite chats"),
        NavItem(Icons.Rounded.FolderOpen, "Saved Prompts", "prompts", "View saved prompts")
    )

    val accountItems = listOf(
        NavItem(Icons.Rounded.Shield, "Subscription", "subscription", "Manage subscription"),
        NavItem(Icons.Rounded.Settings, "Settings", "settings", "App settings"),
        NavItem(Icons.Rounded.PersonOutline, "Account Details", "account", "View account details")
    )

    val supportItems = listOf(
        NavItem(Icons.Rounded.Info, "Help & FAQ", "help", "Get help and read FAQ"),
        NavItem(Icons.Rounded.ChatBubbleOutline, "Send Feedback", "feedback", "Send feedback")
    )

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        drawerShape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(drawerBg)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Profile Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(modifier = Modifier.size(54.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_user_male),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF673AB7)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Muhammad Sufyan Zafar",
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Find previous chats...",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search icon",
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search",
                                    tint = textColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = textColor.copy(alpha = 0.2f),
                        unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                        focusedContainerColor = textColor.copy(alpha = 0.05f),
                        unfocusedContainerColor = textColor.copy(alpha = 0.05f),
                        cursorColor = textColor
                    ),
                    textStyle = TextStyle(fontSize = 14.sp, color = textColor)
                )

                if (chatHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Recent Chats" else "Search Results",
                        color = sectionHeaderColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatHistory.take(8)) { chat ->
                            QuickChatChip(chat, textColor, onChatClick)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item { DrawerSectionHeader("General", sectionHeaderColor) }
                    items(generalItems.size) { index ->
                        DrawerNavigationItem(generalItems[index], textColor, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader("Account", sectionHeaderColor) }
                    items(accountItems.size) { index ->
                        DrawerNavigationItem(accountItems[index], textColor, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader("Support", sectionHeaderColor) }
                    items(supportItems.size) { index ->
                        DrawerNavigationItem(supportItems[index], textColor, onItemClick)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

                    Surface(
                        onClick = { onItemClick("credits") },
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(24.dp),
                        color = textColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f)),
                        modifier = Modifier.scale(scale)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "AI Credits icon",
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AI Credits",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onLogoutClick) {
                        Text(
                            text = "Log Out",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickChatChip(
    chat: ChatSessionWithCount,
    textColor: Color,
    onClick: (ChatSessionEntity) -> Unit
) {
    Surface(
        onClick = { onClick(chat.session) },
        shape = RoundedCornerShape(16.dp),
        color = textColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f)),
        modifier = Modifier.widthIn(max = 120.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chat.session.title,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DrawerSectionHeader(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
    )
}

@Composable
fun DrawerNavigationItem(item: NavItem, textColor: Color, onClick: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = item.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        selected = false,
        onClick = { onClick(item.route) },
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = textColor.copy(alpha = 0.6f),
            unselectedTextColor = textColor
        ),
        modifier = Modifier
            .padding(vertical = 2.dp)
            .scale(scale),
        interactionSource = interactionSource
    )
}
