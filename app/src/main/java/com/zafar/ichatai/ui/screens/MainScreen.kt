package com.zafar.ichatai.ui.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.zafar.ichatai.R
import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.components.NavDrawerContent
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.viewmodel.CreditsViewModel
import com.zafar.ichatai.viewmodel.UserViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun MainScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    creditsViewModel: CreditsViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToPrompts: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToCredits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText
    val isTyping by viewModel.isTyping
    val selectedImageUri by viewModel.selectedImageUri
    val isOnline by viewModel.isOnline.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val totalCredits by creditsViewModel.totalCredits.collectAsState()

    val userName by userViewModel.userName.collectAsState()
    val userAvatarUri by userViewModel.avatarUri.collectAsState()
    val userGender by userViewModel.gender.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val blurRadius = if (drawerState.currentValue == DrawerValue.Open || 
        (drawerState.isAnimationRunning && drawerState.targetValue == DrawerValue.Open)) 12.dp else 0.dp

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // New Chat when language changes
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val contextLanguage = context.resources.configuration.locales[0].language
    
    LaunchedEffect(contextLanguage) {
        if (currentLanguage != contextLanguage) {
            viewModel.updateLanguage(contextLanguage)
            if (messages.isNotEmpty()) {
                viewModel.createNewChat(context.getString(R.string.welcome_message))
            }
        }
    }

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
            Toast.makeText(context, context.getString(R.string.file_selected, uri.toString()), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, context.getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    val welcomeMsg = stringResource(R.string.welcome_message)
    LaunchedEffect(messages) {
        if (messages.isEmpty()) {
            viewModel.createNewChat(welcomeMsg)
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
                    text = stringResource(R.string.exit_app_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.exit_app_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { (context as? ComponentActivity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.exit), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
                userName = userName,
                userAvatarUri = userAvatarUri,
                userGender = userGender,
                onChatClick = { chat ->
                    scope.launch {
                        viewModel.loadChat(chat.id)
                        drawerState.close()
                    }
                },
                onItemClick = { route ->
                    when (route) {
                        "history" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToHistory()
                                drawerState.close()
                            }
                        }
                        "favorites" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToFavorites()
                                drawerState.close()
                            }
                        }
                        "prompts" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToPrompts()
                                drawerState.close()
                            }
                        }
                        "subscription" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToSubscription()
                                drawerState.close()
                            }
                        }
                        "credits" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToCredits()
                                drawerState.close()
                            }
                        }
                        "settings" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToSettings()
                                drawerState.close()
                            }
                        }
                        "account" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToAccount()
                                drawerState.close()
                            }
                        }
                        "help" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToHelp()
                                drawerState.close()
                            }
                        }
                        "feedback" -> {
                            scope.launch {
                                viewModel.saveCurrentSessionSuspend()
                                onNavigateToFeedback()
                                drawerState.close()
                            }
                        }
                    }
                },
            )
        },
        gesturesEnabled = true
    ) {
        GlowBackground {
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
                        credits = totalCredits,
                        userAvatarUri = userAvatarUri,
                        userGender = userGender,
                        onNewChatClick = { viewModel.createNewChat(welcomeMsg) },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onCreditsClick = {
                            onNavigateToCredits()
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
                containerColor = Color.Transparent
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
}

@Composable
fun TopBar(
    credits: Int,
    userAvatarUri: String?,
    userGender: String,
    onNewChatClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreditsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val avatarModifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable { onMenuClick() }

        if (userAvatarUri != null) {
            if (userAvatarUri.startsWith("res:")) {
                val resId = when (userAvatarUri) {
                    "res:avatar_user_male" -> R.drawable.avatar_user_male
                    "res:avatar_user_female" -> R.drawable.avatar_user_female
                    else -> R.drawable.avatar_default
                }
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "User Profile",
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = userAvatarUri,
                    contentDescription = "User Profile",
                    modifier = avatarModifier,
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
                contentDescription = "User Profile",
                modifier = avatarModifier,
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onCreditsClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.credits_text, credits),
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
                AiMessage(stringResource(R.string.thinking))
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
        GlassCard(
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
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
                text = stringResource(R.string.quick_prompt_chips),
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
                item { 
                    val title = stringResource(R.string.prompt_draft_title)
                    val content = stringResource(R.string.prompt_draft_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_brainstorm_title)
                    val content = stringResource(R.string.prompt_brainstorm_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_icebreaker_title)
                    val content = stringResource(R.string.prompt_icebreaker_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_fix_sentence_title)
                    val content = stringResource(R.string.prompt_fix_sentence_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_code_title)
                    val content = stringResource(R.string.prompt_code_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_playlist_title)
                    val content = stringResource(R.string.prompt_playlist_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_quiz_title)
                    val content = stringResource(R.string.prompt_quiz_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
                item { 
                    val title = stringResource(R.string.prompt_plan_title)
                    val content = stringResource(R.string.prompt_plan_content)
                    PromptChip(title, hasIcon = false, onClick = { onPromptClick(content) }) 
                }
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
                            stringResource(R.string.ask_anything),
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
    GlassCard(
        shape = RoundedCornerShape(24.dp),
        alpha = 0.9f,
        borderAlpha = 0.2f,
        modifier = Modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            AttachmentItem(Icons.Default.CameraAlt, stringResource(R.string.camera), onCameraClick)
            AttachmentItem(Icons.Default.Image, stringResource(R.string.gallery), onGalleryClick)
            AttachmentItem(Icons.AutoMirrored.Filled.InsertDriveFile, stringResource(R.string.files), onFilesClick)
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
