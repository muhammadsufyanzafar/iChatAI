package com.zafar.ichatai.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.zafar.ichatai.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object AiClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val httpClient = OkHttpClient()

    suspend fun getResponse(query: String, imageBase64: String? = null): String = suspendCancellableCoroutine { continuation ->
        try {
            val contentArray = JSONArray()

            // Add text content
            if (query.isNotBlank()) {
                contentArray.put(JSONObject().apply {
                    put("type", "text")
                    put("text", query)
                })
            } else if (imageBase64 != null) {
                // If only image, add a default prompt
                contentArray.put(JSONObject().apply {
                    put("type", "text")
                    put("text", "Please identify this image")
                })
            }

            // Add image content if available
            if (imageBase64 != null) {
                contentArray.put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", "data:image/jpeg;base64,$imageBase64")
                    })
                })
            }

            val body = JSONObject().apply {
                put("model", "openrouter/free")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", contentArray)
                    })
                })
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "com.zafar.ichatai")
                .addHeader("X-Title", "iChatAI")
                .post(body.toString().toRequestBody(JSON))
                .build()

            val call = httpClient.newCall(request)
            
            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { 
                        if (!response.isSuccessful) {
                            val errorBody = response.body?.string() ?: ""
                            continuation.resumeWithException(IOException("HTTP ${response.code}: ${response.message}\n$errorBody"))
                            return
                        }
                        
                        val respBody = response.body?.string() ?: ""
                        try {
                            val json = JSONObject(respBody)
                            val choices = json.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val choice0 = choices.getJSONObject(0)
                                val messageObj = choice0.optJSONObject("message")
                                val content = messageObj?.optString("content", "") ?: ""
                                continuation.resume(content.trim())
                            } else {
                                continuation.resumeWithException(IOException("Empty response from model."))
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            })

        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            // Resize if too large (OpenRouter/Gemini might have limits)
            val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
                val ratio = Math.min(1024f / bitmap.width, 1024f / bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
