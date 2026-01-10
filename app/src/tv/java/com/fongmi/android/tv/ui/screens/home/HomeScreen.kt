package com.fongmi.android.tv.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.components.CategoryChipRow
import com.fongmi.android.tv.ui.components.CategoryItem
import com.fongmi.android.tv.ui.components.ContentRow
import com.fongmi.android.tv.ui.components.FocusableButton
import com.fongmi.android.tv.ui.components.VodItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * Home Screen for TV App
 * Displays categories, recommendations, and quick access buttons.
 */
@Composable
fun HomeScreen(
    onCategoryClick: (String) -> Unit,
    onVodClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLiveClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    // TODO: Replace with actual ViewModel data
    val categories = remember {
        listOf(
            CategoryItem("all", "全部", isSelected = true),
            CategoryItem("movie", "电影"),
            CategoryItem("tv", "电视剧"),
            CategoryItem("anime", "动漫"),
            CategoryItem("variety", "综艺"),
            CategoryItem("documentary", "纪录片")
        )
    }

    val recommendations = remember {
        listOf(
            VodItem("1", "default", "示例影片 1", null, "2024 · 动作"),
            VodItem("2", "default", "示例影片 2", null, "2024 · 喜剧"),
            VodItem("3", "default", "示例影片 3", null, "2024 · 科幻"),
            VodItem("4", "default", "示例影片 4", null, "2024 · 爱情"),
            VodItem("5", "default", "示例影片 5", null, "2024 · 悬疑"),
            VodItem("6", "default", "示例影片 6", null, "2024 · 恐怖")
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background)
    ) {
        // Header
        HomeHeader(
            siteName = "影視",
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onLiveClick = onLiveClick,
            onHistoryClick = onHistoryClick,
            onFavoritesClick = onFavoritesClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category chips
        CategoryChipRow(
            categories = categories,
            onCategoryClick = { category ->
                onCategoryClick(category.id)
            },
            modifier = Modifier.focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content rows
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                ContentRow(
                    title = "热门推荐",
                    items = recommendations,
                    onItemClick = { vodItem ->
                        onVodClick(vodItem.siteKey, vodItem.id)
                    }
                )
            }

            item {
                ContentRow(
                    title = "最近更新",
                    items = recommendations.shuffled(),
                    onItemClick = { vodItem ->
                        onVodClick(vodItem.siteKey, vodItem.id)
                    }
                )
            }

            item {
                ContentRow(
                    title = "电影精选",
                    items = recommendations.shuffled(),
                    onItemClick = { vodItem ->
                        onVodClick(vodItem.siteKey, vodItem.id)
                    }
                )
            }
        }
    }
}

/**
 * Home screen header with logo and quick access buttons.
 */
@Composable
private fun HomeHeader(
    siteName: String,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLiveClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo / Site name
        Text(
            text = siteName,
            style = TvTypography.HeadlineLarge,
            color = TvColors.Primary
        )

        // Quick access buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderButton(
                icon = Icons.Default.LiveTv,
                label = "直播",
                onClick = onLiveClick
            )
            HeaderButton(
                icon = Icons.Default.History,
                label = "历史",
                onClick = onHistoryClick
            )
            HeaderButton(
                icon = Icons.Default.Star,
                label = "收藏",
                onClick = onFavoritesClick
            )
            HeaderButton(
                icon = Icons.Default.Search,
                label = "搜索",
                onClick = onSearchClick
            )
            HeaderButton(
                icon = Icons.Default.Settings,
                label = "设置",
                onClick = onSettingsClick
            )
        }
    }
}

/**
 * Header button with icon and label.
 */
@Composable
private fun HeaderButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FocusableButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
            )
            Text(
                text = label,
                style = TvTypography.LabelMedium,
                color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
            )
        }
    }
}

/**
 * Loading indicator for home screen.
 */
@Composable
fun HomeLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = TvColors.Primary)
    }
}

/**
 * Error state for home screen.
 */
@Composable
fun HomeErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = TvTypography.BodyLarge,
                color = TvColors.TextSecondary
            )
            FocusableButton(onClick = onRetry) { isFocused ->
                Text(
                    text = "重试",
                    style = TvTypography.LabelLarge,
                    color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}
