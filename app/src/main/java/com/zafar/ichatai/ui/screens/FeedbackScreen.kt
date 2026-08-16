package com.zafar.ichatai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.FeedbackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit = {},
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    LaunchedEffect(uiState.submitSuccess) {
        uiState.submitSuccess?.let { success ->
            if (success) {
                Toast.makeText(context, context.getString(R.string.feedback_success), Toast.LENGTH_LONG).show()
                onBackClick()
            } else {
                Toast.makeText(context, context.getString(R.string.feedback_fail), Toast.LENGTH_LONG).show()
            }
            viewModel.resetSubmissionStatus()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            uris.forEach { viewModel.attachScreenshot(it) }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                launcher.launch("image/*")
            } else {
                Toast.makeText(context, context.getString(R.string.gallery_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    )

    val checkAndLaunchGallery = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            launcher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.send_feedback),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
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
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            bottomBar = {
                FeedbackBottomBar(
                    isSubmitting = uiState.isSubmitting,
                    enabled = uiState.description.isNotBlank(),
                    onSubmit = { viewModel.submitFeedback() }
                )
            }
        ) { paddingValues: PaddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FeedbackHeader()

                CategorySection(
                    categories = viewModel.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )

                DescriptionSection(
                    description = uiState.description,
                    onDescriptionChanged = { viewModel.onDescriptionChanged(it) }
                )

                ScreenshotSection(
                    screenshots = uiState.attachedScreenshots,
                    onRemoveScreenshot = { viewModel.removeScreenshot(it) },
                    onAddMoreClick = { checkAndLaunchGallery() }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FeedbackBottomBar(
    isSubmitting: Boolean,
    enabled: Boolean,
    onSubmit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled && !isSubmitting
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSubmitting) stringResource(R.string.submitting) else stringResource(R.string.submit_feedback),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FeedbackHeader() {
    val colorScheme = MaterialTheme.colorScheme
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        alpha = 0.4f
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.help_improve_ichatai),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.feedback_header_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.category_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            categories.forEach { category ->
                item(key = category) {
                    val isSelected = selectedCategory == category
                    val icon = when (category) {
                        "General Feedback" -> Icons.AutoMirrored.Outlined.Message
                        "Bug / Crash Report" -> Icons.Outlined.BugReport
                        "Feature Request" -> Icons.Outlined.AutoAwesome
                        "AI Model Accuracy / Hallucination" -> Icons.Outlined.Psychology
                        "UI / Design Suggestion" -> Icons.Outlined.Palette
                        "Credit & Billing Inquiry" -> Icons.Outlined.AccountBalanceWallet
                        else -> Icons.Outlined.Info
                    }

                        val translatedCategory = when (category) {
                            "General Feedback" -> stringResource(R.string.feedback_general)
                            "Bug / Crash Report" -> stringResource(R.string.feedback_bug)
                            "Feature Request" -> stringResource(R.string.feedback_feature)
                            "AI Model Accuracy / Hallucination" -> stringResource(R.string.feedback_accuracy)
                            "UI / Design Suggestion" -> stringResource(R.string.feedback_ui)
                            "Credit & Billing Inquiry" -> stringResource(R.string.feedback_billing)
                            else -> category
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(category) },
                            label = { Text(text = translatedCategory) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            labelColor = colorScheme.onSurface.copy(alpha = 0.7f),
                            iconColor = colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = colorScheme.onSurface.copy(alpha = 0.1f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DescriptionSection(
    description: String,
    onDescriptionChanged: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.description_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.required),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.feedback_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.1f),
                focusedBorderColor = colorScheme.primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ScreenshotSection(
    screenshots: List<Uri>,
    onRemoveScreenshot: (Uri) -> Unit,
    onAddMoreClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.screenshots_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Text(
                text = "${screenshots.size}/5",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        if (screenshots.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                screenshots.forEach { uri ->
                    item {
                        ScreenshotItem(
                            uri = uri,
                            onRemove = { onRemoveScreenshot(uri) }
                        )
                    }
                }
                if (screenshots.size < 5) {
                    item {
                        AddMoreScreenshotsButton(onClick = onAddMoreClick)
                    }
                }
            }
        } else {
            DashedAttachmentBox(onClick = onAddMoreClick)
        }
    }
}

@Composable
fun ScreenshotItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .padding(4.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.delete),
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun DashedAttachmentBox(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .drawDashedBorder(
                color = colorScheme.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(color = colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.tap_to_attach),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AddMoreScreenshotsButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(100.dp)
            .drawDashedBorder(
                color = colorScheme.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(color = colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = stringResource(R.string.add_more),
            tint = colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

fun Modifier.drawDashedBorder(
    color: Color,
    shape: RoundedCornerShape,
    strokeWidth: Float = 2f,
    dashLength: Float = 10f,
    gapLength: Float = 10f
) = this.drawWithContent {
    drawContent()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)

    // Approximate corner radius
    val density = this
    val cornerRadiusPx = shape.topStart.toPx(size, density)

    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth, pathEffect = pathEffect),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
    )
}
