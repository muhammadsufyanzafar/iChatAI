package com.zafar.ichatai.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.R
import com.zafar.ichatai.model.UpdateInfo
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.UpdateUIState
import com.zafar.ichatai.viewmodel.UpdateViewModel
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
    
    val updateViewModel: UpdateViewModel = hiltViewModel()
    val updateState by updateViewModel.uiState.collectAsState()

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateUIState.UpToDate -> {
                Toast.makeText(context, context.getString(R.string.latest_version_msg), Toast.LENGTH_SHORT).show()
                updateViewModel.resetState()
            }
            is UpdateUIState.Error -> {
                Toast.makeText(context, (updateState as UpdateUIState.Error).message, Toast.LENGTH_SHORT).show()
                updateViewModel.resetState()
            }
            else -> {}
        }
    }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.about_ichatai),
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
                        GlassCard(
                            modifier = Modifier.size(120.dp),
                            containerColor = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(32.dp),
                            borderAlpha = 0.2f
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ichatai_icon),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(25.dp))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.app_name),
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
                                        text = stringResource(R.string.app_version),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.version_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = { updateViewModel.checkForUpdates() },
                                    enabled = updateState !is UpdateUIState.Checking,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    if (updateState is UpdateUIState.Checking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(stringResource(R.string.check_updates), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = stringResource(R.string.release_notes),
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
                            AboutInfoRow(label = stringResource(R.string.developer), value = stringResource(R.string.developer_name))
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = stringResource(R.string.website),
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
                                title = stringResource(R.string.support_email),
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
                                title = stringResource(R.string.privacy_policy),
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = onNavigateToPrivacy
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            AboutLinkItem(
                                title = stringResource(R.string.terms_of_service),
                                icon = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = onNavigateToTerms
                            )
                        }
                    }
                }

                // Footer
                item {
                    Text(
                        text = stringResource(R.string.copyright_format, stringResource(R.string.developer_name)),
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

    if (updateState is UpdateUIState.UpdateAvailable) {
        UpdateDialog(
            updateInfo = (updateState as UpdateUIState.UpdateAvailable).updateInfo,
            onDismiss = { updateViewModel.resetState() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = { if (!updateInfo.isForceUpdate) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !updateInfo.isForceUpdate,
            dismissOnClickOutside = !updateInfo.isForceUpdate
        ),
        modifier = Modifier.padding(24.dp),
        content = {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                alpha = 0.95f
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.update_available),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.version_format, updateInfo.latestVersionName, updateInfo.releaseDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (updateInfo.platforms.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.available_on),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = updateInfo.platforms.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.whats_new),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        updateInfo.changelog.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = colorScheme.primary)
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { openUrl(context, updateInfo.seeMoreUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.update_now), fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(18.dp))
                    }

                    if (!updateInfo.isForceUpdate) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                stringResource(R.string.later),
                                color = colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
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
                        text = stringResource(R.string.release_notes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MarkdownText(
                        markdown = stringResource(R.string.release_notes_markdown),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                openUrl(context, "https://ichatai-website.is-cool.dev/release-notes")
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.full_archival_fixes),
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
                        Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
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
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.email_body))
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compose_email)))
    } catch (_: Exception) {
        // Handle error
    }
}
