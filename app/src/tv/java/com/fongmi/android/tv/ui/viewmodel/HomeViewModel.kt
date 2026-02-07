package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Result
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.CategoryContentResult
import com.fongmi.android.tv.data.repository.FirstSearchResult
import com.fongmi.android.tv.data.repository.HomeContentResult
import com.fongmi.android.tv.data.repository.TmdbItem
import com.fongmi.android.tv.data.repository.TmdbRepository
import com.fongmi.android.tv.data.repository.TmdbResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.HomeMode
import com.fongmi.android.tv.ui.state.HomeUiState
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
 * ViewModel for Home Screen
 * Manages UI state and business logic for the home page
 * Supports dual mode: TMDB poster wall and site source content
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Currently loaded category content
    private val _categoryContent = MutableStateFlow<Map<String, List<Vod>>>(emptyMap())
    val categoryContent: StateFlow<Map<String, List<Vod>>> = _categoryContent.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Load initial home data based on saved mode preference
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // Restore saved home mode
            val savedMode = HomeMode.fromValue(Setting.getHomeMode())
            val currentSite = vodRepository.getHomeSite()

            _uiState.update {
                it.copy(
                    homeMode = savedMode,
                    currentSite = currentSite
                )
            }

            // Load content based on mode
            when (savedMode) {
                HomeMode.TMDB -> loadTmdbContent()
                HomeMode.SITE -> {
                    if (currentSite != null) {
                        loadHomeContent(currentSite)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No site configured") }
                    }
                }
            }
        }
    }

    // ========== TMDB Mode ==========

    /**
     * Load TMDB trending movies and TV shows
     */
    fun loadTmdbContent() {
        viewModelScope.launch {
            tmdbRepository.loadTrendingAll().collect { result ->
                when (result) {
                    is TmdbResult.Loading -> {
                        _uiState.update { it.copy(isTmdbLoading = true, tmdbError = null) }
                    }
                    is TmdbResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isTmdbLoading = false,
                                isLoading = false,
                                tmdbMovies = result.movies,
                                tmdbTvShows = result.tvShows,
                                tmdbError = null
                            )
                        }
                    }
                    is TmdbResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isTmdbLoading = false,
                                isLoading = false,
                                tmdbError = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle TMDB poster click - search and navigate to detail
     */
    fun onTmdbItemClick(
        item: TmdbItem,
        onNavigateToDetail: (siteKey: String, vodId: String) -> Unit,
        onNotFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isQuickSearching = true, quickSearchTitle = item.title)
            }

            vodRepository.searchFirstResult(item.title).collect { result ->
                when (result) {
                    is FirstSearchResult.Loading -> {
                        // Already showing loading
                    }
                    is FirstSearchResult.Success -> {
                        _uiState.update {
                            it.copy(isQuickSearching = false, quickSearchTitle = null)
                        }
                        onNavigateToDetail(result.siteKey, result.vodId)
                    }
                    is FirstSearchResult.NotFound -> {
                        _uiState.update {
                            it.copy(isQuickSearching = false, quickSearchTitle = null)
                        }
                        onNotFound(item.title)
                    }
                    is FirstSearchResult.Error -> {
                        _uiState.update {
                            it.copy(isQuickSearching = false, quickSearchTitle = null)
                        }
                        onNotFound(item.title)
                    }
                }
            }
        }
    }

    // ========== Site Mode ==========

    /**
     * Load home content for a site
     */
    fun loadHomeContent(site: Site? = null) {
        val targetSite = site ?: _uiState.value.currentSite ?: return

        viewModelScope.launch {
            vodRepository.loadHomeContent(targetSite).collect { result ->
                when (result) {
                    is HomeContentResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is HomeContentResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = result.categories,
                                featuredContent = result.featured,
                                selectedCategory = result.categories.firstOrNull(),
                                error = null
                            )
                        }
                        // Load content for first category if available
                        result.categories.firstOrNull()?.let { category ->
                            loadCategoryContent(category)
                        }
                    }
                    is HomeContentResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Select a category and load its content
     */
    fun selectCategory(category: Class) {
        _uiState.update { it.copy(selectedCategory = category) }

        // Load content if not already loaded
        if (!_categoryContent.value.containsKey(category.typeId)) {
            loadCategoryContent(category)
        }
    }

    /**
     * Load content for a specific category
     */
    private fun loadCategoryContent(category: Class) {
        val site = _uiState.value.currentSite ?: return

        viewModelScope.launch {
            vodRepository.loadCategoryContent(site, category.typeId).collect { result ->
                when (result) {
                    is CategoryContentResult.Loading -> {
                        // Optionally show loading for category
                    }
                    is CategoryContentResult.Success -> {
                        _categoryContent.update { current ->
                            current + (category.typeId to result.content)
                        }
                    }
                    is CategoryContentResult.Error -> {
                        // Handle category load error
                    }
                }
            }
        }
    }

    // ========== Mode Switching ==========

    /**
     * Switch home mode (TMDB / Site)
     */
    fun switchHomeMode(mode: HomeMode) {
        if (_uiState.value.homeMode == mode) return

        // Save preference
        Setting.putHomeMode(mode.value)

        _uiState.update { it.copy(homeMode = mode, showModeDialog = false) }

        // Load content for new mode
        when (mode) {
            HomeMode.TMDB -> {
                if (_uiState.value.tmdbMovies.isEmpty()) {
                    loadTmdbContent()
                }
            }
            HomeMode.SITE -> {
                if (_uiState.value.featuredContent.isEmpty()) {
                    _uiState.value.currentSite?.let { loadHomeContent(it) }
                }
            }
        }
    }

    /**
     * Change the current site (for site mode)
     */
    fun changeSite(site: Site) {
        vodRepository.setHomeSite(site)
        _uiState.update {
            it.copy(
                currentSite = site,
                categories = emptyList(),
                featuredContent = emptyList(),
                categoryContent = emptyMap(),
                showModeDialog = false
            )
        }
        _categoryContent.update { emptyMap() }

        // If in site mode, load the new site's content
        if (_uiState.value.homeMode == HomeMode.SITE) {
            loadHomeContent(site)
        }
    }

    // ========== Dialog Management ==========

    /**
     * Show mode selection dialog
     */
    fun showModeDialog() {
        _uiState.update { it.copy(showModeDialog = true) }
    }

    /**
     * Dismiss mode selection dialog
     */
    fun dismissModeDialog() {
        _uiState.update { it.copy(showModeDialog = false) }
    }

    // ========== Utility ==========

    /**
     * Get all available sites
     */
    fun getSites(): List<Site> = vodRepository.getSites()

    /**
     * Refresh current content based on mode
     */
    fun refresh() {
        when (_uiState.value.homeMode) {
            HomeMode.TMDB -> loadTmdbContent()
            HomeMode.SITE -> {
                _categoryContent.update { emptyMap() }
                loadHomeContent()
            }
        }
    }

    /**
     * Get content for a specific category
     */
    fun getContentForCategory(categoryId: String): List<Vod> {
        return _categoryContent.value[categoryId] ?: emptyList()
    }

    /**
     * Execute action from jar plugin.
     * Matches Leanback's SiteViewModel.action() behavior.
     * JAR plugins show dialogs internally via App.post() + App.activity().
     */
    fun executeAction(action: String) {
        val site = VodConfig.get().home ?: return
        if (site.type != 3) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                site.recent().spider()?.action(action)
            }
        }
    }
}
