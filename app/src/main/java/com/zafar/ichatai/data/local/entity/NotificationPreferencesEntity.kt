package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPreferencesEntity(
    @PrimaryKey val id: Int = 0,
    val allowNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val soundAndVibration: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val newFeatureAnnouncements: Boolean = true,
    val dailyStreakReminder: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)
