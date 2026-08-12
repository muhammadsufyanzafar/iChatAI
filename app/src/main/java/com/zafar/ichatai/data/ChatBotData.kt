package com.zafar.ichatai.data

import android.net.Uri
import com.google.gson.annotations.SerializedName

/**
 * Internal UI Model
 */
data class ChatMessage(
    val role: String,
    val content: String,
    val imageUris: List<Uri> = emptyList(),
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * OpenRouter / OpenAI API Models
 */
data class OpenRouterRequest(
    val model: String = "openrouter/free",
    val messages: List<ApiMessage>,
    val temperature: Float = 0.7f
)

data class ApiMessage(
    val role: String,
    val content: List<ContentBlock>
)

data class ContentBlock(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ApiMessageResponse
)

data class ApiMessageResponse(
    val role: String,
    val content: String?
)

data class AIModel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("apiKey") val apiKey: String? = null,
    @SerializedName("isRecommended") val isRecommended: Boolean = false
)
