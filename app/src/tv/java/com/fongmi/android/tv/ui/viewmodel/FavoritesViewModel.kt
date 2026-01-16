package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.data.repository.FavoritesListResult
import com.fongmi.android.tv.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

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
            favoritesRepository.getFavorites().collect { result ->
                when (result) {
                    is FavoritesListResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is FavoritesListResult.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, items = result.items)
                        }
                    }
                    is FavoritesListResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Delete a favorite item
     */
    fun deleteItem(item: Keep) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(item)
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
            favoritesRepository.clearFavorites()
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

