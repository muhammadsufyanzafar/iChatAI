package com.zafar.ichatai.data.local.entity

import androidx.room.Embedded

data class ChatSessionWithCount(
    @Embedded val session: ChatSessionEntity,
    val messageCount: Int
)
