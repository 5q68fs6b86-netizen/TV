package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.CategoryContentResult
import com.fongmi.android.tv.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Category Screen
 */
data class CategoryUiState(
    val isLoading: Boolean = true,
    val categoryName: String = "",
    val content: List<Vod> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Category Screen
 * Handles category content loading with pagination
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val typeId: String? = savedStateHandle["typeId"]
    private var currentTypeId: String = ""

    init {
        typeId?.let { loadCategory(it) }
    }

    /**
     * Load category content
     */
    fun loadCategory(categoryId: String, categoryName: String = "") {
        currentTypeId = categoryId
        val site = VodConfig.get().home ?: return

        // Find category name from site types
        val name = if (categoryName.isNotEmpty()) {
            categoryName
        } else {
            site.types?.find { it.typeId == categoryId }?.typeName ?: categoryId
        }

        viewModelScope.launch {
            vodRepository.loadCategoryContent(site, categoryId, 1).collect { result ->
                when (result) {
                    is CategoryContentResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                error = null,
                                categoryName = name
                            )
                        }
                    }
                    is CategoryContentResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                content = result.content,
                                currentPage = result.currentPage,
                                totalPages = result.pageCount,
                                error = null
                            )
                        }
                    }
                    is CategoryContentResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Load next page
     */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage >= state.totalPages) return

        val site = VodConfig.get().home ?: return
        val nextPage = state.currentPage + 1

        viewModelScope.launch {
            vodRepository.loadCategoryContent(site, currentTypeId, nextPage).collect { result ->
                when (result) {
                    is CategoryContentResult.Loading -> {
                        _uiState.update { it.copy(isLoadingMore = true) }
                    }
                    is CategoryContentResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                content = it.content + result.content,
                                currentPage = result.currentPage,
                                totalPages = result.pageCount
                            )
                        }
                    }
                    is CategoryContentResult.Error -> {
                        _uiState.update { it.copy(isLoadingMore = false) }
                    }
                }
            }
        }
    }

    /**
     * Refresh content
     */
    fun refresh() {
        if (currentTypeId.isNotEmpty()) {
            loadCategory(currentTypeId, _uiState.value.categoryName)
        }
    }

    /**
     * Get current site key
     */
    fun getSiteKey(): String = VodConfig.get().home?.key ?: ""
}
