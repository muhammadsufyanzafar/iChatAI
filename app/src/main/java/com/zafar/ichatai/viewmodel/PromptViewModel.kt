package com.zafar.ichatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.PromptFolderWithCount
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import com.zafar.ichatai.data.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PromptSortOrder {
    NEWEST, ALPHABETICAL, MOST_USED
}

@HiltViewModel
class PromptViewModel @Inject constructor(
    private val repository: PromptRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(PromptSortOrder.NEWEST)
    val sortOrder: StateFlow<PromptSortOrder> = _sortOrder.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val folders: StateFlow<List<PromptFolderWithCount>> = combine(
        repository.getAllFoldersWithCount(),
        _sortOrder
    ) { folders, sort ->
        when (sort) {
            PromptSortOrder.NEWEST -> folders.sortedByDescending { it.folder.timestamp }
            PromptSortOrder.ALPHABETICAL -> folders.sortedBy { it.folder.name.lowercase() }
            PromptSortOrder.MOST_USED -> folders.sortedByDescending { it.promptCount }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val individualPrompts: StateFlow<List<SavedPromptEntity>> = combine(
        _searchQuery,
        _sortOrder,
        _selectedFolderId
    ) { query, sort, folderId ->
        Triple(query, sort, folderId)
    }.flatMapLatest { (query, sort, folderId) ->
        val promptsFlow = if (folderId != null) {
            repository.getPromptsByFolder(folderId)
        } else {
            repository.getIndividualPrompts()
        }
        
        promptsFlow.map { prompts ->
            val filtered = if (query.isBlank()) prompts
            else prompts.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
            
            when (sort) {
                PromptSortOrder.NEWEST -> filtered.sortedByDescending { maxOf(it.lastUsed, it.timestamp) }
                PromptSortOrder.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
                PromptSortOrder.MOST_USED -> filtered.sortedByDescending { it.usageCount }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        seedInitialData()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: PromptSortOrder) {
        _sortOrder.value = order
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun addFolder(name: String, colorHex: String = "#4285F4") {
        viewModelScope.launch {
            repository.insertFolder(PromptFolderEntity(name = name, colorHex = colorHex))
        }
    }

    fun updateFolder(folder: PromptFolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folder: PromptFolderEntity) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }

    fun addPrompt(title: String, content: String, folderId: Long? = null, tag: String? = null) {
        viewModelScope.launch {
            repository.insertPrompt(SavedPromptEntity(title = title, content = content, folderId = folderId, tag = tag))
        }
    }

    fun updatePrompt(prompt: SavedPromptEntity) {
        viewModelScope.launch {
            repository.updatePrompt(prompt)
        }
    }

    fun deletePrompt(prompt: SavedPromptEntity) {
        viewModelScope.launch {
            repository.deletePrompt(prompt)
        }
    }

    fun duplicatePrompt(prompt: SavedPromptEntity) {
        viewModelScope.launch {
            repository.insertPrompt(prompt.copy(id = 0, title = "${prompt.title} (Copy)"))
        }
    }
    
    fun duplicateFolder(folderWithCount: PromptFolderWithCount) {
        viewModelScope.launch {
            val folder = folderWithCount.folder
            val newFolderId = repository.insertFolder(folder.copy(id = 0, name = "${folder.name} (Copy)"))
            repository.getPromptsByFolder(folder.id).first().forEach { prompt ->
                repository.insertPrompt(prompt.copy(id = 0, folderId = newFolderId))
            }
        }
    }

    fun getPromptsByFolder(folderId: Long): Flow<List<SavedPromptEntity>> = repository.getPromptsByFolder(folderId)

    fun updatePromptUsage(id: Long) {
        viewModelScope.launch {
            repository.updatePromptUsage(id)
        }
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            val currentFolders = repository.getAllFoldersWithCount().first()
            if (currentFolders.isEmpty()) {
                // 1. Productivity & Business
                val workId = repository.insertFolder(PromptFolderEntity(name = "Productivity & Business", colorHex = "#4285F4"))
                repository.insertPrompt(SavedPromptEntity(folderId = workId, title = "Professional Email Polisher", content = "Please rewrite the following email to make it more professional, concise, and clear while maintaining a friendly tone: [Insert Email]", tag = "Work"))
                repository.insertPrompt(SavedPromptEntity(folderId = workId, title = "Meeting Agenda Creator", content = "Generate a structured meeting agenda for a [Insert Duration] meeting about [Insert Topic]. Include goals and time slots for each item.", tag = "Meeting"))
                repository.insertPrompt(SavedPromptEntity(folderId = workId, title = "SWOT Analysis", content = "Perform a SWOT analysis (Strengths, Weaknesses, Opportunities, Threats) for a business idea involving [Insert Business Concept].", tag = "Strategy"))

                // 2. Programming & Development
                val devId = repository.insertFolder(PromptFolderEntity(name = "Programming & Dev", colorHex = "#34A853"))
                repository.insertPrompt(SavedPromptEntity(folderId = devId, title = "Code Debugger", content = "I am getting an error in my [Insert Language] code. Can you help me find the bug and suggest a fix? Here is the code: [Insert Code]", tag = "Debug"))
                repository.insertPrompt(SavedPromptEntity(folderId = devId, title = "Explain Code Simply", content = "Explain what this code snippet does in simple terms as if I am a beginner: [Insert Code]", tag = "Learning"))
                repository.insertPrompt(SavedPromptEntity(folderId = devId, title = "Regex Helper", content = "Create a regular expression that matches [Insert Requirement] and provide a brief explanation of how it works.", tag = "Utility"))

                // 3. Writing & Content
                val writingId = repository.insertFolder(PromptFolderEntity(name = "Writing & Content", colorHex = "#EA4335"))
                repository.insertPrompt(SavedPromptEntity(folderId = writingId, title = "Blog Post Intro", content = "Write a catchy and engaging introduction for a blog post titled '[Insert Title]'.", tag = "Content"))
                repository.insertPrompt(SavedPromptEntity(folderId = writingId, title = "Creative Story Starter", content = "Give me a unique and mysterious opening sentence for a story about [Insert Theme].", tag = "Creative"))
                repository.insertPrompt(SavedPromptEntity(folderId = writingId, title = "Social Media Captions", content = "Generate 5 different Instagram captions for a photo about [Insert Topic]. Include relevant hashtags.", tag = "Social"))

                // 4. Education & Study
                val eduId = repository.insertFolder(PromptFolderEntity(name = "Education & Study", colorHex = "#FBBC05"))
                repository.insertPrompt(SavedPromptEntity(folderId = eduId, title = "Summarize Text", content = "Summarize the following text into 5 key bullet points: [Insert Text]", tag = "Study"))
                repository.insertPrompt(SavedPromptEntity(folderId = eduId, title = "Complex Topic Simplifier", content = "Explain the concept of [Insert Topic] to me like I'm 10 years old.", tag = "Education"))
                repository.insertPrompt(SavedPromptEntity(folderId = eduId, title = "Flashcard Generator", content = "Create 10 question-and-answer pairs based on the following information for study flashcards: [Insert Text]", tag = "Exam Prep"))

                // 5. Personal & Lifestyle
                val lifeId = repository.insertFolder(PromptFolderEntity(name = "Personal & Lifestyle", colorHex = "#9C27B0"))
                repository.insertPrompt(SavedPromptEntity(folderId = lifeId, title = "Weekly Meal Planner", content = "Create a healthy 7-day meal plan for a person with [Insert Dietary Restrictions]. Include a simple grocery list.", tag = "Health"))
                repository.insertPrompt(SavedPromptEntity(folderId = lifeId, title = "Workout Routine", content = "Design a 30-minute home workout routine for [Insert Goal: e.g., weight loss, muscle gain] without any equipment.", tag = "Fitness"))
                repository.insertPrompt(SavedPromptEntity(folderId = lifeId, title = "Travel Itinerary", content = "Create a 3-day travel itinerary for a first-time visitor to [Insert City]. Focus on [Insert Interests: e.g., food, history, relaxation].", tag = "Travel"))

                // Some Individual Prompts
                repository.insertPrompt(SavedPromptEntity(title = "Language Translator", content = "Translate the following text into [Insert Language]: [Insert Text]", tag = "General"))
                repository.insertPrompt(SavedPromptEntity(title = "Gift Idea Generator", content = "Suggest 5 unique gift ideas for a [Insert Age]-year-old [Insert Gender] who likes [Insert Interests].", tag = "Lifestyle"))
            }
        }
    }
}
