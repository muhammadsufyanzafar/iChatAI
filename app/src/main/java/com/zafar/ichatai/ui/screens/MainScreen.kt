package com.zafar.ichatai.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.blur
import android.os.Build
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.zafar.ichatai.R
import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.ui.components.NavDrawerContent
import com.zafar.ichatai.ui.theme.IChatAITheme
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.File
import java.io.FileOutputStream

@Composable
fun MainScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText
    val isTyping by viewModel.isTyping
    val selectedImageUri by viewModel.selectedImageUri
    val isOnline by viewModel.isOnline.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val blurRadius = if (drawerState.currentValue == DrawerValue.Open || 
        (drawerState.isAnimationRunning && drawerState.targetValue == DrawerValue.Open)) 12.dp else 0.dp

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            showExitDialog = true
        }
    }

    // Close keyboard when drawer opens
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            keyboardController?.hide()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
        showAttachmentMenu = false
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "File selected: $uri", Toast.LENGTH_SHORT).show()
        }
        showAttachmentMenu = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Save bitmap to a temp file and get URI
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(tempFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            viewModel.onImageSelected(Uri.fromFile(tempFile))
        }
        showAttachmentMenu = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Exit Application",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to leave iChatAI? Your current session is automatically saved.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { (context as? ComponentActivity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Exit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                isOnline = isOnline,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                chatHistory = chatHistory,
                onChatClick = { chat ->
                    scope.launch {
                        viewModel.loadChat(chat.id)
                        drawerState.close()
                    }
                },
                onItemClick = { route ->
                    if (route == "history") {
                        scope.launch {
                            viewModel.saveCurrentSessionSuspend()
                            onNavigateToHistory()
                            drawerState.close()
                        }
                    } else if (route == "favorites") {
                        scope.launch {
                            viewModel.saveCurrentSessionSuspend()
                            onNavigateToFavorites()
                            drawerState.close()
                        }
                    }
                },
                onLogoutClick = { /* No logic implemented yet */ }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = Modifier
                .imePadding()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(blurRadius)
                    } else Modifier
                ),
            topBar = {
                TopBar(
                    onNewChatClick = { viewModel.createNewChat() },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                Box {
                    BottomSection(
                        inputText = inputText,
                        selectedImageUri = selectedImageUri,
                        onValueChange = { viewModel.onInputChange(it) },
                        onSendClick = { viewModel.sendMessage(context) },
                        onPromptClick = { viewModel.sendPrompt(context, it) },
                        onAddClick = { showAttachmentMenu = !showAttachmentMenu },
                        onRemoveImage = { viewModel.removeSelectedImage() },
                        isTyping = isTyping
                    )

                    if (showAttachmentMenu) {
                        Popup(
                            alignment = Alignment.BottomStart,
                            offset = IntOffset(16, -260),
                            onDismissRequest = { showAttachmentMenu = false }
                        ) {
                            AttachmentMenu(
                                onCameraClick = {
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                onGalleryClick = { galleryLauncher.launch("image/*") },
                                onFilesClick = { fileLauncher.launch("*/*") }
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ChatList(
                    messages = messages,
                    isTyping = isTyping,
                    listState = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TopBar(onNewChatClick: () -> Unit, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar_user_male),
            contentDescription = "User Profile",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onMenuClick() },
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "25 Credits",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        IconButton(
            onClick = onNewChatClick,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChatList(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(messages) { message ->
            if (message.role == "user") {
                UserMessage(message)
            } else {
                AiMessage(message.content)
            }
        }
        if (isTyping) {
            item {
                AiMessage("Thinking...")
            }
        }
    }
}

@Composable
fun AiMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.bot_avatar),
            contentDescription = "AI Assistant",
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 0.5.dp
        ) {
            MarkdownText(
                markdown = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                isTextSelectable = true
            )
        }
    }
}

@Composable
fun UserMessage(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 0.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (message.content.isNotBlank()) {
                    MarkdownText(
                        markdown = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        isTextSelectable = true
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSection(
    inputText: String,
    selectedImageUri: Uri?,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onPromptClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onRemoveImage: () -> Unit,
    isTyping: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quick Prompt Chips",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { PromptChip("Write a first draft", hasIcon = false, onClick = { onPromptClick("Write a short blog post introduction about the benefits of time blocking for remote workers.") }) }
                item { PromptChip("Brainstorm ideas", hasIcon = false, onClick = { onPromptClick("Give me 5 unique gift ideas for a coffee enthusiast who already owns standard brewing gear.") }) }
                item { PromptChip("Break the ice", hasIcon = false, onClick = { onPromptClick("Give me three fun, low-pressure conversation starters to use during a virtual team-building meeting.") }) }
                item { PromptChip("Fix a clunky sentence", hasIcon = false, onClick = { onPromptClick("Rewrite the sentence which i will provide to you to make it sound more professional and engaging") }) }
                item { PromptChip("Write code", hasIcon = false, onClick = { onPromptClick("Write a simple Python script to read a CSV file and print out the top 5 rows.") }) }
                item { PromptChip("Build a playlist", hasIcon = false, onClick = { onPromptClick("Create a 10-song upbeat indie-pop playlist designed to keep energy high while coding or studying.") }) }
                item { PromptChip("Take a quiz", hasIcon = false, onClick = { onPromptClick("Quiz me on basic geography with 5 multiple-choice questions, and give me my score at the end.") }) }
                item { PromptChip("Plan a project", hasIcon = false, onClick = { onPromptClick("Outline a 4-week study plan for learning the basics of data analysis from scratch.") }) }
            }

            // Image Preview Area
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attachments",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            "Ask anything...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = onSendClick,
                            enabled = (inputText.isNotBlank() || selectedImageUri != null) && !isTyping,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if ((inputText.isNotBlank() || selectedImageUri != null) && !isTyping)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = if ((inputText.isNotBlank() || selectedImageUri != null) && !isTyping)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun AttachmentMenu(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFilesClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        modifier = Modifier
            .width(200.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            AttachmentItem(Icons.Default.CameraAlt, "Camera", onCameraClick)
            AttachmentItem(Icons.Default.Image, "Gallery", onGalleryClick)
            AttachmentItem(Icons.AutoMirrored.Filled.InsertDriveFile, "Files", onFilesClick)
        }
    }
}

@Composable
fun AttachmentItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PromptChip(text: String, hasIcon: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasIcon) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color(0xF2FFC107), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}
