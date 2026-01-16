package com.fongmi.android.tv.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.ui.components.BannerItem
import com.fongmi.android.tv.ui.components.ContentItem
import com.fongmi.android.tv.ui.components.ContentRow
import com.fongmi.android.tv.ui.components.HeroBanner
import com.fongmi.android.tv.ui.components.NavItem
import com.fongmi.android.tv.ui.components.TvNavigationRail
import com.fongmi.android.tv.ui.components.WideContentRow
import com.fongmi.android.tv.ui.components.defaultNavItems
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.HomeViewModel

/**
 * Redesigned Home Screen with Side Navigation + Banner + Content Rows
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: (String) -> Unit,
    onVodClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLiveClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    // Load home content on first composition
    LaunchedEffect(Unit) {
        viewModel.loadHomeContent()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Side Navigation Rail
        TvNavigationRail(
            items = defaultNavItems,
            selectedIndex = selectedNavIndex,
            onItemSelected = { index, item ->
                selectedNavIndex = index
                when (item.route) {
                    "home" -> { /* Already here */ }
                    "search" -> onSearchClick()
                    "live" -> onLiveClick()
                    "favorites" -> onFavoritesClick()
                    "history" -> onHistoryClick()
                    "settings" -> onSettingsClick()
                }
            }
        )

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when {
                uiState.isLoading -> {
                    HomeLoadingState()
                }
                uiState.error != null -> {
                    HomeErrorState(
                        message = uiState.error ?: "未知错误",
                        onRetry = { viewModel.loadHomeContent() }
                    )
                }
                else -> {
                    HomeContent(
                        siteName = uiState.currentSite?.name ?: "",
                        categories = uiState.categories,
                        featuredVods = uiState.featuredContent.take(5),
                        recommendedVods = uiState.featuredContent,
                        onCategoryClick = onCategoryClick,
                        onVodClick = onVodClick
                    )
                }
            }
        }
    }
}

/**
 * Main content area with Banner and Content Rows
 */
@Composable
private fun HomeContent(
    siteName: String,
    categories: List<com.fongmi.android.tv.bean.Class>,
    featuredVods: List<Vod>,
    recommendedVods: List<Vod>,
    onCategoryClick: (String) -> Unit,
    onVodClick: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Banner (featured content)
        if (featuredVods.isNotEmpty()) {
            HeroBanner(
                items = featuredVods.map { vod ->
                    BannerItem(
                        id = vod.vodId ?: "",
                        title = vod.vodName ?: "",
                        subtitle = vod.vodYear ?: vod.typeName,
                        description = vod.vodContent,
                        imageUrl = vod.vodPic,
                        vodId = vod.vodId
                    )
                },
                onItemClick = { banner ->
                    banner.vodId?.let { vodId ->
                        onVodClick("", vodId)
                    }
                },
                height = 380.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Categories Row (horizontal scrollable categories)
        if (categories.isNotEmpty()) {
            ContentRow(
                title = "分类",
                items = categories.take(10).map { cls ->
                    ContentItem(
                        id = cls.typeId ?: "",
                        title = cls.typeName ?: "",
                        imageUrl = null,
                        siteKey = cls.typeId
                    )
                },
                onItemClick = { item ->
                    item.siteKey?.let { onCategoryClick(it) }
                },
                onSeeAllClick = { /* Navigate to all categories */ },
                cardWidth = 120.dp,
                cardAspectRatio = 1f
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Content Row
        if (recommendedVods.isNotEmpty()) {
            ContentRow(
                title = "推荐",
                items = recommendedVods.take(15).map { vod ->
                    ContentItem(
                        id = vod.vodId ?: "",
                        title = vod.vodName ?: "",
                        imageUrl = vod.vodPic,
                        subtitle = vod.vodYear ?: vod.vodRemarks,
                        badge = vod.vodRemarks,
                        vodId = vod.vodId
                    )
                },
                onItemClick = { item ->
                    item.vodId?.let { vodId ->
                        onVodClick(item.siteKey ?: "", vodId)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Additional content rows based on categories
        categories.take(3).forEach { category ->
            ContentRow(
                title = category.typeName ?: "",
                items = recommendedVods
                    .filter { it.typeName == category.typeName }
                    .take(10)
                    .map { vod ->
                        ContentItem(
                            id = vod.vodId ?: "",
                            title = vod.vodName ?: "",
                            imageUrl = vod.vodPic,
                            subtitle = vod.vodYear,
                            badge = vod.vodRemarks,
                            vodId = vod.vodId
                        )
                    },
                onItemClick = { item ->
                    item.vodId?.let { vodId ->
                        onVodClick(item.siteKey ?: "", vodId)
                    }
                },
                onSeeAllClick = {
                    category.typeId?.let { onCategoryClick(it) }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

/**
 * Loading state
 */
@Composable
private fun HomeLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "加载中...",
                style = TvTypography.BodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state
 */
@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "😢",
                style = TvTypography.DisplayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = TvTypography.BodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            com.fongmi.android.tv.ui.components.FocusableButton(
                onClick = onRetry,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) { isFocused ->
                Text(
                    text = "重试",
                    style = TvTypography.LabelLarge,
                    color = if (isFocused) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
