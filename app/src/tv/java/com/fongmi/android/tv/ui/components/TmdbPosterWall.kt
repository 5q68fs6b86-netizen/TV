package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fongmi.android.tv.data.repository.TmdbItem
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * TMDB Poster Wall content - displays trending movies and TV shows
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && movies.isEmpty() && tvShows.isEmpty() -> {
                TmdbLoadingState()
            }
            error != null && movies.isEmpty() && tvShows.isEmpty() -> {
                TmdbErrorState(
                    message = error,
                    onRetry = onRetry
                )
            }
            else -> {
                TmdbContent(
                    movies = movies,
                    tvShows = tvShows,
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
private fun TmdbContent(
    movies: List<TmdbItem>,
    tvShows: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 24.dp)
    ) {
        // Featured banner (first movie)
        movies.firstOrNull()?.let { featured ->
            TmdbFeaturedBanner(
                item = featured,
                onClick = { onItemClick(featured) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Trending Movies Row
        if (movies.isNotEmpty()) {
            TmdbContentRow(
                title = "热门电影",
                items = movies,
                onItemClick = onItemClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trending TV Shows Row
        if (tvShows.isNotEmpty()) {
            TmdbContentRow(
                title = "热门剧集",
                items = tvShows,
                onItemClick = onItemClick
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

/**
 * Featured banner for the first TMDB item
 */
@Composable
private fun TmdbFeaturedBanner(
    item: TmdbItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.02f
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Backdrop image
            AsyncImage(
                model = item.backdropUrl ?: item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = item.title,
                    style = TvTypography.HeadlineLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        item.year?.let { append(it) }
                        if (item.voteAverage > 0) {
                            if (isNotEmpty()) append(" · ")
                            append("★ ${String.format("%.1f", item.voteAverage)}")
                        }
                    },
                    style = TvTypography.BodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                item.overview?.let { overview ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overview,
                        style = TvTypography.BodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Horizontal row of TMDB items
 */
@Composable
private fun TmdbContentRow(
    title: String,
    items: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit,
    cardWidth: Dp = 150.dp
) {
    Column {
        Text(
            text = title,
            style = TvTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                TmdbPosterCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    cardWidth = cardWidth
                )
            }
        }
    }
}

/**
 * Single TMDB poster card
 */
@Composable
fun TmdbPosterCard(
    item: TmdbItem,
    onClick: () -> Unit,
    cardWidth: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier.width(cardWidth),
        shape = RoundedCornerShape(12.dp),
        focusScale = 1.05f
    ) { isFocused ->
        Column {
            // Poster image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating badge
                if (item.voteAverage > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${String.format("%.1f", item.voteAverage)}",
                            style = TvTypography.LabelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                style = TvTypography.BodyMedium,
                color = if (isFocused) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Year
            item.year?.let { year ->
                Text(
                    text = year,
                    style = TvTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
