package com.zafar.ichatai.ui.theme

import androidx.compose.ui.graphics.Color
import com.zafar.ichatai.R

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class AccentColor(val color: Color, val darkColor: Color, val displayNameRes: Int) {
    PURPLE(LightPrimaryAccent, DarkPrimaryAccent, R.string.purple),
    BLUE(Color(0xFF007AFF), Color(0xFF0A84FF), R.string.blue),
    GREEN(Color(0xFF34C759), Color(0xFF30D158), R.string.green),
    YELLOW(Color(0xFFFFCC00), Color(0xFFFFD60A), R.string.yellow),
    RED(Color(0xFFFF3B30), Color(0xFFFF453A), R.string.red),
    PINK(Color(0xFFFF2D55), Color(0xFFFF375F), R.string.pink)
}
