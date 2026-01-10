package com.fongmi.android.tv.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.VodCard
import com.fongmi.android.tv.ui.components.VodItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.SearchViewModel

/**
 * Search Screen with keyboard input and results grid
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onVodClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
    ) {
        // Search bar
        SearchBar(
            keyword = uiState.keyword,
            onKeywordChange = { viewModel.updateKeyword(it) },
            onSearch = { viewModel.search() },
            onClear = { viewModel.clearSearch() },
            modifier = Modifier.focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isSearching && uiState.results.isEmpty() -> {
                // Loading state
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
                            text = "搜索中...",
                            style = TvTypography.BodyMedium,
                            color = TvColors.TextSecondary
                        )
                    }
                }
            }
            uiState.results.isNotEmpty() -> {
                // Results grid
                SearchResults(
                    results = uiState.results,
                    isLoading = uiState.isSearching,
                    onVodClick = { vod ->
                        val siteKey = VodConfig.get().home?.key ?: ""
                        onVodClick(siteKey, vod.vodId ?: "")
                    },
                    onLoadMore = { viewModel.loadNextPage() }
                )
            }
            uiState.keyword.isBlank() -> {
                // Show search history
                SearchHistorySection(
                    history = uiState.searchHistory,
                    onHistoryClick = { viewModel.searchFromHistory(it) },
                    onClearHistory = { viewModel.clearHistory() },
                    onRemoveItem = { viewModel.removeFromHistory(it) }
                )
            }
            else -> {
                // No results
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到相关内容",
                        style = TvTypography.BodyLarge,
                        color = TvColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TvColors.Surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TvColors.TextSecondary
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.weight(1f),
            textStyle = TvTypography.BodyLarge.copy(color = TvColors.TextPrimary),
            singleLine = true,
            cursorBrush = SolidColor(TvColors.Primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box {
                    if (keyword.isEmpty()) {
                        Text(
                            text = "搜索影片、演员、导演...",
                            style = TvTypography.BodyLarge,
                            color = TvColors.TextSecondary
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (keyword.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = TvColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<Vod>,
    isLoading: Boolean,
    onVodClick: (Vod) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(results) { vod ->
            VodCard(
                title = vod.vodName ?: "",
                imageUrl = vod.vodPic,
                subtitle = vod.vodRemarks,
                onClick = { onVodClick(vod) }
            )
        }

        if (isLoading) {
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

@Composable
private fun SearchHistorySection(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveItem: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TvColors.TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "输入关键词开始搜索",
                    style = TvTypography.BodyLarge,
                    color = TvColors.TextSecondary
                )
            }
        }
        return
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TvColors.TextSecondary
                )
                Text(
                    text = "搜索历史",
                    style = TvTypography.TitleMedium,
                    color = TvColors.TextPrimary
                )
            }

            FocusableItem(onClick = onClearHistory) { isFocused ->
                Text(
                    text = "清除",
                    style = TvTypography.LabelMedium,
                    color = if (isFocused) TvColors.Primary else TvColors.TextSecondary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { keyword ->
                HistoryChip(
                    keyword = keyword,
                    onClick = { onHistoryClick(keyword) },
                    onRemove = { onRemoveItem(keyword) }
                )
            }
        }
    }
}

@Composable
private fun HistoryChip(
    keyword: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .background(
                    color = if (isFocused) TvColors.Primary else TvColors.Surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = keyword,
                style = TvTypography.LabelMedium,
                color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove",
                    tint = if (isFocused) TvColors.OnPrimary else TvColors.TextSecondary
                )
            }
        }
    }
}
