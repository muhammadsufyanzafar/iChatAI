package com.zafar.ichatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.FaqData
import com.zafar.ichatai.data.repository.HelpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val repository: HelpRepository
) : ViewModel() {

    private val _faqs = MutableStateFlow<List<FaqData>>(emptyList())
    val faqs: StateFlow<List<FaqData>> = _faqs.asStateFlow()

    init {
        loadFaqs()
    }

    private fun loadFaqs() {
        viewModelScope.launch {
            _faqs.value = repository.getFaqs()
        }
    }
}
