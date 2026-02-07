package com.fongmi.android.tv.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.VodCard
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.ActionResult
import com.fongmi.android.tv.ui.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

/**
 * Category Screen - displays content for a specific category with pagination
 * Supports folder navigation for jar plugins that return nested content
 */
@Composable
fun CategoryScreen(
    typeId: String,
    typeName: String = "",
    viewModel: CategoryViewModel = hiltViewModel(),
    onVodClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Handle action results
    LaunchedEffect(actionResult) {
        when (val result = actionResult) {
            is ActionResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                viewModel.clearActionResult()
            }
            is ActionResult.Success -> {
                // Action completed successfully
                // If result contains a message, show it
                result.result.msg?.let { msg ->
                    if (msg.isNotEmpty()) {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.clearActionResult()
            }
            else -> {}
        }
    }

    // Load category on first composition
    LaunchedEffect(typeId) {
        viewModel.loadCategory(typeId, typeName)
    }

    // Auto-load more when reaching end
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 6 && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.currentPage < uiState.totalPages) {
            viewModel.loadNextPage()
        }
    }

    /**
     * Handle back navigation - if inside folder, go back; else exit screen
     */
    fun handleBackNavigation() {
        if (viewModel.canNavigateBack()) {
            val scrollPosition = viewModel.navigateBack()
            // Restore scroll position after content loads
            scrollPosition?.let { pos ->
                coroutineScope.launch {
                    // Wait a bit for content to load
                    kotlinx.coroutines.delay(100)
                    gridState.scrollToItem(pos)
                }
            }
        } else {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    handleBackNavigation()
                    true
                } else {
                    false
                }
            }
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FocusableItem(
                onClick = { handleBackNavigation() },
                modifier = Modifier.focusRequester(focusRequester)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.categoryName.ifEmpty { "分类" },
                        style = TvTypography.HeadlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Show folder depth indicator
                    if (uiState.folderDepth > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.height(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "深度 ${uiState.folderDepth}",
                                    style = TvTypography.LabelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                if (uiState.totalPages > 1) {
                    Text(
                        text = "第 ${uiState.currentPage} / ${uiState.totalPages} 页",
                        style = TvTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "加载中...",
                            style = TvTypography.BodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "加载失败",
                            style = TvTypography.BodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FocusableItem(onClick = { viewModel.refresh() }) { isFocused ->
                            Text(
                                text = "重试",
                                style = TvTypography.LabelLarge,
                                color = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(
                                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            uiState.content.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无内容",
                        style = TvTypography.BodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.content) { vod ->
                        VodCard(
                            title = vod.vodName ?: "",
                            imageUrl = vod.vodPic,
                            subtitle = vod.vodRemarks,
                            // Show folder icon for folder items
                            badge = if (vod.isFolder) "📁" else null,
                            onClick = {
                                System.out.println("TV_Click_Debug: CategoryScreen onClick - vodName='${vod.vodName}', vodId='${vod.vodId}', isAction=${vod.isAction}, action='${vod.action}', isFolder=${vod.isFolder}")
                                when {
                                    // Action button from jar plugin
                                    vod.isAction -> {
                                        System.out.println("TV_Click_Debug: CategoryScreen -> executeAction")
                                        viewModel.executeAction(vod.action ?: "")
                                    }
                                    // Folder navigation
                                    vod.isFolder -> {
                                        System.out.println("TV_Click_Debug: CategoryScreen -> navigateIntoFolder")
                                        // Get current scroll position before navigating
                                        val currentPosition = gridState.firstVisibleItemIndex
                                        viewModel.navigateIntoFolder(vod, currentPosition)
                                    }
                                    // Normal video click
                                    else -> {
                                        System.out.println("TV_Click_Debug: CategoryScreen -> onVodClick")
                                        onVodClick(viewModel.getSiteKey(), vod.vodId ?: "")
                                    }
                                }
                            }
                        )
                    }

                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Action loading overlay
    if (uiState.isActionLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "处理中...",
                    style = TvTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
