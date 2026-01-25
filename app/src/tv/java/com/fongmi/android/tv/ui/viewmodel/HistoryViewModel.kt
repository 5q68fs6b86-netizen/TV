package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.data.repository.HistoryListResult
import com.fongmi.android.tv.data.repository.HistoryRepository
import com.fongmi.android.tv.ui.dialog.HistoryClearOption
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
    val error: String? = null,
    val showClearDialog: Boolean = false,
    val selectedItems: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
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

    // ========== Clear Dialog ==========

    fun showClearDialog() {
        _uiState.update { it.copy(showClearDialog = true) }
    }

    fun dismissClearDialog() {
        _uiState.update { it.copy(showClearDialog = false) }
    }

    fun clearByOption(option: HistoryClearOption) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            when (option) {
                HistoryClearOption.TODAY -> {
                    val todayStart = now - (now % (24 * 60 * 60 * 1000))
                    historyRepository.clearHistoryByTime(todayStart)
                }
                HistoryClearOption.WEEK -> {
                    historyRepository.clearHistoryByTime(now - 7 * 24 * 60 * 60 * 1000L)
                }
                HistoryClearOption.MONTH -> {
                    historyRepository.clearHistoryByTime(now - 30 * 24 * 60 * 60 * 1000L)
                }
                HistoryClearOption.ALL -> {
                    historyRepository.clearHistory()
                }
            }
            loadHistory()
        }
    }

    // ========== Selection Mode ==========

    fun toggleSelectionMode() {
        _uiState.update {
            it.copy(isSelectionMode = !it.isSelectionMode, selectedItems = emptySet())
        }
    }

    fun toggleItemSelection(key: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItems.contains(key)) {
                state.selectedItems - key
            } else {
                state.selectedItems + key
            }
            state.copy(selectedItems = newSelected)
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            state.copy(selectedItems = state.items.map { it.key }.toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItems = emptySet()) }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _uiState.value.selectedItems.forEach { key ->
                historyRepository.deleteHistoryByKey(key)
            }
            _uiState.update {
                it.copy(
                    items = it.items.filter { h -> !it.selectedItems.contains(h.key) },
                    selectedItems = emptySet(),
                    isSelectionMode = false
                )
            }
        }
    }
}

