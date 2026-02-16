package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fongmi.android.tv.data.repository.TmdbCompany
import com.fongmi.android.tv.data.repository.TmdbItem
import com.fongmi.android.tv.data.repository.TmdbProvider
import com.fongmi.android.tv.ui.theme.TvTypography

// ========== Popular Showcase (备受欢迎 - 大卡片) ==========

/**
 * Showcase row for "Popular" categories with large decorative cards.
 * Shows: 备受欢迎·综艺 / 备受欢迎·剧集 / 备受欢迎·电影
 */
@Composable
fun PopularShowcaseRow(
    popularMovies: List<TmdbItem>,
    popularTv: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (popularMovies.isEmpty() && popularTv.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (popularTv.isNotEmpty()) {
            item {
                PopularShowcaseCard(
                    title = "备受欢迎·剧集",
                    subtitle = popularTv.firstOrNull()?.title ?: "",
                    backdropUrl = popularTv.firstOrNull()?.backdropUrl,
                    onClick = { popularTv.firstOrNull()?.let(onItemClick) }
                )
            }
        }
        if (popularMovies.isNotEmpty()) {
            item {
                PopularShowcaseCard(
                    title = "备受欢迎·电影",
                    subtitle = popularMovies.firstOrNull()?.title ?: "",
                    backdropUrl = popularMovies.firstOrNull()?.backdropUrl,
                    onClick = { popularMovies.firstOrNull()?.let(onItemClick) }
                )
            }
        }
    }
}

@Composable
private fun PopularShowcaseCard(
    title: String,
    subtitle: String,
    backdropUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
            .width(420.dp)
            .height(240.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.04f
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Backdrop
            AsyncImage(
                model = backdropUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Laurel wreath decoration + title
                Text(
                    text = "\uD83C\uDF3F $title \uD83C\uDF3F",
                    style = TvTypography.TitleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = TvTypography.BodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ========== Provider Row (播出平台) ==========

/**
 * Row of streaming provider cards (Netflix, Disney+, etc.)
 */
@Composable
fun ProviderRow(
    providers: List<TmdbProvider>,
    providerContent: Map<Int, List<TmdbItem>>,
    onProviderClick: (TmdbProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    if (providers.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "播出平台",
            style = TvTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(providers, key = { it.id }) { provider ->
                ProviderCard(
                    provider = provider,
                    backdropItems = providerContent[provider.id]?.take(3) ?: emptyList(),
                    onClick = { onProviderClick(provider) }
                )
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: TmdbProvider,
    backdropItems: List<TmdbItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
            .width(300.dp)
            .height(170.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.04f
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Background: tilted poster thumbnails
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy((-20).dp)
            ) {
                backdropItems.forEachIndexed { index, item ->
                    val rotation = when (index) {
                        0 -> -8f
                        1 -> 0f
                        else -> 8f
                    }
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 70.dp, height = 100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            // Dark overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 500f
                        )
                    )
            )

            // Provider name
            Text(
                text = provider.name,
                style = TvTypography.HeadlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
            )
        }
    }
}

// ========== Genre Browse Row (分类浏览) ==========

@Composable
fun GenreBrowseRow(
    genres: Map<Int, String>,
    genreContent: Map<Int, List<TmdbItem>>,
    onGenreClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayGenres = genres.entries
        .filter { genreContent.containsKey(it.key) }
        .take(8)

    if (displayGenres.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "分类浏览",
            style = TvTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayGenres.toList(), key = { it.key }) { (genreId, genreName) ->
                GenreBrowseCard(
                    name = genreName,
                    thumbnails = genreContent[genreId]?.take(2) ?: emptyList(),
                    onClick = { onGenreClick(genreId, genreName) }
                )
            }
        }
    }
}

@Composable
private fun GenreBrowseCard(
    name: String,
    thumbnails: List<TmdbItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
            .width(260.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.04f
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Thumbnail images on right
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                thumbnails.forEach { item ->
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 60.dp, height = 85.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            // Gradient for text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 400f
                        )
                    )
            )

            // Genre name
            Text(
                text = name,
                style = TvTypography.HeadlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
            )
        }
    }
}

// ========== Company Row (电影公司) ==========

@Composable
fun CompanyRow(
    companies: List<TmdbCompany>,
    companyContent: Map<Int, List<TmdbItem>>,
    onCompanyClick: (TmdbCompany) -> Unit,
    modifier: Modifier = Modifier
) {
    if (companies.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "电影公司",
            style = TvTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(companies, key = { it.id }) { company ->
                CompanyCard(
                    company = company,
                    backdropItem = companyContent[company.id]?.firstOrNull(),
                    onClick = { onCompanyClick(company) }
                )
            }
        }
    }
}

@Composable
private fun CompanyCard(
    company: TmdbCompany,
    backdropItem: TmdbItem?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
            .width(260.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.04f
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Background: first movie backdrop
            backdropItem?.let { item ->
                AsyncImage(
                    model = item.backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )
            }

            // Company name (as logo placeholder)
            Text(
                text = company.name,
                style = TvTypography.HeadlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}

// ========== Top Rated Showcase (高分榜单) ==========

/**
 * Side-by-side top rated cards for movies and TV shows.
 */
@Composable
fun TopRatedShowcaseRow(
    topRatedMovies: List<TmdbItem>,
    topRatedTv: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (topRatedMovies.isEmpty() && topRatedTv.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (topRatedTv.isNotEmpty()) {
            TopRatedCard(
                title = "高分剧集",
                items = topRatedTv.take(3),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (topRatedMovies.isNotEmpty()) {
            TopRatedCard(
                title = "高分电影",
                items = topRatedMovies.take(3),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopRatedCard(
    title: String,
    items: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = { items.firstOrNull()?.let(onItemClick) },
        modifier = modifier.height(280.dp),
        shape = RoundedCornerShape(16.dp),
        focusScale = 1.02f
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header with laurel
                Text(
                    text = "\uD83C\uDF3F $title \uD83C\uDF3F",
                    style = TvTypography.TitleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Poster thumbnails
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items.take(3).forEach { item ->
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(2f / 3f)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Top 3 list
                items.take(3).forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = TvTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = item.title,
                            style = TvTypography.BodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ========== TMDB Content Row (通用横向内容行) ==========

/**
 * Horizontal scrolling row of TMDB items with title header.
 */
@Composable
fun TmdbContentRow(
    title: String,
    items: List<TmdbItem>,
    onItemClick: (TmdbItem) -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 150.dp,
    useBackdrop: Boolean = false
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = TvTypography.TitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                TmdbPosterCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    cardWidth = cardWidth,
                    useBackdrop = useBackdrop
                )
            }
        }
    }
}

/**
 * Single TMDB poster card (reusable)
 */
@Composable
fun TmdbPosterCard(
    item: TmdbItem,
    onClick: () -> Unit,
    cardWidth: Dp = 150.dp,
    useBackdrop: Boolean = false,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier.width(cardWidth),
        shape = RoundedCornerShape(12.dp),
        focusScale = 1.05f
    ) { isFocused ->
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (useBackdrop) 16f / 9f else 2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = if (useBackdrop) (item.backdropUrl ?: item.posterUrl) else item.posterUrl,
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
                            text = String.format("%.1f", item.voteAverage),
                            style = TvTypography.LabelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = TvTypography.BodyMedium,
                color = if (isFocused) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Year + type
            val subtitle = buildString {
                item.year?.let { append(it) }
                if (item.mediaType == "tv") {
                    if (isNotEmpty()) append(" · ")
                    append(if (item.genreIds.contains(16)) "动画" else "剧集")
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = TvTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
