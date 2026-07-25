package com.zafar.ichatai.data.local.entity

import androidx.room.Embedded

data class PromptFolderWithCount(
    @Embedded val folder: PromptFolderEntity,
    val promptCount: Int
)
