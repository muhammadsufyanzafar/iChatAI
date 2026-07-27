package com.zafar.ichatai.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zafar.ichatai.MainActivity
import com.zafar.ichatai.R
import com.zafar.ichatai.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: NotificationRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        scope.launch {
            val prefs = repository.getCurrentPreferences()
            if (!prefs.allowNotifications || !prefs.pushNotifications) return@launch

            if (prefs.quietHoursEnabled) {
                if (isInQuietHours(prefs.quietHoursStart, prefs.quietHoursEnd)) {
                    return@launch
                }
            }

            // Determine if we should show based on topic/type
            val topic = remoteMessage.from?.replace("/topics/", "")
            when (topic) {
                "feature_announcements" -> if (!prefs.newFeatureAnnouncements) return@launch
                "streak_reminders" -> if (!prefs.dailyStreakReminder) return@launch
            }

            showNotification(remoteMessage)
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val channelId = "default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val scopeLaunch = CoroutineScope(Dispatchers.IO).launch {
                val prefs = repository.getCurrentPreferences()
                val importance = if (prefs.soundAndVibration) {
                    NotificationManager.IMPORTANCE_DEFAULT
                } else {
                    NotificationManager.IMPORTANCE_LOW
                }
                val channel = NotificationChannel(channelId, "General Notifications", importance).apply {
                    description = "Default notification channel"
                    if (!prefs.soundAndVibration) {
                        setSound(null, null)
                        enableVibration(false)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon or custom small icon
            .setContentTitle(remoteMessage.notification?.title ?: remoteMessage.data["title"])
            .setContentText(remoteMessage.notification?.body ?: remoteMessage.data["body"])
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun isInQuietHours(start: String, end: String): Boolean {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTime = currentHour * 60 + currentMinute

        val startParts = start.split(":")
        val startHour = startParts[0].toIntOrNull() ?: 0
        val startMinute = startParts[1].toIntOrNull() ?: 0
        val startTime = startHour * 60 + startMinute

        val endParts = end.split(":")
        val endHour = endParts[0].toIntOrNull() ?: 0
        val endMinute = endParts[1].toIntOrNull() ?: 0
        val endTime = endHour * 60 + endMinute

        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Here you would typically send the token to your server if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
