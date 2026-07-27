package com.zafar.ichatai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.local.entity.NotificationPreferencesEntity
import com.zafar.ichatai.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val preferences: StateFlow<NotificationPreferencesEntity> = repository.preferences
        .map { it ?: NotificationPreferencesEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationPreferencesEntity()
        )

    fun toggleAllowNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(allowNotifications = enabled))
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(pushNotifications = enabled))
        }
    }

    fun toggleSoundAndVibration(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(soundAndVibration = enabled))
        }
    }

    fun toggleNewFeatureAnnouncements(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(newFeatureAnnouncements = enabled))
        }
    }

    fun toggleDailyStreakReminder(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(dailyStreakReminder = enabled))
        }
    }

    fun toggleQuietHours(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(quietHoursEnabled = enabled))
        }
    }

    fun updateQuietHours(start: String, end: String) {
        viewModelScope.launch {
            val current = preferences.value
            repository.updatePreferences(current.copy(quietHoursStart = start, quietHoursEnd = end))
        }
    }
}
