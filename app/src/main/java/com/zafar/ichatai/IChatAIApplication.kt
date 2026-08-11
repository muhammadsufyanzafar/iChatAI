package com.zafar.ichatai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.utils.DeviceInfoCollector
import com.zafar.ichatai.utils.NotificationHelper
import com.zafar.ichatai.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class IChatAIApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        // Initialize session timer
        val startTime = SessionManager.sessionStartTime

        // Setup Crashlytics with initial telemetry
        setupCrashlytics()

        // Apply theme mode
        applyThemeMode()

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)
    }

    private fun applyThemeMode() {
        val modeName = userPreferences.getThemeMode()
        val nightMode = when (modeName) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun setupCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        val telemetry = DeviceInfoCollector.getCrashlyticsKeys(this, userPreferences)
        
        telemetry.forEach { (key, value) ->
            when (value) {
                is String -> crashlytics.setCustomKey(key, value)
                is Int -> crashlytics.setCustomKey(key, value)
                is Boolean -> crashlytics.setCustomKey(key, value)
                is Float -> crashlytics.setCustomKey(key, value)
                is Double -> crashlytics.setCustomKey(key, value.toFloat())
            }
        }
        
        crashlytics.setUserId(userPreferences.getUserEmail().ifBlank { "Anonymous_${userPreferences.getUserName()}" })
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
