package com.zafar.ichatai.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.ui.theme.AccentColor
import com.zafar.ichatai.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(userPreferences.getThemeMode())
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(
        AccentColor.valueOf(userPreferences.getAccentColor())
    )
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        userPreferences.setThemeMode(mode.name)
        applyThemeMode(mode)
    }

    private fun applyThemeMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    init {
        applyThemeMode(_themeMode.value)
    }

    fun setAccentColor(color: AccentColor) {
        _accentColor.value = color
        userPreferences.setAccentColor(color.name)
    }

    fun resetToDefaults() {
        setThemeMode(ThemeMode.SYSTEM)
        setAccentColor(AccentColor.PURPLE)
    }
}
