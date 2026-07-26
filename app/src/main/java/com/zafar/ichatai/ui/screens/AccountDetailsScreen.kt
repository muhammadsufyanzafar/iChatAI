package com.zafar.ichatai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.UserViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountDetailsScreen(
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showFullScreenAvatar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatarUri(it.toString()) }
        showAvatarSheet = false
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Gallery permission is required to select a photo", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            viewModel.updateAvatarUri(Uri.fromFile(file).toString())
        }
        showAvatarSheet = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Full Screen Avatar View
    if (showFullScreenAvatar) {
        Dialog(
            onDismissRequest = { showFullScreenAvatar = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullScreenAvatar = false },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    if (avatarUri!!.startsWith("res:")) {
                        val resId = when (avatarUri) {
                            "res:avatar_user_male" -> R.drawable.avatar_user_male
                            "res:avatar_user_female" -> R.drawable.avatar_user_female
                            else -> R.drawable.avatar_default
                        }
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    val avatarRes = when (gender) {
                        "Female" -> R.drawable.avatar_user_female
                        "Male" -> R.drawable.avatar_user_male
                        else -> R.drawable.avatar_default
                    }
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
                IconButton(
                    onClick = { showFullScreenAvatar = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .statusBarsPadding()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    // Avatar Bottom Sheet
    if (showAvatarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(32.dp),
                alpha = 0.95f, // Much higher opacity for visibility
                borderAlpha = 0.2f
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAvatarSheet = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                        }
                        Text("Profile Photo", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(
                            onClick = {
                                viewModel.updateAvatarUri(null)
                                showAvatarSheet = false
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Built-in Avatars", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val builtInAvatars = listOf(
                            R.drawable.avatar_user_male to "res:avatar_user_male",
                            R.drawable.avatar_user_female to "res:avatar_user_female",
                            R.drawable.avatar_default to "res:avatar_default"
                        )
                        builtInAvatars.forEach { (resId, uri) ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.updateAvatarUri(uri)
                                        showAvatarSheet = false
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text("Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AvatarOption(Icons.Default.PhotoLibrary, "Gallery") {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            
                            val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                galleryLauncher.launch("image/*")
                            } else {
                                galleryPermissionLauncher.launch(permission)
                            }
                        }
                        AvatarOption(Icons.Default.CameraAlt, "Camera") {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Professional Dialogs
    if (showEditNameDialog) {
        ProfessionalEditDialog(
            title = "Edit Name",
            initialValue = userName,
            onDismiss = { showEditNameDialog = false },
            onSave = { viewModel.updateUserName(it) },
            maxLength = 30,
            icon = Icons.Default.Person
        )
    }

    if (showEditEmailDialog) {
        ProfessionalEditDialog(
            title = "Edit Email",
            initialValue = userEmail,
            onDismiss = { showEditEmailDialog = false },
            onSave = { viewModel.updateUserEmail(it) },
            maxLength = 50,
            icon = Icons.Default.Email,
            isEmail = true
        )
    }

    if (showGenderDialog) {
        ProfessionalGenderDialog(
            currentGender = gender,
            onDismiss = { showGenderDialog = false },
            onSave = { viewModel.updateGender(it) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete your account? This will erase all your chat history and settings forever.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAccount(onAccountDeleted) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete All Data") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    GlowBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Account Details", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // Avatar Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    val avatarModifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = { showAvatarSheet = true },
                            onLongClick = { showFullScreenAvatar = true }
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)

                    if (avatarUri != null) {
                        if (avatarUri!!.startsWith("res:")) {
                            val resId = when (avatarUri) {
                                "res:avatar_user_male" -> R.drawable.avatar_user_male
                                "res:avatar_user_female" -> R.drawable.avatar_user_female
                                else -> R.drawable.avatar_default
                            }
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Avatar",
                                modifier = avatarModifier,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Avatar",
                                modifier = avatarModifier,
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        val avatarRes = when (gender) {
                            "Female" -> R.drawable.avatar_user_female
                            "Male" -> R.drawable.avatar_user_male
                            else -> R.drawable.avatar_default
                        }
                        Image(
                            painter = painterResource(id = avatarRes),
                            contentDescription = "Avatar",
                            modifier = avatarModifier,
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Surface(
                        onClick = { showAvatarSheet = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp),
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Info Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ProfileInfoItem(
                            label = "Display Name",
                            value = userName,
                            onEdit = { showEditNameDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        ProfileInfoItem(
                            label = "Email Address",
                            value = if (userEmail.isEmpty()) "Not set" else userEmail,
                            onEdit = { showEditEmailDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        ProfileInfoItem(
                            label = "Gender",
                            value = gender,
                            onEdit = { showGenderDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Delete Button
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(bottom = 32.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AvatarOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ProfessionalEditDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    maxLength: Int,
    icon: ImageVector,
    isEmail: Boolean = false
) {
    var text by remember { mutableStateOf(initialValue) }
    val isValid = if (isEmail && text.isNotEmpty()) Patterns.EMAIL_ADDRESS.matcher(text).matches() else true

    Dialog(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            alpha = 0.98f,
            borderAlpha = 0.3f
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxLength) text = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(title) },
                    singleLine = true,
                    isError = !isValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        errorCursorColor = MaterialTheme.colorScheme.error
                    ),
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (!isValid) Text("Invalid email format", color = MaterialTheme.colorScheme.error) else Spacer(Modifier.width(1.dp))
                            Text("${text.length}/$maxLength", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (isValid) { onSave(text); onDismiss() } },
                        shape = RoundedCornerShape(12.dp),
                        enabled = isValid,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) { Text("Save Changes", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun ProfessionalGenderDialog(
    currentGender: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val genders = listOf("Male", "Female", "Prefer not to say")
    Dialog(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            alpha = 0.98f,
            borderAlpha = 0.3f
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    text = "Select Gender",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(28.dp))
                
                genders.forEach { option ->
                    val selected = currentGender == option
                    Surface(
                        onClick = { onSave(option); onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
