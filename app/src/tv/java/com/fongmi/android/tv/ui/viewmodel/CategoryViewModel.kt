package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Result
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.CategoryContentResult
import com.fongmi.android.tv.data.repository.VodRepository
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
 * Navigation stack entry for folder navigation
 */
data class FolderStackEntry(
    val typeId: String,
    val typeName: String,
    val scrollPosition: Int = 0
)

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
    val error: String? = null,
    // Action handling state
    val isActionLoading: Boolean = false,
    // Folder navigation state
    val folderDepth: Int = 0,  // 0 = root, >0 = inside folder(s)
    val canNavigateBack: Boolean = false
)

/**
 * Result for action execution
 */
sealed class ActionResult {
    data object Loading : ActionResult()
    data class Success(val result: Result) : ActionResult()
    data class Error(val message: String) : ActionResult()
}

/**
 * ViewModel for Category Screen
 * Handles category content loading with pagination and folder navigation
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    // Action result for UI to observe
    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    // Folder navigation stack
    private val folderStack = mutableListOf<FolderStackEntry>()

    private val typeId: String? = savedStateHandle["typeId"]
    private var currentTypeId: String = ""
    private var rootTypeId: String = ""
    private var rootTypeName: String = ""

    init {
        typeId?.let { loadCategory(it) }
    }

    /**
     * Load category content
     */
    fun loadCategory(categoryId: String, categoryName: String = "") {
        currentTypeId = categoryId
        // Save root info for navigation
        if (rootTypeId.isEmpty()) {
            rootTypeId = categoryId
            rootTypeName = categoryName
        }

        val site = VodConfig.get().home ?: return

        // Use provided name or fallback to categoryId
        val name = categoryName.ifEmpty { categoryId }

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
                                error = null,
                                folderDepth = folderStack.size,
                                canNavigateBack = folderStack.isNotEmpty()
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
     * Navigate into a folder
     * @param folder The folder Vod item
     * @param currentScrollPosition Current scroll position to restore later
     */
    fun navigateIntoFolder(folder: Vod, currentScrollPosition: Int = 0) {
        // Push current state to stack
        folderStack.add(
            FolderStackEntry(
                typeId = currentTypeId,
                typeName = _uiState.value.categoryName,
                scrollPosition = currentScrollPosition
            )
        )

        // Load folder content using vodId as new typeId
        val folderName = folder.vodName ?: folder.vodId ?: ""
        loadCategory(folder.vodId ?: "", folderName)
    }

    /**
     * Navigate back from current folder
     * @return The scroll position to restore, or null if at root
     */
    fun navigateBack(): Int? {
        if (folderStack.isEmpty()) return null

        val previousEntry = folderStack.removeLast()
        loadCategory(previousEntry.typeId, previousEntry.typeName)
        return previousEntry.scrollPosition
    }

    /**
     * Check if we can navigate back (inside a folder)
     */
    fun canNavigateBack(): Boolean = folderStack.isNotEmpty()

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
     * Execute action from jar plugin
     * This handles special action buttons that jar plugins may return.
     * NOTE: Runs on IO thread like Leanback does. JAR plugins that need to show
     * dialogs will use App.post() to post to main thread themselves.
     * Running on Main thread causes "Window has no registered input channel" crash
     * when jar plugins trigger system dialogs or start new activities.
     */
    fun executeAction(action: String) {
        val site = VodConfig.get().home ?: return
        if (site.type != 3) return // Only jar plugins (type 3) support actions

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            _actionResult.update { ActionResult.Loading }

            try {
                // Run on IO thread - jar plugins will use App.post() for dialogs
                // This matches Leanback's SiteViewModel.action() behavior
                val result = withContext(Dispatchers.IO) {
                    val spider = site.recent().spider()
                    val json = spider?.action(action)
                    if (json != null) Result.fromJson(json) else null
                }

                if (result != null) {
                    _actionResult.update { ActionResult.Success(result) }

                    // If action returns new content list, update the UI
                    if (!result.list.isNullOrEmpty()) {
                        _uiState.update {
                            it.copy(
                                content = result.list,
                                isActionLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isActionLoading = false) }
                    }
                } else {
                    _actionResult.update { ActionResult.Error("Action returned empty result") }
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            } catch (e: Exception) {
                _actionResult.update { ActionResult.Error(e.message ?: "Action failed") }
                _uiState.update { it.copy(isActionLoading = false) }
            }
        }
    }

    /**
     * Clear action result after handling
     */
    fun clearActionResult() {
        _actionResult.update { null }
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
