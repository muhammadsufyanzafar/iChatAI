package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 0, // We only ever have one user
    val name: String,
    val email: String,
    val gender: String,
    val avatarPath: String?
)
