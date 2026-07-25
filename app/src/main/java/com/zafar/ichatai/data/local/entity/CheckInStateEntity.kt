package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in_state")
data class CheckInStateEntity(
    @PrimaryKey val id: Int = 1,
    val lastCheckInMillis: Long = 0,
    val currentStreak: Int = 0 // 0 to 7
)
