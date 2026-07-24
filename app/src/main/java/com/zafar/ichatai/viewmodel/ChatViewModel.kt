package com.zafar.ichatai.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import com.zafar.ichatai.data.repository.ChatRepository
import com.zafar.ichatai.network.AiClient
import com.zafar.ichatai.utils.NetworkObserver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository
    // A separate scope for persistence to ensure it survives viewModelScope cancellation
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())
    }

    var inputText = mutableStateOf("")
        private set

    var isTyping = mutableStateOf(false)
        private set

    var selectedImageUri = mutableStateOf<Uri?>(null)
        private set

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("assistant", "New chat started. Ask me anything!"))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteSearchQuery = MutableStateFlow("")
    val favoriteSearchQuery: StateFlow<String> = _favoriteSearchQuery.asStateFlow()

    val chatHistory: StateFlow<List<ChatSessionWithCount>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteHistory: StateFlow<List<ChatSessionWithCount>> = _favoriteSearchQuery
        .flatMapLatest { query ->
            repository.searchFavoriteSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val networkObserver = NetworkObserver(application)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFavoriteSearchQueryChange(query: String) {
        _favoriteSearchQuery.value = query
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri.value = uri
    }

    fun removeSelectedImage() {
        selectedImageUri.value = null
    }

    /**
     * Persists the current session and messages to the Room database.
     * Returns a Job so callers can wait for completion if needed.
     */
    fun saveCurrentSession(): Job {
        val messagesToSave = _messages.value
        val sessionId = _currentSessionId.value
        
        return persistenceScope.launch {
            performSave(messagesToSave, sessionId)
        }
    }

    /**
     * Suspend version of saveCurrentSession for reliable sequential execution.
     */
    suspend fun saveCurrentSessionSuspend() {
        val messagesToSave = _messages.value
        val sessionId = _currentSessionId.value
        performSave(messagesToSave, sessionId)
    }

    private suspend fun performSave(messages: List<ChatMessage>, sessionId: Long?): Long {
        // Don't save if it's just the default welcome message or empty
        if (messages.size <= 1 && messages.firstOrNull()?.role == "assistant") {
            return sessionId ?: -1L
        }

        return withContext(Dispatchers.IO) {
            try {
                // Ensure the block is not cancelled by process death if it already started
                withContext(NonCancellable) {
                    val id = sessionId ?: repository.createNewSession(
                        messages.firstOrNull { it.role == "user" }?.content?.take(30) ?: "New Chat"
                    )
                    
                    if (sessionId == null) {
                        _currentSessionId.value = id
                    }

                    val existingSession = repository.getSessionById(id)
                    if (existingSession != null) {
                        // Update title if it's still the default and we have a user message
                        val newTitle = if (existingSession.title == "New Chat") {
                            messages.firstOrNull { it.role == "user" }?.content?.take(30) ?: "New Chat"
                        } else existingSession.title
                        
                        repository.updateSession(
                            existingSession.copy(
                                title = newTitle,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }

                    // Clear and re-insert to ensure DB state matches current session exactly
                    repository.clearMessagesForSession(id)
                    repository.saveMessages(messages.map { it.toEntity(id) })
                    id
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sessionId ?: -1L
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            saveCurrentSessionSuspend()
            // Reset UI immediately after save finishes
            _currentSessionId.value = null
            _messages.value = listOf(ChatMessage("assistant", "New chat started. Ask me anything!"))
            inputText.value = ""
            selectedImageUri.value = null
        }
    }

    fun loadChat(sessionId: Long) {
        viewModelScope.launch {
            // Save current before loading new
            saveCurrentSessionSuspend()
            
            val historicalMessages = repository.getMessagesListBySessionId(sessionId)
            _messages.value = historicalMessages.map { it.toUiModel() }
            _currentSessionId.value = sessionId
        }
    }

    fun deleteChat(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = listOf(ChatMessage("assistant", "New chat started. Ask me anything!"))
            }
        }
    }

    fun togglePinChat(sessionId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updatePinnedStatus(sessionId, !isPinned)
        }
    }

    fun toggleTopPinnedChat(sessionId: Long, isTopPinned: Boolean) {
        viewModelScope.launch {
            repository.updateTopPinnedStatus(sessionId, !isTopPinned)
        }
    }

    fun sendMessage(context: Context) {
        val text = inputText.value
        val imageUri = selectedImageUri.value
        
        if (text.isNotBlank() || imageUri != null) {
            val userMsg = ChatMessage("user", text, imageUri)
            _messages.value = _messages.value + userMsg
            inputText.value = ""
            selectedImageUri.value = null
            
            fetchAiResponse(context, text, imageUri)
        }
    }

    fun sendPrompt(context: Context, prompt: String) {
        val userMsg = ChatMessage("user", prompt)
        _messages.value = _messages.value + userMsg
        fetchAiResponse(context, prompt, null)
    }

    private fun fetchAiResponse(context: Context, query: String, imageUri: Uri?) {
        viewModelScope.launch {
            isTyping.value = true
            try {
                val imageBase64 = imageUri?.let { AiClient.uriToBase64(context, it) }
                val responseContent = AiClient.getResponse(query, imageBase64)
                val assistantMsg = ChatMessage("assistant", responseContent)
                _messages.value = _messages.value + assistantMsg
                
                // Optional: Save after each AI response for better reliability 
                // but we stay "deferred" per user request.
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "The connection timed out. Please try again."
                    is UnknownHostException, is IOException -> "It looks like you're offline."
                    else -> "Something went wrong. (Error: ${e.localizedMessage ?: "Unknown"})"
                }
                val assistantMsg = ChatMessage("assistant", errorMessage)
                _messages.value = _messages.value + assistantMsg
            } finally {
                isTyping.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val messagesToSave = _messages.value
        val sessionId = _currentSessionId.value
        
        if (!(messagesToSave.size <= 1 && messagesToSave.firstOrNull()?.role == "assistant")) {
            // runBlocking is necessary here to prevent process death before write finishes
            runBlocking {
                performSave(messagesToSave, sessionId)
            }
        }
    }

    private fun ChatMessage.toEntity(sessionId: Long) = ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role,
        content = content,
        imageUri = imageUri,
        timestamp = timestamp
    )

    private fun ChatMessageEntity.toUiModel() = ChatMessage(
        id = id,
        role = role,
        content = content,
        imageUri = imageUri,
        timestamp = timestamp
    )
}
