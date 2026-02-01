package com.fongmi.android.tv.ui.screens.search

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.SiteSearchResult
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.VodCard
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.SearchViewModel

/**
 * Search Screen with keyboard input, multi-site search, and results grid
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackClick()
                    true
                } else false
            }
    ) {
        // Search bar with aggregated search toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBar(
                keyword = uiState.keyword,
                onKeywordChange = { viewModel.updateKeyword(it) },
                onSearch = { if (uiState.isAggregatedSearch) viewModel.searchMultiSite() else viewModel.search() },
                onClear = { viewModel.clearSearch() },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )

            // Aggregated search toggle
            AggregatedSearchToggle(
                isEnabled = uiState.isAggregatedSearch,
                onToggle = { viewModel.toggleAggregatedSearch(it) }
            )
        }

        // Suggestions
        if (uiState.suggestions.isNotEmpty() && uiState.keyword.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionsRow(
                suggestions = uiState.suggestions,
                onSuggestionClick = { viewModel.searchFromHistory(it) }
            )
        }

        // Site filter (when aggregated search is enabled)
        if (uiState.isAggregatedSearch && uiState.searchableSites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SiteFilterRow(
                sites = uiState.searchableSites,
                selectedSites = uiState.selectedSites,
                onSiteToggle = { viewModel.toggleSiteSelection(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isSearching && uiState.results.isEmpty() -> {
                SearchLoadingState()
            }
            uiState.results.isNotEmpty() -> {
                if (uiState.isAggregatedSearch && uiState.siteResults.isNotEmpty()) {
                    AggregatedSearchResults(
                        siteResults = uiState.siteResults,
                        onVodClick = { vod ->
                            val siteKey = vod.site?.key ?: ""
                            onVodClick(siteKey, vod.vodId ?: "")
                        }
                    )
                } else {
                    SearchResults(
                        results = uiState.results,
                        isLoading = uiState.isSearching,
                        onVodClick = { vod ->
                            val siteKey = vod.site?.key ?: com.fongmi.android.tv.api.config.VodConfig.get().home?.key ?: ""
                            onVodClick(siteKey, vod.vodId ?: "")
                        },
                        onLoadMore = { viewModel.loadNextPage() }
                    )
                }
            }
            uiState.keyword.isBlank() -> {
                SearchHistorySection(
                    history = uiState.searchHistory,
                    onHistoryClick = { viewModel.searchFromHistory(it) },
                    onClearHistory = { viewModel.clearHistory() },
                    onRemoveItem = { viewModel.removeFromHistory(it) }
                )
            }
            else -> {
                NoResultsState()
            }
        }
    }
}

@Composable
private fun AggregatedSearchToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    FocusableItem(onClick = { onToggle(!isEnabled) }) { isFocused ->
        Row(
            modifier = Modifier
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TravelExplore,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "聚合",
                style = TvTypography.LabelMedium,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimary else if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (isEnabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(suggestions) { suggestion ->
            FocusableItem(onClick = { onSuggestionClick(suggestion) }) { isFocused ->
                Text(
                    text = suggestion,
                    style = TvTypography.LabelSmall,
                    color = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SiteFilterRow(
    sites: List<Site>,
    selectedSites: List<Site>,
    onSiteToggle: (Site) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sites) { site ->
            val isSelected = selectedSites.contains(site) || selectedSites.isEmpty()
            FocusableItem(onClick = { onSiteToggle(site) }) { isFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                isFocused -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isSelected && !isFocused) 1.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = site.name ?: "未知",
                        style = TvTypography.LabelSmall,
                        color = when {
                            isFocused -> MaterialTheme.colorScheme.onPrimary
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(text = "搜索中...", style = TvTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoResultsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "未找到相关内容", style = TvTypography.BodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AggregatedSearchResults(
    siteResults: List<SiteSearchResult>,
    onVodClick: (Vod) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        siteResults.filter { it.results.isNotEmpty() }.forEach { siteResult ->
            // Site header
            item {
                Text(
                    text = "${siteResult.site.name} (${siteResult.results.size})",
                    style = TvTypography.TitleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // Results from this site
            items(siteResult.results.take(6)) { vod ->
                VodCard(
                    title = vod.vodName ?: "",
                    imageUrl = vod.vodPic,
                    subtitle = vod.vodRemarks,
                    onClick = { onVodClick(vod) }
                )
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
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.weight(1f),
            textStyle = TvTypography.BodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box {
                    if (keyword.isEmpty()) {
                        Text(
                            text = "搜索影片、演员、导演...",
                            style = TvTypography.BodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "输入关键词开始搜索",
                    style = TvTypography.BodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "搜索历史",
                    style = TvTypography.TitleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            FocusableItem(onClick = onClearHistory) { isFocused ->
                Text(
                    text = "清除",
                    style = TvTypography.LabelMedium,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = keyword,
                style = TvTypography.LabelMedium,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove",
                    tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
