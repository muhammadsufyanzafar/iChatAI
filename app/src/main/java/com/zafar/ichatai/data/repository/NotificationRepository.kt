package com.zafar.ichatai.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.zafar.ichatai.data.local.dao.NotificationPreferencesDao
import com.zafar.ichatai.data.local.entity.NotificationPreferencesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationPreferencesDao: NotificationPreferencesDao
) {
    val preferences: Flow<NotificationPreferencesEntity?> = notificationPreferencesDao.getPreferences()

    suspend fun updatePreferences(preferences: NotificationPreferencesEntity) {
        notificationPreferencesDao.updatePreferences(preferences)
        updateFcmSubscriptions(preferences)
    }

    private fun updateFcmSubscriptions(prefs: NotificationPreferencesEntity) {
        val fcm = FirebaseMessaging.getInstance()
        
        if (prefs.allowNotifications && prefs.pushNotifications) {
            fcm.subscribeToTopic("general_notifications")
            
            if (prefs.newFeatureAnnouncements) {
                fcm.subscribeToTopic("feature_announcements")
            } else {
                fcm.unsubscribeFromTopic("feature_announcements")
            }
            
            if (prefs.dailyStreakReminder) {
                fcm.subscribeToTopic("streak_reminders")
            } else {
                fcm.unsubscribeFromTopic("streak_reminders")
            }
        } else {
            fcm.unsubscribeFromTopic("general_notifications")
            fcm.unsubscribeFromTopic("feature_announcements")
            fcm.unsubscribeFromTopic("streak_reminders")
        }
    }
    
    suspend fun getCurrentPreferences(): NotificationPreferencesEntity {
        return preferences.first() ?: NotificationPreferencesEntity()
    }
}
