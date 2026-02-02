package com.fongmi.android.tv.ui.screens.home

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.ui.components.NavItem
import com.fongmi.android.tv.ui.components.TmdbPosterWall
import com.fongmi.android.tv.ui.components.TvNavigationRail
import com.fongmi.android.tv.ui.components.defaultNavItems
import com.fongmi.android.tv.ui.dialog.HomeModeDialog
import com.fongmi.android.tv.ui.state.HomeMode
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.HomeViewModel

/**
 * Redesigned Home Screen with dual mode support:
 * - TMDB poster wall mode
 * - Site source content mode
 * Side Navigation + Banner + Content Rows
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: (String) -> Unit,
    onVodClick: (String, String) -> Unit,
    onTmdbVodClick: (String, String) -> Unit = onVodClick,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLiveClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

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
                when (item.route) {
                    "home" -> {
                        if (selectedNavIndex == 0) {
                            // Already on home, show mode dialog
                            viewModel.showModeDialog()
                        } else {
                            selectedNavIndex = 0
                        }
                    }
                    "search" -> {
                        selectedNavIndex = index
                        onSearchClick()
                    }
                    "live" -> {
                        selectedNavIndex = index
                        onLiveClick()
                    }
                    "favorites" -> {
                        selectedNavIndex = index
                        onFavoritesClick()
                    }
                    "history" -> {
                        selectedNavIndex = index
                        onHistoryClick()
                    }
                    "settings" -> {
                        selectedNavIndex = index
                        onSettingsClick()
                    }
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
                uiState.isLoading && uiState.homeMode == HomeMode.SITE -> {
                    HomeLoadingState()
                }
                uiState.error != null && uiState.homeMode == HomeMode.SITE -> {
                    HomeErrorState(
                        message = uiState.error ?: "未知错误",
                        onRetry = { viewModel.loadHomeContent() }
                    )
                }
                else -> {
                    // Dual mode content
                    when (uiState.homeMode) {
                        HomeMode.TMDB -> {
                            TmdbPosterWall(
                                movies = uiState.tmdbMovies,
                                tvShows = uiState.tmdbTvShows,
                                isLoading = uiState.isTmdbLoading,
                                error = uiState.tmdbError,
                                isQuickSearching = uiState.isQuickSearching,
                                quickSearchTitle = uiState.quickSearchTitle,
                                onItemClick = { item ->
                                    viewModel.onTmdbItemClick(
                                        item = item,
                                        onNavigateToDetail = { siteKey, vodId ->
                                            onTmdbVodClick(siteKey, vodId)
                                        },
                                        onNotFound = { title ->
                                            Toast.makeText(
                                                context,
                                                "未找到「$title」的资源",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                onRetry = { viewModel.loadTmdbContent() }
                            )
                        }
                        HomeMode.SITE -> {
                            SiteHomeContent(
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
    }

    // Home mode dialog
    if (uiState.showModeDialog) {
        HomeModeDialog(
            currentMode = uiState.homeMode,
            currentSite = uiState.currentSite,
            sites = viewModel.getSites(),
            onModeChange = { mode -> viewModel.switchHomeMode(mode) },
            onSiteChange = { site -> viewModel.changeSite(site) },
            onDismiss = { viewModel.dismissModeDialog() }
        )
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
