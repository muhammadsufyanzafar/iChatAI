package com.zafar.ichatai.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.zafar.ichatai.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(userPreferences.getSelectedLanguage())
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isTranslateEnabled = MutableStateFlow(userPreferences.isTranslateEnabled())
    val isTranslateEnabled: StateFlow<Boolean> = _isTranslateEnabled.asStateFlow()

    fun setLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
        userPreferences.saveSelectedLanguage(languageCode)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun setTranslateEnabled(enabled: Boolean) {
        _isTranslateEnabled.value = enabled
        userPreferences.setTranslateEnabled(enabled)
    }

    fun resetToDefaults() {
        setLanguage("en")
        setTranslateEnabled(false)
    }
}
