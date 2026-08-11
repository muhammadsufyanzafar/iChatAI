package com.zafar.ichatai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zafar.ichatai.R
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount

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
    val textColor = colorScheme.onSurface
    val sectionHeaderColor = colorScheme.primary.copy(alpha = 0.8f)

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
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface,
                            colorScheme.surfaceVariant.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            // Decorative background element
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .background(colorScheme.primary.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Profile Section in a Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    onClick = { onItemClick("account") }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val profileImageModifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.1f))

                        Box(contentAlignment = Alignment.Center) {
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
                            
                            // Online Status Ring
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.5f) else colorScheme.error.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = userName,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) stringResource(R.string.online) else stringResource(R.string.offline),
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Modern Search Bar
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    alpha = 0.08f
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.find_chats_placeholder),
                                color = textColor.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.clear),
                                        tint = textColor.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorScheme.primary
                        ),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = textColor)
                    )
                }

                if (chatHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) stringResource(R.string.recent_chats) else stringResource(R.string.search_results),
                        color = sectionHeaderColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatHistory.take(8)) { chat ->
                            QuickChatChip(chat, textColor, onChatClick)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item { DrawerSectionHeader(stringResource(R.string.general), sectionHeaderColor) }
                    items(generalItems) { item ->
                        ModernDrawerItem(item, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader(stringResource(R.string.account), sectionHeaderColor) }
                    items(accountItems) { item ->
                        ModernDrawerItem(item, onItemClick)
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { DrawerSectionHeader(stringResource(R.string.support), sectionHeaderColor) }
                    items(supportItems) { item ->
                        ModernDrawerItem(item, onItemClick)
                    }
                }

                // AI Credits Card at the Bottom
                GlassCard(
                    onClick = { onItemClick("credits") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    containerColor = colorScheme.primary.copy(alpha = 0.1f),
                    borderAlpha = 0.3f
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(colorScheme.primary, colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.ai_credits),
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.boost_your_credits),
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.3f)
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
    val colorScheme = MaterialTheme.colorScheme
    GlassCard(
        onClick = { onClick(chat.session) },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.widthIn(max = 130.dp),
        containerColor = colorScheme.surfaceVariant.copy(alpha = 0.2f),
        borderAlpha = 0.1f
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chat.session.title,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DrawerSectionHeader(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 12.dp)
    )
}

@Composable
fun ModernDrawerItem(
    item: NavItem,
    onClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")
    
    Surface(
        onClick = { onClick(item.route) },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    tint = colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
