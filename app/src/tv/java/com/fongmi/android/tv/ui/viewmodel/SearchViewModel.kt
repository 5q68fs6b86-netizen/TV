package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.SearchResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Search Screen
 * Handles search functionality with debounce
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val vodRepository: VodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // Search history (could be persisted to database)
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())

    init {
        // Load search history
        loadSearchHistory()
    }

    /**
     * Update search keyword with debounce
     */
    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }

        // Cancel previous search job
        searchJob?.cancel()

        if (keyword.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        // Debounce search
        searchJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            search(keyword)
        }
    }

    /**
     * Perform search immediately
     */
    fun search(keyword: String = _uiState.value.keyword) {
        if (keyword.isBlank()) return

        _uiState.update { it.copy(keyword = keyword) }

        viewModelScope.launch {
            vodRepository.search(keyword).collect { result ->
                when (result) {
                    is SearchResult.Loading -> {
                        _uiState.update { it.copy(isSearching = true, error = null) }
                    }
                    is SearchResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                results = result.results,
                                currentPage = result.currentPage,
                                totalPages = result.pageCount,
                                error = null
                            )
                        }
                        // Add to history
                        addToHistory(keyword)
                    }
                    is SearchResult.Error -> {
                        _uiState.update { it.copy(isSearching = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Load next page of results
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isSearching || currentState.currentPage >= currentState.totalPages) {
            return
        }

        viewModelScope.launch {
            vodRepository.search(currentState.keyword, currentState.currentPage + 1).collect { result ->
                when (result) {
                    is SearchResult.Loading -> {
                        _uiState.update { it.copy(isSearching = true) }
                    }
                    is SearchResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                results = it.results + result.results,
                                currentPage = result.currentPage,
                                totalPages = result.pageCount
                            )
                        }
                    }
                    is SearchResult.Error -> {
                        _uiState.update { it.copy(isSearching = false) }
                    }
                }
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                keyword = "",
                results = emptyList(),
                isSearching = false,
                currentPage = 1,
                totalPages = 1,
                error = null
            )
        }
    }

    /**
     * Search from history
     */
    fun searchFromHistory(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
        search(keyword)
    }

    /**
     * Get search history
     */
    fun getSearchHistory(): List<String> = _uiState.value.searchHistory

    /**
     * Clear search history
     */
    fun clearHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
        // TODO: Persist to database
    }

    /**
     * Remove item from history
     */
    fun removeFromHistory(keyword: String) {
        _uiState.update {
            it.copy(searchHistory = it.searchHistory.filter { h -> h != keyword })
        }
        // TODO: Persist to database
    }

    private fun loadSearchHistory() {
        // TODO: Load from database
        // For now, use in-memory list
    }

    private fun addToHistory(keyword: String) {
        if (keyword.isBlank()) return

        _uiState.update { state ->
            val newHistory = (listOf(keyword) + state.searchHistory.filter { it != keyword })
                .take(20) // Keep last 20 searches
            state.copy(searchHistory = newHistory)
        }
        // TODO: Persist to database
    }
}
