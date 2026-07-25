package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_prompts",
    foreignKeys = [
        ForeignKey(
            entity = PromptFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class SavedPromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long? = null,
    val title: String,
    val content: String,
    val tag: String? = null,
    val lastUsed: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
