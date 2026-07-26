package com.zafar.ichatai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.zafar.ichatai.ui.screens.AccountDetailsScreen
import com.zafar.ichatai.ui.screens.ChatHistoryScreen
import com.zafar.ichatai.ui.screens.CheckInScreen
import com.zafar.ichatai.ui.screens.CreditsScreen
import com.zafar.ichatai.ui.screens.FavoriteChatScreen
import com.zafar.ichatai.ui.screens.MainScreen
import com.zafar.ichatai.ui.screens.SavedPromptsScreen
import com.zafar.ichatai.ui.screens.SettingsScreen
import com.zafar.ichatai.ui.screens.SplashScreen
import com.zafar.ichatai.ui.screens.SubscriptionScreen
import com.zafar.ichatai.ui.theme.IChatAITheme
import com.zafar.ichatai.viewmodel.ChatViewModel
import com.zafar.ichatai.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                onNavigateToAccount = { navController.navigate("account") }
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
