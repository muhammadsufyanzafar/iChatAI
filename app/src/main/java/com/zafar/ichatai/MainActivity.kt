package com.zafar.ichatai

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.zafar.ichatai.ui.screens.HelpAndFaqScreen
import com.zafar.ichatai.ui.screens.LanguageScreen
import com.zafar.ichatai.ui.screens.MainScreen
import com.zafar.ichatai.ui.screens.NotificationSettingsScreen
import com.zafar.ichatai.ui.screens.QuietHoursScreen
import com.zafar.ichatai.ui.screens.SavedPromptsScreen
import com.zafar.ichatai.ui.screens.SettingsScreen
import com.zafar.ichatai.ui.screens.AboutScreen
import com.zafar.ichatai.ui.screens.SplashScreen
import com.zafar.ichatai.ui.screens.StorageManagementScreen
import com.zafar.ichatai.ui.screens.SubscriptionScreen
import com.zafar.ichatai.ui.screens.TermsOfServiceScreen
import com.zafar.ichatai.ui.screens.ContactUsScreen
import com.zafar.ichatai.ui.screens.FeedbackScreen
import com.zafar.ichatai.ui.screens.AppearanceScreen
import com.zafar.ichatai.ui.screens.AIModelPreferencesScreen
import com.zafar.ichatai.ui.theme.IChatAITheme
import com.zafar.ichatai.ui.theme.ThemeMode
import com.zafar.ichatai.ui.theme.AccentColor
import com.zafar.ichatai.utils.NavigationTracker
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.viewmodel.CloudSyncViewModel
import com.zafar.ichatai.viewmodel.UserViewModel
import com.zafar.ichatai.viewmodel.AppearanceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        enableEdgeToEdge()
        setContent {
            val appearanceViewModel: AppearanceViewModel = hiltViewModel()
            val themeMode by appearanceViewModel.themeMode.collectAsState(ThemeMode.SYSTEM)
            val accentColor by appearanceViewModel.accentColor.collectAsState(AccentColor.PURPLE)

            IChatAITheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                AppNavigation(appearanceViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(appearanceViewModel: AppearanceViewModel) {
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
    
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            backStackEntry.destination.route?.let { route ->
                NavigationTracker.track(route)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
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
                },
                onNavigateToHelp = {
                    navController.navigate("help_faq")
                },
                onNavigateToFeedback = {
                    navController.navigate("send_feedback")
                },
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
            val context = LocalContext.current
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToSubscription = { navController.navigate("subscription") },
                onNavigateToAccount = { navController.navigate("account") },
                onNavigateToNotifications = { navController.navigate("notification_settings") },
                onNavigateToStorage = { navController.navigate("storage_management") },
                onNavigateToCloudSync = { navController.navigate("cloud_sync") },
                onNavigateToLanguage = { navController.navigate("language") },
                onNavigateToAppearance = { navController.navigate("appearance_settings") },
                onNavigateToAIModelPreferences = { navController.navigate("ai_model_preferences") },
                onNavigateToHelp = { navController.navigate("help_faq") },
                onNavigateToTerms = { navController.navigate("terms_privacy") },
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToContact = { navController.navigate("contact_us") },
                onNavigateToFeedback = {navController.navigate("send_feedback")}
            )
        }
        composable("contact_us") {
            ContactUsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToFAQ = {
                    navController.navigate("help_faq")
                },
                onNavigateToFeedback = {
                    navController.navigate("send_feedback")
                }
            )
        }
        composable("send_feedback") {
            FeedbackScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("about") {
            val context = LocalContext.current
            AboutScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate("terms_privacy") },
                onNavigateToPrivacy = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ichatai-website.vercel.app/privacy"))
                    context.startActivity(intent)
                }
            )
        }
        composable("terms_privacy") {
            TermsOfServiceScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("help_faq") {
            HelpAndFaqScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("cloud_sync") {
            CloudSyncScreen(
                viewModel = cloudSyncViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("language") {
            LanguageScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("appearance_settings") {
            AppearanceScreen(
                viewModel = appearanceViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("ai_model_preferences") {
            AIModelPreferencesScreen(
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
        composable("help") {
            HelpAndFaqScreen(
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
