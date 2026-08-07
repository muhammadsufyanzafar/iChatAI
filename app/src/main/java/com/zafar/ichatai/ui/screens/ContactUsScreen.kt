package com.zafar.ichatai.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onBackClick: () -> Unit = {},
    onNavigateToFAQ: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.contact_us),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
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
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
            ) {
                // Get in Touch Section
                item {
                    ContactSection(title = stringResource(R.string.get_in_touch)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Live Chat
                                ContactMethodItem(
                                    icon = Icons.Outlined.ChatBubbleOutline,
                                    title = stringResource(R.string.chat_whatsapp),
                                    subtitle = stringResource(R.string.chat_whatsapp_desc),
                                    actionLabel = stringResource(R.string.start_chat),
                                    isButton = true,
                                    onActionClick = {
                                        startWhatsAppChat(context)
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = colorScheme.onSurface.copy(alpha = 0.08f)
                                )

                                // Email Support
                                ContactMethodItem(
                                    icon = Icons.Outlined.Email,
                                    title = stringResource(R.string.email_support),
                                    subtitle = stringResource(R.string.email_support_desc),
                                    actionLabel = stringResource(R.string.compose_email),
                                    onActionClick = {
                                        sendEmail(context)
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = colorScheme.onSurface.copy(alpha = 0.08f)
                                )

                                // Send Feedback
                                ContactMethodItem(
                                    icon = Icons.Outlined.Feedback,
                                    title = stringResource(R.string.feedback),
                                    subtitle = stringResource(R.string.send_feedback_desc),
                                    actionLabel = stringResource(R.string.feedback),
                                    onActionClick = onNavigateToFeedback
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = colorScheme.onSurface.copy(alpha = 0.08f)
                                )

                                // Official Website
                                ContactMethodItem(
                                    icon = Icons.Outlined.Language,
                                    title = stringResource(R.string.official_website),
                                    subtitle = stringResource(R.string.visit_site_desc),
                                    actionLabel = stringResource(R.string.visit_site),
                                    onActionClick = {
                                        openUrl(context, "https://ichatai-website.is-cool.dev/")
                                    }
                                )
                            }
                        }
                    }
                }

                // Follow Us Section
                item {
                    ContactSection(title = stringResource(R.string.follow_us)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Social Icons
                                SocialIconButton(
                                    iconRes = R.drawable.twitter,
                                    hasWhiteBackground = true,
                                    onClick = { openUrl(context, "https://x.com/m_sufyan_zafar") }
                                )
                                SocialIconButton(
                                    iconRes = R.drawable.linkedin,
                                    onClick = { openUrl(context, "https://www.linkedin.com/in/muhammad-sufyan-zafar-pk/") }
                                )
                                SocialIconButton(
                                    iconRes = R.drawable.instagram,
                                    onClick = { openUrl(context, "https://www.instagram.com/muhammadsufyanzafar/") }
                                )
                                SocialIconButton(
                                    iconRes = R.drawable.github,
                                    hasWhiteBackground = true,
                                    onClick = { openUrl(context, "https://github.com/muhammadsufyanzafar") }
                                )
                            }
                        }
                    }
                }

                // Footer FAQ link
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.faq_full),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { onNavigateToFAQ() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        content()
    }
}

@Composable
fun ContactMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    isButton: Boolean = false,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        if (isButton) {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
fun SocialIconButton(
    iconRes: Int,
    hasWhiteBackground: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .then(
                if (hasWhiteBackground) {
                    Modifier.background(Color.White, shape = CircleShape)
                } else {
                    Modifier
                }
            )
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}

private fun startWhatsAppChat(context: Context) {
    try {
        val phoneNumber = "+923445054799" // Placeholder: Use developer's actual WhatsApp number
        val message = "Hello, I need support with iChatAI."
        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
    }
}

private fun sendEmail(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:sufyan.pk444@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "iChatAI Support Request")
            putExtra(Intent.EXTRA_TEXT, "Hello iChatAI Support Team,\n\n")
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compose_email)))
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
    }
}


private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.unable_open_link), Toast.LENGTH_SHORT).show()
    }
}
