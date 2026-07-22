package com.zafar.ichatai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zafar.ichatai.ui.screens.MainScreen
import com.zafar.ichatai.ui.theme.IChatAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IChatAITheme {
                MainScreen()
            }
        }
    }
}
