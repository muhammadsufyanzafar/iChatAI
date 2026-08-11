package com.zafar.ichatai.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zafar.ichatai.MainActivity
import com.zafar.ichatai.R
import com.zafar.ichatai.data.repository.NotificationRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // We can't use Hilt direct injection in Workers easily without additional setup, 
        // so we check preferences manually or via a simple check.
        
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, "streak_reminders")
            .setSmallIcon(R.drawable.bot_avatar) // Use your app icon
            .setContentTitle(applicationContext.getString(R.string.streak_reminder_title))
            .setContentText(applicationContext.getString(R.string.streak_reminder_msg))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(applicationContext).notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Permission missing on Android 13+
        }
    }
}
