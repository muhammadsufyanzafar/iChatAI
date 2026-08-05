package com.zafar.ichatai.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.zafar.ichatai.BuildConfig
import com.zafar.ichatai.data.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.ByteArrayOutputStream

/**
 * Requirement 3 (Interface): Repository/Network Call structure
 */
interface OpenRouterService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") token: String,
        @Header("HTTP-Referer") referer: String = "com.zafar.ichatai",
        @Header("X-Title") title: String = "iChatAI",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

object AiClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: OpenRouterService = retrofit.create(OpenRouterService::class.java)

    /**
     * Requirement 3 (Implementation): Suspend function for network call
     */
    suspend fun getResponse(
        query: String, 
        imageBase64DataUrl: String? = null, 
        targetLanguage: String? = null,
        modelId: String = "google/gemini-pro-1.5-exp-0827",
        apiKey: String = BuildConfig.OPENROUTER_API_KEY,
        temperature: Float = 0.7f
    ): String {
        val contents = mutableListOf<ContentBlock>()
        
        var finalQuery = if (query.isBlank() && imageBase64DataUrl != null) "Describe this image" else query

        // Add translation instruction if targetLanguage is provided
        if (targetLanguage != null && targetLanguage != "en") {
            val languageName = getLanguageName(targetLanguage)
            finalQuery = "$finalQuery\n\n(IMPORTANT: Please provide your entire response in $languageName language only.)"
        }

        // Add text block
        contents.add(ContentBlock(
            type = "text", 
            text = finalQuery
        ))
        
        // Add image block if available (Requirement 2 structure)
        imageBase64DataUrl?.let {
            contents.add(ContentBlock(
                type = "image_url", 
                imageUrl = ImageUrl(url = it)
            ))
        }

        val request = OpenRouterRequest(
            model = modelId,
            messages = listOf(ApiMessage(role = "user", content = contents)),
            temperature = temperature
        )

        val cleanApiKey = apiKey.trim().removePrefix("Bearer ").removePrefix("bearer ")

        val response = service.getChatCompletion(
            token = "Bearer $cleanApiKey",
            referer = "com.zafar.ichatai",
            title = "iChatAI",
            request = request
        )
        Log.d("AiClient", "Response received for model $modelId")
        return response.choices.firstOrNull()?.message?.content ?: "No response from AI."
    }

    private fun getLanguageName(code: String): String {
        return when(code) {
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "zh" -> "Simplified Chinese"
            "it" -> "Italian"
            "ur" -> "Urdu"
            "hi" -> "Hindi"
            "ar" -> "Arabic"
            "pt" -> "Portuguese"
            else -> "English"
        }
    }

    /**
     * Requirement 1: File Selection & Base64 Conversion to Data URL
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            // Resize for optimal API performance
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            
            // Returns the formatted Data URL
            "data:image/jpeg;base64,$base64"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
