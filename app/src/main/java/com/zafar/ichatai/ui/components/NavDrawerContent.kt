package com.zafar.ichatai.ui.components

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val contentDescription: String
)

@Composable
fun NavDrawerContent(
    isOnline: Boolean = true,
    onItemClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val drawerBg = if (isDark) Color(0xFF1A1A1A).copy(alpha = 0.92f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val textColor = if (isDark) Color.White else Color.Black
    val sectionHeaderColor = if (isDark) Color.Gray else Color.DarkGray

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
                Surface(
                    color = textColor.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search icon",
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Find previous chats...",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
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
                        onClick = {},
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
