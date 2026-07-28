package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Feedback action */ }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Feedback",
                                tint = colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = colorScheme.onBackground
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
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
            ) {
                // Account Section
                item {
                    SettingsSection(title = "Account") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Person,
                                title = "User Profile",
                                subtitle = "Tapping this opens profile Name, Email, Change Avatar",
                                onClick = onNavigateToAccount
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Shield,
                                title = "Subscription",
                                subtitle = "Displays current plan, e.g., Pro Annual - Active",
                                onClick = onNavigateToSubscription
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SettingsItem(
                            modifier = Modifier.fillMaxWidth(0.475f),
                            icon = Icons.Outlined.Lock,
                            title = "Security & Privacy",
                            subtitle = "Passcode, Two-Factor Auth",
                            onClick = { /* TODO */ }
                        )
                    }
                }

                // Application Section
                item {
                    SettingsSection(title = "Application") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Palette,
                                title = "Appearance",
                                subtitle = "Light, Dark, System Default",
                                onClick = { /* TODO */ }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Translate,
                                title = "Language",
                                subtitle = "English, Spanish, French, etc.",
                                onClick = { /* TODO */ }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Notifications,
                                title = "Notifications",
                                subtitle = "Push, Sound, Vibration",
                                onClick = onNavigateToNotifications
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.AutoAwesome,
                                title = "AI Model Preferences",
                                subtitle = "Default model, creativity settings",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // Data Section
                item {
                    SettingsSection(title = "Data") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Storage,
                                title = "Data & Storage Management",
                                subtitle = "Clear cache, export history",
                                onClick = onNavigateToStorage
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.CloudQueue,
                                title = "Cloud Sync",
                                subtitle = "On/Off, Sync Now",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // Support & About Section
                item {
                    SettingsSection(title = "Support & About") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.HelpOutline,
                                title = "Help Center & FAQ",
                                onClick = { /* TODO */ }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Description,
                                title = "Terms of Service",
                                onClick = { /* TODO */ }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Info,
                                title = "About iChatAI",
                                subtitle = "App Version 2.0, Developer info",
                                onClick = { /* TODO */ }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            SettingsItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Email,
                                title = "Contact Us",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // Log Out Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Log out action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(24.dp)
                                    .padding(end = 4.dp),
                                tint = colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier.heightIn(min = 100.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 14.sp,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
