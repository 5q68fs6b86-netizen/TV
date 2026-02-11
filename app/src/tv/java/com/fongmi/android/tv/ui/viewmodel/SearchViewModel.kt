package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.bean.Hot
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Suggest
import com.fongmi.android.tv.bean.SuggestTwo
import com.fongmi.android.tv.data.repository.AggregatedSearchResult
import com.fongmi.android.tv.data.repository.SearchResult
import com.fongmi.android.tv.data.repository.SiteSearchResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.SearchUiState
import com.github.catvod.net.OkHttp
import com.github.catvod.utils.Trans
import com.google.common.net.HttpHeaders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Headers
import java.net.URLEncoder
import javax.inject.Inject

/**
 * ViewModel for Search Screen
 * Handles search functionality with debounce and multi-site aggregation
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val vodRepository: VodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(isAggregatedSearch = Setting.isAggregatedSearch()))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var suggestJob: Job? = null

    init {
        loadSearchHistory()
        loadSearchableSites()
        loadHotSearches()
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
     * Perform multi-site aggregated search with streaming results
     */
    fun searchMultiSite(keyword: String = _uiState.value.keyword) {
        if (keyword.isBlank()) return

        _uiState.update { it.copy(keyword = keyword) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val selectedSites = _uiState.value.selectedSites.ifEmpty { null }
            vodRepository.searchMultiSite(keyword, selectedSites).collect { result ->
                when (result) {
                    is AggregatedSearchResult.Loading -> {
                        _uiState.update { it.copy(isSearching = true, error = null, results = emptyList(), siteResults = emptyList(), focusedSite = null) }
                    }
                    is AggregatedSearchResult.Partial -> {
                        val allResults = result.siteResults.flatMap { it.results }
                        _uiState.update {
                            it.copy(
                                isSearching = result.stillSearching,
                                results = allResults,
                                siteResults = result.siteResults,
                                error = null
                            )
                        }
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
     * Set focused site to filter displayed results
     */
    fun focusSite(site: Site?) {
        _uiState.update { it.copy(focusedSite = site) }
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
        suggestJob?.cancel()
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
     * Append a character to the keyword (for keyboard input)
     */
    fun appendKeyword(char: String) {
        if (_uiState.value.keyword.length >= 20) return
        val newKeyword = _uiState.value.keyword + char
        _uiState.update { it.copy(keyword = newKeyword) }
        updateSuggestions(newKeyword)
    }

    /**
     * Delete the last character from the keyword (for keyboard input)
     */
    fun deleteLastChar() {
        val current = _uiState.value.keyword
        if (current.isEmpty()) return
        val newKeyword = current.dropLast(1)
        _uiState.update { it.copy(keyword = newKeyword) }
        if (newKeyword.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
        } else {
            updateSuggestions(newKeyword)
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
        Setting.putSearchHistory("")
    }

    /**
     * Remove item from history
     */
    fun removeFromHistory(keyword: String) {
        _uiState.update {
            val newHistory = it.searchHistory.filter { h -> h != keyword }
            Setting.putSearchHistory(newHistory.joinToString(","))
            it.copy(searchHistory = newHistory)
        }
    }

    private fun loadSearchHistory() {
        val saved = Setting.getSearchHistory()
        if (saved.isNotBlank()) {
            val history = saved.split(",").filter { it.isNotBlank() }
            _uiState.update { it.copy(searchHistory = history) }
        }
    }

    private fun loadSearchableSites() {
        val sites = vodRepository.getSearchableSites()
        _uiState.update { it.copy(searchableSites = sites) }
    }

    /**
     * Load hot searches from API with cache support
     */
    private fun loadHotSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load from cache first
            val cached = Hot.get(Setting.getHot())
            if (cached.isNotEmpty()) {
                _uiState.update { it.copy(hotSearches = cached) }
            }

            // Fetch fresh data from API
            try {
                val response = OkHttp.newCall(
                    "https://hot.api.coolmarket.eu.org/api/douban-hot-mixed",
                    Headers.headersOf(HttpHeaders.REFERER, "https://www.360kan.com/rank/general")
                ).execute()
                val items = Hot.get(response.body?.string() ?: "")
                if (items.isNotEmpty()) {
                    _uiState.update { it.copy(hotSearches = items) }
                }
            } catch (e: Exception) {
                // Use cached data on error
            }
        }
    }

    private fun addToHistory(keyword: String) {
        if (keyword.isBlank()) return

        _uiState.update { state ->
            val newHistory = (listOf(keyword) + state.searchHistory.filter { it != keyword }).take(20)
            Setting.putSearchHistory(newHistory.joinToString(","))
            state.copy(searchHistory = newHistory)
        }
    }

    /**
     * Update search suggestions by calling external APIs
     * Uses pinyin conversion for better Chinese input support
     */
    private fun updateSuggestions(keyword: String) {
        suggestJob?.cancel()

        if (keyword.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        suggestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(Trans.z2p(keyword), "UTF-8")
                val results = mutableListOf<String>()

                // Parallel requests to two suggestion APIs
                val aiseetDeferred = async {
                    try {
                        val response = OkHttp.newCall(
                            "https://tv.aiseet.atianqi.com/i-tvbin/qtv_video/search/get_search_smart_box?format=json&page_num=0&page_size=10&key=$encoded"
                        ).execute()
                        SuggestTwo.get(response.body?.string() ?: "")
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val iqiyiDeferred = async {
                    try {
                        val response = OkHttp.newCall(
                            "https://suggest.video.iqiyi.com/?if=mobile&key=$encoded"
                        ).execute()
                        Suggest.get(response.body?.string() ?: "")
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                results.addAll(aiseetDeferred.await())
                results.addAll(iqiyiDeferred.await())

                // Remove duplicates and limit to 10 suggestions
                val uniqueSuggestions = results.distinct().take(10)
                _uiState.update { it.copy(suggestions = uniqueSuggestions) }
            } catch (e: Exception) {
                // Fallback to history-based suggestions on error
                val historySuggestions = _uiState.value.searchHistory
                    .filter { it.contains(keyword, ignoreCase = true) && it != keyword }
                    .take(5)
                _uiState.update { it.copy(suggestions = historySuggestions) }
            }
        }
    }
}
