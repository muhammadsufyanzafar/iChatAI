package com.zafar.ichatai.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zafar.ichatai.MainActivity
import com.zafar.ichatai.R

object NotificationHelper {
    const val CHANNEL_STREAK = "streak_reminders"
    const val CHANNEL_SYNC = "sync_alerts"
    const val CHANNEL_ANNOUNCEMENTS = "announcements"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_STREAK,
                    "Daily Rewards",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders to claim your daily AI credits"
                },
                NotificationChannel(
                    CHANNEL_SYNC,
                    "Cloud Sync",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Status updates for Google Drive synchronization"
                },
                NotificationChannel(
                    CHANNEL_ANNOUNCEMENTS,
                    "New Features",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "News about new AI models and app updates"
                }
            )
            
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showLowCreditsNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.low_credits_title))
            .setContentText(context.getString(R.string.low_credits_msg))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1002, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showSyncErrorNotification(context: Context, error: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Cloud Sync Failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1003, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
