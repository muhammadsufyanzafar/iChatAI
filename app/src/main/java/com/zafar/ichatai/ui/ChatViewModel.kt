package com.zafar.ichatai.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.network.AiClient
import com.zafar.ichatai.utils.NetworkObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    var inputText = mutableStateOf("")
        private set

    var isTyping = mutableStateOf(false)
        private set

    var selectedImageUri = mutableStateOf<Uri?>(null)
        private set

    val messages = mutableStateListOf<ChatMessage>()

    private val networkObserver = NetworkObserver(application)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        messages.add(ChatMessage("assistant", "New chat started. Ask me anything!"))
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkObserver.observe.collect { status ->
                _isOnline.value = status == NetworkObserver.Status.Available
            }
        }
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
