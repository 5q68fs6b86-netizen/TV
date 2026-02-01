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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.VodCard
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.CategoryViewModel

/**
 * Category Screen - displays content for a specific category with pagination
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
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackClick()
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
                onClick = onBackClick,
                modifier = Modifier.focusRequester(focusRequester)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isFocused) TvColors.Primary else TvColors.Surface,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                    )
                }
            }

            Column {
                Text(
                    text = uiState.categoryName.ifEmpty { "分类" },
                    style = TvTypography.HeadlineLarge,
                    color = TvColors.TextPrimary
                )
                if (uiState.totalPages > 1) {
                    Text(
                        text = "第 ${uiState.currentPage} / ${uiState.totalPages} 页",
                        style = TvTypography.BodySmall,
                        color = TvColors.TextSecondary
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
                        CircularProgressIndicator(color = TvColors.Primary)
                        Text(
                            text = "加载中...",
                            style = TvTypography.BodyMedium,
                            color = TvColors.TextSecondary
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
                            color = TvColors.TextSecondary
                        )
                        FocusableItem(onClick = { viewModel.refresh() }) { isFocused ->
                            Text(
                                text = "重试",
                                style = TvTypography.LabelLarge,
                                color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary,
                                modifier = Modifier
                                    .background(
                                        color = if (isFocused) TvColors.Primary else TvColors.Surface,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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
                        color = TvColors.TextSecondary
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
                            onClick = {
                                onVodClick(viewModel.getSiteKey(), vod.vodId ?: "")
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
                                CircularProgressIndicator(color = TvColors.Primary)
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
}
