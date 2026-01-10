package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.CategoryContentResult
import com.fongmi.android.tv.data.repository.HomeContentResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Manages UI state and business logic for the home page
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vodRepository: VodRepository
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
     * Load initial home data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            val currentSite = vodRepository.getHomeSite()
            _uiState.update { it.copy(currentSite = currentSite) }

            if (currentSite != null) {
                loadHomeContent(currentSite)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "No site configured") }
            }
        }
    }

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

    /**
     * Change the current site
     */
    fun changeSite(site: Site) {
        vodRepository.setHomeSite(site)
        _uiState.update {
            it.copy(
                currentSite = site,
                categories = emptyList(),
                featuredContent = emptyList(),
                categoryContent = emptyMap()
            )
        }
        _categoryContent.update { emptyMap() }
        loadHomeContent(site)
    }

    /**
     * Get all available sites
     */
    fun getSites(): List<Site> = vodRepository.getSites()

    /**
     * Refresh current content
     */
    fun refresh() {
        _categoryContent.update { emptyMap() }
        loadHomeContent()
    }

    /**
     * Get content for a specific category
     */
    fun getContentForCategory(categoryId: String): List<Vod> {
        return _categoryContent.value[categoryId] ?: emptyList()
    }
}
