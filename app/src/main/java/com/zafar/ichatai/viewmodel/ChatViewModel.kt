package com.zafar.ichatai.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
    import com.zafar.ichatai.data.ChatMessage
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import com.zafar.ichatai.data.repository.ChatRepository
import com.zafar.ichatai.data.repository.PromptRepository
import com.zafar.ichatai.network.AiClient
import com.zafar.ichatai.network.RemoteConfigManager
import com.zafar.ichatai.utils.NetworkObserver
import com.zafar.ichatai.utils.VibrationHelper
import com.zafar.ichatai.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST, ALPHABETICAL, MOST_MESSAGES
}

enum class FilterCriteria {
    ALL, TODAY, YESTERDAY, THIS_WEEK, PINNED_ONLY
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val promptRepository: PromptRepository,
    private val userPreferences: UserPreferences,
    private val networkObserver: NetworkObserver,
    private val remoteConfigManager: RemoteConfigManager,
    private val vibrationHelper: VibrationHelper
) : ViewModel() {
    
    // A separate scope for persistence to ensure it survives viewModelScope cancellation
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var inputText = mutableStateOf("")
        private set

    var isTyping = mutableStateOf(false)
        private set

    private val _selectedAttachments = MutableStateFlow<List<Uri>>(emptyList())
    val selectedAttachments: StateFlow<List<Uri>> = _selectedAttachments.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historySortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val historySortOrder: StateFlow<SortOrder> = _historySortOrder.asStateFlow()

    private val _historyFilter = MutableStateFlow(FilterCriteria.ALL)
    val historyFilter: StateFlow<FilterCriteria> = _historyFilter.asStateFlow()

    private val _favoriteSearchQuery = MutableStateFlow("")
    val favoriteSearchQuery: StateFlow<String> = _favoriteSearchQuery.asStateFlow()

    private val _favoriteSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val favoriteSortOrder: StateFlow<SortOrder> = _favoriteSortOrder.asStateFlow()

    private val _currentLanguage = MutableStateFlow(userPreferences.getSelectedLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatHistory: StateFlow<List<ChatSessionWithCount>> = combine(
        _searchQuery, _historySortOrder, _historyFilter
    ) { query, sort, filter ->
        Triple(query, sort, filter)
    }.flatMapLatest { (query, sort, filter) ->
        repository.searchSessions(query).map { list ->
            list.filter { item ->
                when (filter) {
                    FilterCriteria.ALL -> true
                    FilterCriteria.TODAY -> isToday(item.session.timestamp)
                    FilterCriteria.YESTERDAY -> isYesterday(item.session.timestamp)
                    FilterCriteria.THIS_WEEK -> isWithinLastWeek(item.session.timestamp)
                    FilterCriteria.PINNED_ONLY -> item.session.isPinned
                }
            }.sortedWith(getComparator(sort))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteHistory: StateFlow<List<ChatSessionWithCount>> = combine(
        _favoriteSearchQuery, _favoriteSortOrder
    ) { query, sort ->
        Pair(query, sort)
    }.flatMapLatest { (query, sort) ->
        repository.searchFavoriteSessions(query).map { list ->
            list.sortedWith(getComparator(sort))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun getComparator(sort: SortOrder): Comparator<ChatSessionWithCount> {
        return when (sort) {
            SortOrder.NEWEST_FIRST -> compareByDescending { it.session.timestamp }
            SortOrder.OLDEST_FIRST -> compareBy { it.session.timestamp }
            SortOrder.ALPHABETICAL -> compareBy { it.session.title.lowercase() }
            SortOrder.MOST_MESSAGES -> compareByDescending { it.messageCount }
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val year = calendar.get(java.util.Calendar.YEAR)
        calendar.timeInMillis = timestamp
        return calendar.get(java.util.Calendar.DAY_OF_YEAR) == today && 
               calendar.get(java.util.Calendar.YEAR) == year
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val year = calendar.get(java.util.Calendar.YEAR)
        calendar.timeInMillis = timestamp
        return calendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterday && 
               calendar.get(java.util.Calendar.YEAR) == year
    }

    private fun isWithinLastWeek(timestamp: Long): Boolean {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return timestamp >= weekAgo
    }

    fun onHistorySortChange(order: SortOrder) { _historySortOrder.value = order }
    fun onHistoryFilterChange(filter: FilterCriteria) { _historyFilter.value = filter }
    fun onFavoriteSortChange(order: SortOrder) { _favoriteSortOrder.value = order }

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        observeNetwork()
        viewModelScope.launch {
            remoteConfigManager.fetchAndActivate()
        }
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

    fun updateLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
    }

    fun onImageSelected(uri: Uri?) {
        if (uri != null) {
            val current = _selectedAttachments.value
            if (current.size < 3) {
                _selectedAttachments.value = current + uri
            }
        }
    }

    fun removeSelectedImage(uri: Uri) {
        _selectedAttachments.value -= uri
    }

    fun clearAttachments() {
        _selectedAttachments.value = emptyList()
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
        // Don't save if it's empty or just a system/assistant welcome message
        val hasUserMessage = messages.any { it.role == "user" }
        if (!hasUserMessage) {
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

    /**
     * Persists a single message to the Room database.
     */
    private fun saveMessageRealtime(message: ChatMessage) {
        val sessionId = _currentSessionId.value ?: return // Should not happen if logic is correct

        persistenceScope.launch {
            try {
                repository.saveMessages(listOf(message.toEntity(sessionId)))
                // Update session timestamp and potentially title
                val session = repository.getSessionById(sessionId)
                if (session != null) {
                    val newTitle = if ((session.title == "New Chat" || session.title == "New Chat started") && message.role == "user") {
                        message.content.take(30).ifBlank { "Image Chat" }
                    } else session.title
                    
                    repository.updateSession(session.copy(
                        title = newTitle,
                        timestamp = System.currentTimeMillis()
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun ensureSessionCreated(firstMessage: String): Long {
        return _currentSessionId.value ?: withContext(Dispatchers.IO) {
            val title = firstMessage.take(30).ifBlank { "New Chat" }
            val newId = repository.createNewSession(title)
            _currentSessionId.value = newId
            newId
        }
    }

    fun createNewChat() {
        // Only allow if we have a current session or messages
        if (_messages.value.isEmpty()) return

        viewModelScope.launch {
            // Save state before clearing
            saveCurrentSessionSuspend()
            
            _currentSessionId.value = null
            _messages.value = emptyList()
            inputText.value = ""
            _selectedAttachments.value = emptyList()
        }
    }

    fun loadChat(sessionId: Long) {
        viewModelScope.launch {
            if (_messages.value.isNotEmpty()) {
                saveCurrentSessionSuspend()
            }
            
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
                _messages.value = emptyList()
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
        val attachments = _selectedAttachments.value
        
        if (text.isNotBlank() || attachments.isNotEmpty()) {
            vibrationHelper.vibrateClick()
            val userMsg = ChatMessage("user", text, attachments)
            _messages.value += userMsg
            inputText.value = ""
            _selectedAttachments.value = emptyList()
            
            if (text.isNotBlank()) {
                viewModelScope.launch {
                    promptRepository.saveRecentPrompt(text)
                }
            }
            
            fetchAiResponse(context, text, attachments)
        }
    }

    fun sendPrompt(context: Context, prompt: String) {
        val userMsg = ChatMessage("user", prompt)
        _messages.value += userMsg
        
        viewModelScope.launch {
            promptRepository.saveRecentPrompt(prompt)
        }

        // Wait for successful AI response before persisting session
        fetchAiResponse(context, prompt, emptyList())
    }

    private fun fetchAiResponse(context: Context, query: String, attachments: List<Uri>) {
        // Capture the user message that triggered this response
        val userMsg = _messages.value.lastOrNull { it.role == "user" }

        viewModelScope.launch {
            isTyping.value = true
            try {
                val imageUrls = attachments.mapNotNull { AiClient.uriToBase64(context, it) }
                
                // Get translation settings
                val translateEnabled = userPreferences.isTranslateEnabled()
                val targetLang = if (translateEnabled) userPreferences.getSelectedLanguage() else null

                // Get AI model preferences
                val selectedModelId = userPreferences.getSelectedModelId()
                val temperature = userPreferences.getTemperature()
                
                val remoteModel = remoteConfigManager.getAIModels().find { it.id == selectedModelId }
                val apiKey = remoteModel?.apiKey ?: BuildConfig.OPENROUTER_API_KEY
                
                val responseContent = AiClient.getResponse(
                    query = query, 
                    imageUrls = imageUrls,
                    targetLanguage = targetLang,
                    modelId = selectedModelId,
                    apiKey = apiKey,
                    temperature = temperature
                )
                
                val assistantMsg = ChatMessage("assistant", responseContent)
                _messages.value += assistantMsg
                vibrationHelper.vibrateMessageReceived()
                
                // Real-time Persistence: Only save if AI response is successful
                viewModelScope.launch {
                    // 1. Ensure session exists
                    val sessionId = ensureSessionCreated(query)
                    
                    // 2. If this is a new session, save the triggering user message first
                    val messagesInDb = repository.getMessagesListBySessionId(sessionId)
                    if (messagesInDb.none { it.role == "user" } && userMsg != null) {
                        repository.saveMessages(listOf(userMsg.toEntity(sessionId)))
                    }
                    
                    // 3. Save the successful AI response
                    saveMessageRealtime(assistantMsg)
                }
                
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "The connection timed out. Please try again."
                    is UnknownHostException, is IOException -> "It looks like you're offline."
                    is HttpException -> {
                        when (e.code()) {
                            429 -> "Rate limit exceeded (429). Please wait a moment before trying again or check your API key credits."
                            401 -> "Unauthorized (401). Please check if your API key is correct."
                            500, 503 -> "Server error. OpenRouter might be experiencing issues."
                            else -> "Network error (${e.code()}). Please try again."
                        }
                    }
                    else -> "Something went wrong. (Error: ${e.localizedMessage ?: "Unknown"})"
                }
                val assistantMsg = ChatMessage("assistant", errorMessage)
                _messages.value += assistantMsg
                vibrationHelper.vibrateError()
                // Error messages are NOT saved to the database. 
                // Since we also didn't save the User message yet, no "failed" session is created.
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
        imageUris = imageUris,
        timestamp = timestamp
    )

    private fun ChatMessageEntity.toUiModel() = ChatMessage(
        id = id,
        role = role,
        content = content,
        imageUris = imageUris,
        timestamp = timestamp
    )
}
