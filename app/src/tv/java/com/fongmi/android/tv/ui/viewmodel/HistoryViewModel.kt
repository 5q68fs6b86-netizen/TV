package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.data.repository.HistoryListResult
import com.fongmi.android.tv.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for History Screen
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<History> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for History Screen
 * Manages watch history data
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * Load watch history
     */
    fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getHistory().collect { result ->
                when (result) {
                    is HistoryListResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is HistoryListResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, items = result.items)
                        }
                    }
                    is HistoryListResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Delete a history item
     */
    fun deleteItem(item: History) {
        viewModelScope.launch {
            historyRepository.deleteHistory(item)
            _uiState.update {
                it.copy(items = it.items.filter { h -> h.key != item.key })
            }
        }
    }

    /**
     * Clear all history
     */
    fun clearAll() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            _uiState.update { it.copy(items = emptyList()) }
        }
    }

    /**
     * Refresh history
     */
    fun refresh() {
        loadHistory()
    }
}

