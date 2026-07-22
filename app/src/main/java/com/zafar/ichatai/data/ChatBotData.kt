package com.zafar.ichatai.data

import android.net.Uri

data class ChatMessage(
    val role: String,
    val content: String,
    val imageUri: Uri? = null
)
