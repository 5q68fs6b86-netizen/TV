package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.AggregatedSearchResult
import com.fongmi.android.tv.data.repository.SearchResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.db.AppDatabase
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
 * Handles search functionality with debounce and multi-site aggregation
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val vodRepository: VodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        loadSearchableSites()
    }

    /**
     * Update search keyword with debounce
     */
    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }

        searchJob?.cancel()

        if (keyword.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), siteResults = emptyList(), isSearching = false, suggestions = emptyList()) }
            return
        }

        // Update suggestions
        updateSuggestions(keyword)

        // Debounce search
        searchJob = viewModelScope.launch {
            delay(500)
            if (_uiState.value.isAggregatedSearch) {
                searchMultiSite(keyword)
            } else {
                search(keyword)
            }
        }
    }

    /**
     * Perform single-site search
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
     * Perform multi-site aggregated search
     */
    fun searchMultiSite(keyword: String = _uiState.value.keyword) {
        if (keyword.isBlank()) return

        _uiState.update { it.copy(keyword = keyword) }

        viewModelScope.launch {
            val selectedSites = _uiState.value.selectedSites.ifEmpty { null }
            vodRepository.searchMultiSite(keyword, selectedSites).collect { result ->
                when (result) {
                    is AggregatedSearchResult.Loading -> {
                        _uiState.update { it.copy(isSearching = true, error = null) }
                    }
                    is AggregatedSearchResult.Success -> {
                        val allResults = result.siteResults.flatMap { it.results }
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                results = allResults,
                                siteResults = result.siteResults,
                                error = null
                            )
                        }
                        addToHistory(keyword)
                    }
                    is AggregatedSearchResult.Error -> {
                        _uiState.update { it.copy(isSearching = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Toggle aggregated search mode
     */
    fun toggleAggregatedSearch(enabled: Boolean) {
        _uiState.update { it.copy(isAggregatedSearch = enabled) }
        if (_uiState.value.keyword.isNotBlank()) {
            if (enabled) searchMultiSite() else search()
        }
    }

    /**
     * Toggle site selection for aggregated search
     */
    fun toggleSiteSelection(site: Site) {
        _uiState.update { state ->
            val newSelected = if (state.selectedSites.contains(site)) {
                state.selectedSites - site
            } else {
                state.selectedSites + site
            }
            state.copy(selectedSites = newSelected)
        }
    }

    /**
     * Load next page of results
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isSearching || currentState.currentPage >= currentState.totalPages) return

        viewModelScope.launch {
            vodRepository.search(currentState.keyword, currentState.currentPage + 1).collect { result ->
                when (result) {
                    is SearchResult.Loading -> _uiState.update { it.copy(isSearching = true) }
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
                    is SearchResult.Error -> _uiState.update { it.copy(isSearching = false) }
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
                siteResults = emptyList(),
                suggestions = emptyList(),
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
        if (_uiState.value.isAggregatedSearch) searchMultiSite(keyword) else search(keyword)
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
        viewModelScope.launch {
            try {
                AppDatabase.get().getSearchHistoryDao().clear()
            } catch (_: Exception) {}
        }
    }

    /**
     * Remove item from history
     */
    fun removeFromHistory(keyword: String) {
        _uiState.update {
            it.copy(searchHistory = it.searchHistory.filter { h -> h != keyword })
        }
        viewModelScope.launch {
            try {
                AppDatabase.get().getSearchHistoryDao().delete(keyword)
            } catch (_: Exception) {}
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                val history = AppDatabase.get().getSearchHistoryDao().getAll()
                _uiState.update { it.copy(searchHistory = history.map { h -> h.keyword }) }
            } catch (_: Exception) {}
        }
    }

    private fun loadSearchableSites() {
        val sites = vodRepository.getSearchableSites()
        _uiState.update { it.copy(searchableSites = sites) }
    }

    private fun addToHistory(keyword: String) {
        if (keyword.isBlank()) return

        _uiState.update { state ->
            val newHistory = (listOf(keyword) + state.searchHistory.filter { it != keyword }).take(20)
            state.copy(searchHistory = newHistory)
        }

        viewModelScope.launch {
            try {
                AppDatabase.get().getSearchHistoryDao().insert(
                    com.fongmi.android.tv.bean.History.search(keyword)
                )
            } catch (_: Exception) {}
        }
    }

    private fun updateSuggestions(keyword: String) {
        // Generate suggestions from history
        val suggestions = _uiState.value.searchHistory
            .filter { it.contains(keyword, ignoreCase = true) && it != keyword }
            .take(5)
        _uiState.update { it.copy(suggestions = suggestions) }
    }
}
