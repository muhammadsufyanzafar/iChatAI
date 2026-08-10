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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import com.zafar.ichatai.ui.components.GlassCard
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import coil.compose.AsyncImage

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
    userName: String = "User",
    userAvatarUri: String? = null,
    userGender: String = "Male",
    onSearchQueryChange: (String) -> Unit = {},
    chatHistory: List<ChatSessionWithCount> = emptyList(),
    onChatClick: (ChatSessionEntity) -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val drawerBg = colorScheme.surface
    val textColor = colorScheme.onSurface
    val sectionHeaderColor = colorScheme.onSurfaceVariant

    val generalItems = listOf(
        NavItem(Icons.Rounded.History, stringResource(R.string.chat_history), "history", stringResource(R.string.view_chat_history)),
        NavItem(Icons.Rounded.StarOutline, stringResource(R.string.favorites_title), "favorites", stringResource(R.string.view_favorite_chats)),
        NavItem(Icons.Rounded.FolderOpen, stringResource(R.string.saved_prompts), "prompts", stringResource(R.string.view_saved_prompts))
    )

    val accountItems = listOf(
        NavItem(Icons.Rounded.Shield, stringResource(R.string.subscription), "subscription", stringResource(R.string.manage_subscription)),
        NavItem(Icons.Rounded.Settings, stringResource(R.string.settings), "settings", stringResource(R.string.app_settings)),
        NavItem(Icons.Rounded.PersonOutline, stringResource(R.string.account_details_title), "account", stringResource(R.string.view_account_details))
    )

    val supportItems = listOf(
        NavItem(Icons.Rounded.Info, stringResource(R.string.help_faq), "help", stringResource(R.string.get_help_faq)),
        NavItem(Icons.Rounded.ChatBubbleOutline, stringResource(R.string.send_feedback), "feedback", stringResource(R.string.send_feedback))
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
                    val profileImageModifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)

                    Box(modifier = Modifier.size(54.dp)) {
                        if (userAvatarUri != null) {
                            if (userAvatarUri.startsWith("res:")) {
                                val resId = when (userAvatarUri) {
                                    "res:avatar_user_male" -> R.drawable.avatar_user_male
                                    "res:avatar_user_female" -> R.drawable.avatar_user_female
                                    else -> R.drawable.avatar_default
                                }
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = stringResource(R.string.user_profile_pic),
                                    modifier = profileImageModifier,
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = userAvatarUri,
                                    contentDescription = stringResource(R.string.user_profile_pic),
                                    modifier = profileImageModifier,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            val avatarRes = when (userGender) {
                                "Female" -> R.drawable.avatar_user_female
                                "Male" -> R.drawable.avatar_user_male
                                else -> R.drawable.avatar_default
                            }
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = stringResource(R.string.user_profile_pic),
                                modifier = profileImageModifier,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
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
                            text = stringResource(R.string.find_chats_placeholder),
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.clear),
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
                        text = if (searchQuery.isEmpty()) stringResource(R.string.recent_chats) else stringResource(R.string.search_results),
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
                    item { DrawerSectionHeader(stringResource(R.string.general), sectionHeaderColor) }
                    items(generalItems.size) { index ->
                        DrawerNavigationItem(generalItems[index], textColor, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader(stringResource(R.string.account), sectionHeaderColor) }
                    items(accountItems.size) { index ->
                        DrawerNavigationItem(accountItems[index], textColor, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader(stringResource(R.string.support), sectionHeaderColor) }
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

                    GlassCard(
                        onClick = { onItemClick("credits") },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.scale(scale)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.ai_credits),
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
    GlassCard(
        onClick = { onClick(chat.session) },
        shape = RoundedCornerShape(16.dp),
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
