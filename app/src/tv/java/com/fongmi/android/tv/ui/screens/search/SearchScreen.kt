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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.SiteSearchResult
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.TvKeyboard
import com.fongmi.android.tv.ui.components.VodCard
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.SearchViewModel

/**
 * Search Screen with A-Z keyboard, multi-site search, and results grid
 * Layout: Left side - Keyboard | Right side - Hot/Suggestions/Results
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

    Row(
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
        // Left side: Keyboard area
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(end = 24.dp)
        ) {
            // Search input display (shows current keyword)
            SearchInputDisplay(
                keyword = uiState.keyword,
                isSearching = uiState.isSearching
            )

            Spacer(modifier = Modifier.height(16.dp))

            // A-Z Keyboard
            TvKeyboard(
                onCharClick = { char -> viewModel.appendKeyword(char) },
                onDeleteClick = { viewModel.deleteLastChar() },
                onDeleteLongClick = { viewModel.clearSearch() },
                onSearchClick = {
                    if (uiState.isAggregatedSearch) viewModel.searchMultiSite()
                    else viewModel.search()
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
        }

        // Right side: Hot/Suggestions/Results area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Aggregated search toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AggregatedSearchToggle(
                    isEnabled = uiState.isAggregatedSearch,
                    onToggle = { viewModel.toggleAggregatedSearch(it) }
                )
            }

            // Suggestions (when typing)
            if (uiState.suggestions.isNotEmpty() && uiState.keyword.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                SuggestionsSection(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { viewModel.searchFromHistory(it) }
                )
            }

            // Site filter (when aggregated search is enabled and has results)
            if (uiState.isAggregatedSearch && uiState.siteResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SiteFilterRow(
                    sites = uiState.siteResults.map { it.site },
                    selectedSites = uiState.selectedSites,
                    onSiteToggle = { viewModel.toggleSiteSelection(it) },
                    onSiteFocused = { viewModel.focusSite(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isSearching && uiState.results.isEmpty() && uiState.siteResults.isEmpty() -> {
                    SearchLoadingState()
                }
                uiState.results.isNotEmpty() || uiState.siteResults.isNotEmpty() -> {
                    if (uiState.isAggregatedSearch && uiState.siteResults.isNotEmpty()) {
                        AggregatedSearchResults(
                            siteResults = uiState.siteResults,
                            focusedSite = uiState.focusedSite,
                            isSearching = uiState.isSearching,
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
                    // Hot searches
                    if (uiState.hotSearches.isNotEmpty()) {
                        HotSearchesSection(
                            hotSearches = uiState.hotSearches,
                            onHotClick = { viewModel.searchFromHistory(it) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    // Search history
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
}

/**
 * Search input display showing current keyword
 */
@Composable
private fun SearchInputDisplay(
    keyword: String,
    isSearching: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = keyword.ifEmpty { "输入首字母搜索..." },
            style = TvTypography.BodyLarge,
            color = if (keyword.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (isSearching) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Hot searches section
 */
@Composable
private fun HotSearchesSection(
    hotSearches: List<String>,
    onHotClick: (String) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "热门搜索",
                style = TvTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hotSearches) { keyword ->
                FocusableItem(onClick = { onHotClick(keyword) }) { isFocused ->
                    Text(
                        text = keyword,
                        style = TvTypography.LabelMedium,
                        color = if (isFocused) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(
                                color = if (isFocused) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Suggestions section with title
 */
@Composable
private fun SuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "搜索建议",
            style = TvTypography.TitleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions) { suggestion ->
                FocusableItem(onClick = { onSuggestionClick(suggestion) }) { isFocused ->
                    Text(
                        text = suggestion,
                        style = TvTypography.LabelMedium,
                        color = if (isFocused) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                color = if (isFocused) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
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
private fun SiteFilterRow(
    sites: List<Site>,
    selectedSites: List<Site>,
    onSiteToggle: (Site) -> Unit,
    onSiteFocused: (Site?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sites) { site ->
            val isSelected = selectedSites.contains(site) || selectedSites.isEmpty()
            FocusableItem(
                onClick = { onSiteToggle(site) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) onSiteFocused(site)
                }
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .height(32.dp)
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
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = site.name ?: "未知",
                        style = TvTypography.LabelSmall,
                        maxLines = 1,
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
    focusedSite: Site?,
    isSearching: Boolean,
    onVodClick: (Vod) -> Unit
) {
    // Filter results based on focused site
    val displayResults = if (focusedSite != null) {
        siteResults.filter { it.site.key == focusedSite.key }
    } else {
        siteResults
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSearching) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "搜索中... (${siteResults.size}个站源已返回)",
                    style = TvTypography.LabelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            val limit = Setting.getSearchResultLimit()
            displayResults.filter { it.results.isNotEmpty() }.forEach { siteResult ->
                val limitedResults = if (limit > 0) siteResult.results.take(limit) else siteResult.results
                items(limitedResults) { vod ->
                    VodCard(
                        title = vod.vodName ?: "",
                        imageUrl = vod.vodPic,
                        subtitle = vod.vodRemarks,
                        siteName = siteResult.site.name,
                        onClick = { onVodClick(vod) }
                    )
                }
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
    FocusableItem(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp)
    ) { isFocused ->
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
