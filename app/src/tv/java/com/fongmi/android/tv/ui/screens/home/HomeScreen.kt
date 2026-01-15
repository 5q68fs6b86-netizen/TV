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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.ui.components.CategoryChipRow
import com.fongmi.android.tv.ui.components.CategoryItem
import com.fongmi.android.tv.ui.components.ContentRow
import com.fongmi.android.tv.ui.components.FocusableButton
import com.fongmi.android.tv.ui.components.VodItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.HomeViewModel

/**
 * Home Screen for TV App
 * Displays categories, recommendations, and quick access buttons.
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
    val categoryContent by viewModel.categoryContent.collectAsState()
    val context = LocalContext.current

    // Double-click back to exit
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val backPressThreshold = 2000L // 2 seconds

    // Convert bean Classes to UI CategoryItems
    val categories = remember(uiState.categories) {
        uiState.categories.map { clazz ->
            CategoryItem(
                id = clazz.typeId ?: "",
                name = clazz.typeName ?: "",
                isSelected = clazz.typeId == uiState.selectedCategory?.typeId
            )
        }
    }

    // Convert bean Vods to UI VodItems
    fun vodToVodItem(vod: Vod): VodItem {
        return VodItem(
            id = vod.vodId ?: "",
            siteKey = uiState.currentSite?.key ?: "",
            title = vod.vodName ?: "",
            imageUrl = vod.vodPic,
            subtitle = vod.vodRemarks
        )
    }

    val focusRequester = remember { FocusRequester() }

    when {
        uiState.isLoading -> {
            HomeLoadingState()
        }
        uiState.error != null -> {
            HomeErrorState(
                message = uiState.error ?: "Unknown error",
                onRetry = { viewModel.refresh() }
            )
        }
        else -> {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TvColors.Background)
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                            event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastBackPressTime < backPressThreshold) {
                                // Double-click detected, exit app
                                (context as? Activity)?.finish()
                            } else {
                                // First click, show toast
                                lastBackPressTime = currentTime
                                Toast.makeText(context, "再按一次退出", Toast.LENGTH_SHORT).show()
                            }
                            true
                        } else {
                            false
                        }
                    }
            ) {
                // Header
                HomeHeader(
                    siteName = uiState.currentSite?.name ?: "影視",
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    onLiveClick = onLiveClick,
                    onHistoryClick = onHistoryClick,
                    onFavoritesClick = onFavoritesClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category chips
                if (categories.isNotEmpty()) {
                    CategoryChipRow(
                        categories = categories,
                        onCategoryClick = { category ->
                            // Find the original Class object
                            val clazz = uiState.categories.find { it.typeId == category.id }
                            if (clazz != null) {
                                viewModel.selectCategory(clazz)
                            }
                            onCategoryClick(category.id)
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Featured content from home
                    if (uiState.featuredContent.isNotEmpty()) {
                        item {
                            ContentRow(
                                title = "热门推荐",
                                items = uiState.featuredContent.take(10).map { vodToVodItem(it) },
                                onItemClick = { vodItem ->
                                    onVodClick(vodItem.siteKey, vodItem.id)
                                }
                            )
                        }
                    }

                    // Content for each category
                    uiState.categories.take(5).forEach { category ->
                        val content = categoryContent[category.typeId]
                        if (!content.isNullOrEmpty()) {
                            item {
                                ContentRow(
                                    title = category.typeName ?: "",
                                    items = content.take(10).map { vodToVodItem(it) },
                                    onItemClick = { vodItem ->
                                        onVodClick(vodItem.siteKey, vodItem.id)
                                    }
                                )
                            }
                        }
                    }

                    // If no content yet, show placeholder
                    if (uiState.featuredContent.isEmpty() && categoryContent.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
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
