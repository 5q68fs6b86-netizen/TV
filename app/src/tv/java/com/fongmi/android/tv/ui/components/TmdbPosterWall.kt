package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.data.repository.TmdbCompany
import com.fongmi.android.tv.data.repository.TmdbItem
import com.fongmi.android.tv.data.repository.TmdbProvider
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * TMDB Poster Wall - Full home screen content with all sections.
 * Matches the reference design with ~12 content blocks.
 */
@Composable
fun TmdbPosterWall(
    movies: List<TmdbItem>,
    tvShows: List<TmdbItem>,
    isLoading: Boolean,
    error: String?,
    isQuickSearching: Boolean,
    quickSearchTitle: String?,
    onItemClick: (TmdbItem) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    // New data fields
    trendingToday: List<TmdbItem> = emptyList(),
    trendingWeek: List<TmdbItem> = emptyList(),
    popularMovies: List<TmdbItem> = emptyList(),
    popularTv: List<TmdbItem> = emptyList(),
    nowPlaying: List<TmdbItem> = emptyList(),
    trendingAnime: List<TmdbItem> = emptyList(),
    topRatedMovies: List<TmdbItem> = emptyList(),
    topRatedTv: List<TmdbItem> = emptyList(),
    genres: Map<Int, String> = emptyMap(),
    providers: List<TmdbProvider> = emptyList(),
    companies: List<TmdbCompany> = emptyList(),
    providerContent: Map<Int, List<TmdbItem>> = emptyMap(),
    companyContent: Map<Int, List<TmdbItem>> = emptyMap(),
    genreContent: Map<Int, List<TmdbItem>> = emptyMap()
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && trendingToday.isEmpty() && movies.isEmpty() -> {
                TmdbLoadingState()
            }
            error != null && trendingToday.isEmpty() && movies.isEmpty() -> {
                TmdbErrorState(
                    message = error,
                    onRetry = onRetry
                )
            }
            else -> {
                TmdbFullContent(
                    trendingToday = trendingToday,
                    trendingWeek = trendingWeek,
                    popularMovies = popularMovies,
                    popularTv = popularTv,
                    nowPlaying = nowPlaying,
                    trendingAnime = trendingAnime,
                    topRatedMovies = topRatedMovies,
                    topRatedTv = topRatedTv,
                    genres = genres,
                    providers = providers,
                    companies = companies,
                    providerContent = providerContent,
                    companyContent = companyContent,
                    genreContent = genreContent,
                    onItemClick = onItemClick
                )
            }
        }

        // Quick search overlay
        if (isQuickSearching) {
            QuickSearchOverlay(title = quickSearchTitle)
        }
    }
}

@Composable
private fun TmdbFullContent(
    trendingToday: List<TmdbItem>,
    trendingWeek: List<TmdbItem>,
    popularMovies: List<TmdbItem>,
    popularTv: List<TmdbItem>,
    nowPlaying: List<TmdbItem>,
    trendingAnime: List<TmdbItem>,
    topRatedMovies: List<TmdbItem>,
    topRatedTv: List<TmdbItem>,
    genres: Map<Int, String>,
    providers: List<TmdbProvider>,
    companies: List<TmdbCompany>,
    providerContent: Map<Int, List<TmdbItem>>,
    companyContent: Map<Int, List<TmdbItem>>,
    genreContent: Map<Int, List<TmdbItem>>,
    onItemClick: (TmdbItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 1. Hero Banner Carousel (today's trending top 10)
        TmdbHeroBanner(
            items = trendingToday,
            genres = genres,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 今日趋势
        TmdbContentRow(
            title = "今日趋势",
            items = trendingToday,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3. 本周趋势
        TmdbContentRow(
            title = "本周趋势",
            items = trendingWeek,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4. 备受欢迎 (Popular showcase cards)
        PopularShowcaseRow(
            popularMovies = popularMovies,
            popularTv = popularTv,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4b. Popular movies row
        TmdbContentRow(
            title = "热门电影",
            items = popularMovies,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4c. Popular TV row
        TmdbContentRow(
            title = "热门剧集",
            items = popularTv,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 5. 正在热映
        TmdbContentRow(
            title = "正在热映",
            items = nowPlaying,
            onItemClick = onItemClick,
            useBackdrop = true,
            cardWidth = 220.dp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 6. 今日动漫
        TmdbContentRow(
            title = "今日动漫",
            items = trendingAnime,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 7. 播出平台
        ProviderRow(
            providers = providers,
            providerContent = providerContent,
            onProviderClick = { provider ->
                // Click the first item from this provider
                providerContent[provider.id]?.firstOrNull()?.let(onItemClick)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 8. 分类浏览
        GenreBrowseRow(
            genres = genres,
            genreContent = genreContent,
            onGenreClick = { genreId, _ ->
                // Click first item from this genre
                genreContent[genreId]?.firstOrNull()?.let(onItemClick)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 9. 电影公司
        CompanyRow(
            companies = companies,
            companyContent = companyContent,
            onCompanyClick = { company ->
                companyContent[company.id]?.firstOrNull()?.let(onItemClick)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 10. 高分榜单 (Top Rated)
        TopRatedShowcaseRow(
            topRatedMovies = topRatedMovies,
            topRatedTv = topRatedTv,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

/**
 * Loading state for TMDB content
 */
@Composable
private fun TmdbLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "加载热门内容...",
                style = TvTypography.BodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state for TMDB content
 */
@Composable
private fun TmdbErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = TvTypography.BodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            FocusableButton(
                onClick = onRetry,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) { isFocused ->
                Text(
                    text = "重试",
                    style = TvTypography.LabelLarge,
                    color = if (isFocused) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Overlay shown during quick search
 */
@Composable
private fun QuickSearchOverlay(title: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在搜索「${title ?: ""}」...",
                style = TvTypography.BodyLarge,
                color = Color.White
            )
        }
    }
}
