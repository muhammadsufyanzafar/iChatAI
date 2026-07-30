package com.zafar.ichatai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.zafar.ichatai.ui.screens.AccountDetailsScreen
import com.zafar.ichatai.ui.screens.ChatHistoryScreen
import com.zafar.ichatai.ui.screens.CheckInScreen
import com.zafar.ichatai.ui.screens.CloudSyncScreen
import com.zafar.ichatai.ui.screens.CreditsScreen
import com.zafar.ichatai.ui.screens.FavoriteChatScreen
import com.zafar.ichatai.ui.screens.MainScreen
import com.zafar.ichatai.ui.screens.NotificationSettingsScreen
import com.zafar.ichatai.ui.screens.QuietHoursScreen
import com.zafar.ichatai.ui.screens.SavedPromptsScreen
import com.zafar.ichatai.ui.screens.SettingsScreen
import com.zafar.ichatai.ui.screens.SplashScreen
import com.zafar.ichatai.ui.screens.StorageManagementScreen
import com.zafar.ichatai.ui.screens.SubscriptionScreen
import com.zafar.ichatai.ui.theme.IChatAITheme
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.viewmodel.CloudSyncViewModel
import com.zafar.ichatai.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        enableEdgeToEdge()
        setContent {
            IChatAITheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Create ViewModels at this level so they're shared between screens
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val cloudSyncViewModel: CloudSyncViewModel = hiltViewModel()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Handle result if needed
        }
    )

    LaunchedEffect(Unit) {
        if (userViewModel.isFirstRun()) {
            delay(1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            userViewModel.setFirstRunComplete()
        }
    }
    
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToMain = {
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(
                viewModel = chatViewModel,
                userViewModel = userViewModel,
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToFavorites = {
                    navController.navigate("favorites")
                },
                onNavigateToPrompts = {
                    navController.navigate("prompts")
                },
                onNavigateToSubscription = {
                    navController.navigate("subscription")
                },
                onNavigateToCredits = {
                    navController.navigate("credits")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToAccount = {
                    navController.navigate("account")
                }
            )
        }
        composable("history") {
            ChatHistoryScreen(
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onChatClick = { sessionId ->
                    chatViewModel.loadChat(sessionId)
                    navController.popBackStack()
                }
            )
        }
        composable("favorites") {
            FavoriteChatScreen(
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onChatClick = { sessionId ->
                    chatViewModel.loadChat(sessionId)
                    navController.popBackStack()
                }
            )
        }
        composable("prompts") {
            SavedPromptsScreen(
                onBackClick = { navController.popBackStack() },
                onPromptClick = { content: String ->
                    chatViewModel.onInputChange(content)
                    navController.popBackStack()
                }
            )
        }
        composable("subscription") {
            SubscriptionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("credits") {
            CreditsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCheckIn = {
                    navController.navigate("checkin")
                }
            )
        }
        composable("checkin") {
            CheckInScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSubscription = { navController.navigate("subscription") },
                onNavigateToAccount = { navController.navigate("account") },
                onNavigateToNotifications = { navController.navigate("notification_settings") },
                onNavigateToStorage = { navController.navigate("storage_management") },
                onNavigateToCloudSync = { navController.navigate("cloud_sync") }
            )
        }
        composable("cloud_sync") {
            CloudSyncScreen(
                viewModel = cloudSyncViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("storage_management") {
            StorageManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("notification_settings") {
            NotificationSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToQuietHours = { navController.navigate("quiet_hours") }
            )
        }
        composable("quiet_hours") {
            QuietHoursScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("account") {
            AccountDetailsScreen(
                viewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
