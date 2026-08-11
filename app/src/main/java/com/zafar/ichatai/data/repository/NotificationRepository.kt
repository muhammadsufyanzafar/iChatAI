package com.zafar.ichatai.data.repository

import android.content.Context
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessaging
import com.zafar.ichatai.data.local.dao.NotificationPreferencesDao
import com.zafar.ichatai.data.local.entity.NotificationPreferencesEntity
import com.zafar.ichatai.service.StreakReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferencesDao: NotificationPreferencesDao
) {
    private val workManager = WorkManager.getInstance(context)
    val preferences: Flow<NotificationPreferencesEntity?> = notificationPreferencesDao.getPreferences()

    suspend fun updatePreferences(preferences: NotificationPreferencesEntity) {
        notificationPreferencesDao.updatePreferences(preferences)
        updateFcmSubscriptions(preferences)
        
        if (preferences.dailyStreakReminder && preferences.allowNotifications) {
            scheduleStreakReminder()
        } else {
            cancelStreakReminder()
        }
    }

    fun scheduleStreakReminder() {
        // Schedule for 24 hours from now
        val streakRequest = OneTimeWorkRequestBuilder<StreakReminderWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .addTag("streak_reminder")
            .build()

        workManager.enqueueUniqueWork(
            "streak_reminder",
            ExistingWorkPolicy.REPLACE,
            streakRequest
        )
    }

    fun cancelStreakReminder() {
        workManager.cancelUniqueWork("streak_reminder")
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
