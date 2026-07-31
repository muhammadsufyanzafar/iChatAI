package com.zafar.ichatai.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showReleaseNotes by remember { mutableStateOf(false) }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "About iChatAI",
                            style = MaterialTheme.typography.titleLarge,
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
            ) {
                // App Logo and Title
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ichatai_icon),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "iChatAI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onBackground
                        )
                    }
                }

                // Info Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "App Version",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "2.0.0",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = { /* Placeholder */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Check for Updates", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = "Release Notes",
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                onClick = { showReleaseNotes = true }
                            )
                        }
                    }
                }

                // Developer & Support Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AboutInfoRow(label = "Developer", value = "Muhammad Sufyan Zafar")
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = "Website",
                                subtitle = "muhammadsufyanzafar.github.io",
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = {
                                    openUrl(context, "https://muhammadsufyanzafar.github.io/portfolio")
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = "Support Email",
                                subtitle = "sufyan.pk444@gmail.com",
                                icon = Icons.Default.Email,
                                onClick = {
                                    sendEmail(context)
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = "Privacy Policy",
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = onNavigateToPrivacy
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = "Terms of Service",
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = onNavigateToTerms
                            )
                        }
                    }
                }

                // Footer
                item {
                    Text(
                        text = "© 2026 Muhammad Sufyan Zafar. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    )
                }
            }
        }
    }

    if (showReleaseNotes) {
        ReleaseNotesDialog(onDismiss = { showReleaseNotes = false })
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun AboutLinkItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp),
        content = {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                alpha = 0.9f
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Release Notes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val releaseNotesMarkdown = """
                        ### Version 2.0.0
                        - **Advanced AI Models**: Integrated new OpenRouter free models.
                        - **Frosted Glass UI**: Complete redesign with modern aesthetics.
                        - **Local History**: Faster access to your chat archives.
                        - **Daily Rewards**: Earn credits through check-ins and ads.
                        - **Cloud Sync**: Securely backup your chats to Google Drive.
                    """.trimIndent()
                    
                    MarkdownText(
                        markdown = releaseNotesMarkdown,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                openUrl(context, "https://ichatai-website.vercel.app/release-notes")
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full Archival Bug Fixes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        // Handle error
    }
}

private fun sendEmail(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:sufyan.pk444@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "iChatAI Support Request")
            putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI need help with...")
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (_: Exception) {
        // Handle error
    }
}
