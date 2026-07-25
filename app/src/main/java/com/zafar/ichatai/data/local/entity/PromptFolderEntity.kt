package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_folders")
data class PromptFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#4285F4",
    val timestamp: Long = System.currentTimeMillis()
)
