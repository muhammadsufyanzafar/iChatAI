package com.zafar.ichatai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.zafar.ichatai.ui.theme.DarkBaseBackground
import com.zafar.ichatai.ui.theme.DarkPrimaryAccent
import com.zafar.ichatai.ui.theme.LightBaseBackground
import com.zafar.ichatai.ui.theme.LightPrimaryAccent

@Composable
fun GlowBackground(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) DarkBaseBackground else LightBaseBackground
    val glowColor = if (isDark) DarkPrimaryAccent else LightPrimaryAccent
    val glowAlpha = if (isDark) 0.15f else 0.1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Gradient Background Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}
