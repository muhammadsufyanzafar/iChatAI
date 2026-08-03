package com.zafar.ichatai.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FeedbackUiState(
    val selectedCategory: String = "General Feedback",
    val description: String = "",
    val attachedScreenshots: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val submissionStatus: SubmissionStatus? = null
)

sealed class SubmissionStatus {
    object Success : SubmissionStatus()
    data class Error(val message: String) : SubmissionStatus()
}

@HiltViewModel
class FeedbackViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    val categories = listOf(
        "General Feedback",
        "Bug / Crash Report",
        "Feature Request",
        "AI Model Accuracy / Hallucination",
        "UI / Design Suggestion",
        "Credit & Billing Inquiry"
    )

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun attachScreenshot(uri: Uri) {
        _uiState.update { state ->
            if (state.attachedScreenshots.size < 5) {
                state.copy(attachedScreenshots = state.attachedScreenshots + uri)
            } else {
                state
            }
        }
    }

    fun removeScreenshot(uri: Uri) {
        _uiState.update { state ->
            state.copy(attachedScreenshots = state.attachedScreenshots - uri)
        }
    }

    fun submitFeedback() {
        // Logic to be wired later as per requirements
        _uiState.update { it.copy(isSubmitting = true) }
        
        // Simulating submission for now
        // _uiState.update { it.copy(isSubmitting = false, submissionStatus = SubmissionStatus.Success) }
    }
    
    fun resetSubmissionStatus() {
        _uiState.update { it.copy(submissionStatus = null) }
    }
}
