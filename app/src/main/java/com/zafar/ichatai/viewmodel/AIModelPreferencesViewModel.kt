package com.zafar.ichatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.BuildConfig
import com.zafar.ichatai.data.AIModel
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.network.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIModelPreferencesViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {

    private val defaultModel = AIModel(
        id = "openrouter/free",
        name = "Model A - Standard",
        description = "Fast, balanced performance",
        apiKey = BuildConfig.OPENROUTER_API_KEY,
        isRecommended = true
    )

    private val _models = MutableStateFlow<List<AIModel>>(listOf(defaultModel))
    val models: StateFlow<List<AIModel>> = _models.asStateFlow()

    private val _selectedModelId = MutableStateFlow(userPreferences.getSelectedModelId())
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _temperature = MutableStateFlow(userPreferences.getTemperature())
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchModels()
    }

    fun fetchModels() {
        viewModelScope.launch {
            _isLoading.value = true
            remoteConfigManager.fetchAndActivate()
            val remoteModels = remoteConfigManager.getAIModels()
            
            val allModels = mutableListOf(defaultModel)
            
            // Add or override models from Remote Config
            remoteModels.forEach { remote ->
                val existingIndex = allModels.indexOfFirst { it.id == remote.id }
                if (existingIndex != -1) {
                    allModels[existingIndex] = remote
                } else {
                    allModels.add(remote)
                }
            }
            
            _models.value = allModels
            _isLoading.value = false
        }
    }

    fun selectModel(modelId: String) {
        _selectedModelId.value = modelId
        userPreferences.setSelectedModelId(modelId)
    }

    fun setTemperature(temp: Float) {
        _temperature.value = temp
        userPreferences.setTemperature(temp)
    }
}
