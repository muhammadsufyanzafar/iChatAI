package com.zafar.ichatai.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.network.AiClient
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ChatViewModel : ViewModel() {
    var inputText = mutableStateOf("")
        private set

    var isTyping = mutableStateOf(false)
        private set

    var selectedImageUri = mutableStateOf<Uri?>(null)
        private set

    val messages = mutableStateListOf<ChatMessage>()

    init {
        messages.add(ChatMessage("assistant", "New chat started. Ask me anything!"))
    }

    fun onInputChange(newValue: String) {
        inputText.value = newValue
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri.value = uri
    }

    fun removeSelectedImage() {
        selectedImageUri.value = null
    }

    fun clearChat() {
        messages.clear()
        messages.add(ChatMessage("assistant", "New chat started. Ask me anything!"))
        inputText.value = ""
        selectedImageUri.value = null
    }

    fun sendMessage(context: Context) {
        val text = inputText.value
        val imageUri = selectedImageUri.value
        
        if (text.isNotBlank() || imageUri != null) {
            messages.add(ChatMessage("user", text, imageUri))
            inputText.value = ""
            selectedImageUri.value = null
            fetchAiResponse(context, text, imageUri)
        }
    }

    fun sendPrompt(context: Context, prompt: String) {
        messages.add(ChatMessage("user", prompt))
        fetchAiResponse(context, prompt, null)
    }

    private fun fetchAiResponse(context: Context, query: String, imageUri: Uri?) {
        viewModelScope.launch {
            isTyping.value = true
            try {
                val imageBase64 = imageUri?.let { AiClient.uriToBase64(context, it) }
                val responseContent = AiClient.getResponse(query, imageBase64)
                messages.add(ChatMessage("assistant", responseContent))
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> {
                        "The connection timed out. The AI is taking too long to respond. Please try again later."
                    }
                    is UnknownHostException, is IOException -> {
                        "It looks like you're offline. Please check your internet connection and try again."
                    }
                    else -> "Something went wrong. Please try again. (Error: ${e.localizedMessage ?: "Unknown"})"
                }
                messages.add(ChatMessage("assistant", errorMessage))
            } finally {
                isTyping.value = false
            }
        }
    }
}
