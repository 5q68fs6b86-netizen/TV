package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.Keep
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
 * UI State for Favorites Screen
 */
data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<Keep> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for Favorites Screen
 * Manages favorites (Keep) data
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Load favorites
     */
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = withContext(Dispatchers.IO) {
                    Keep.getVod()
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
     * Delete a favorite item
     */
    fun deleteItem(item: Keep) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                item.delete()
            }
            _uiState.update {
                it.copy(items = it.items.filter { k -> k.key != item.key })
            }
        }
    }

    /**
     * Clear all favorites
     */
    fun clearAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Keep.deleteAll()
            }
            _uiState.update { it.copy(items = emptyList()) }
        }
    }

    /**
     * Refresh favorites
     */
    fun refresh() {
        loadFavorites()
    }
}
