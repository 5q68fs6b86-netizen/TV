package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.History
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
class HistoryViewModel @Inject constructor() : ViewModel() {

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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = withContext(Dispatchers.IO) {
                    History.get()
                }
                _uiState.update {
                    it.copy(isLoading = false, items = items)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            }
        }
    }

    /**
     * Delete a history item
     */
    fun deleteItem(item: History) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                item.delete()
            }
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
            withContext(Dispatchers.IO) {
                History.delete(VodConfig.getCid())
            }
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
