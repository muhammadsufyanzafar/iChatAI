package com.zafar.ichatai.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.BuildConfig
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.network.GitHubApiService
import com.zafar.ichatai.network.GitHubIssueRequest
import com.zafar.ichatai.utils.DeviceInfoCollector
import com.zafar.ichatai.utils.NavigationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val selectedCategory: String = "General Feedback",
    val description: String = "",
    val attachedScreenshots: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val application: Application,
    private val userPreferences: UserPreferences,
    private val gitHubApiService: GitHubApiService
) : ViewModel() {

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
        val currentState = _uiState.value
        if (currentState.description.isBlank()) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                val telemetry = DeviceInfoCollector.collectTelemetry(
                    application,
                    userPreferences,
                    NavigationTracker.getTrail()
                )

                val issueBody = StringBuilder()
                issueBody.append("## User Description\n")
                issueBody.append("${currentState.description}\n\n")
                issueBody.append("## Device Telemetry\n")
                issueBody.append(telemetry)

                if (currentState.attachedScreenshots.isNotEmpty()) {
                    issueBody.append("\n\n## Attached Screenshots\n")
                    issueBody.append("Note: Internal URIs attached. Screenshots require separate upload handling if needed.\n")
                    currentState.attachedScreenshots.forEach { uri ->
                        issueBody.append("- `$uri`\n")
                    }
                }

                val request = GitHubIssueRequest(
                    title = "[${currentState.selectedCategory}] Feedback from ${userPreferences.getUserName()}",
                    body = issueBody.toString(),
                    labels = listOf("feedback", currentState.selectedCategory.lowercase().replace(" ", "-"))
                )

                val response = gitHubApiService.createIssue(
                    owner = BuildConfig.GITHUB_OWNER,
                    repo = BuildConfig.GITHUB_REPO,
                    token = "Bearer ${BuildConfig.GITHUB_TOKEN}",
                    request = request
                )

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                } else {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = false) }
            }
        }
    }

    fun resetSubmissionStatus() {
        _uiState.update { it.copy(submitSuccess = null) }
    }
}
