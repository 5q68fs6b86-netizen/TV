package com.fongmi.android.tv.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fongmi.android.tv.data.repository.TmdbItem
import com.fongmi.android.tv.ui.theme.TvTypography
import kotlinx.coroutines.delay

/**
 * TMDB Hero Banner with auto-carousel for the home screen.
 * Full-screen backdrop + title + date + genre tags + overview + rating + page indicators.
 */
@Composable
fun TmdbHeroBanner(
    items: List<TmdbItem>,
    genres: Map<Int, String>,
    onItemClick: (TmdbItem) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollInterval: Long = 6000L
) {
    if (items.isEmpty()) return

    val displayItems = items.take(10)
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentItem = displayItems.getOrNull(currentIndex) ?: return

    // Auto scroll
    if (autoScrollInterval > 0 && displayItems.size > 1) {
        LaunchedEffect(currentIndex, displayItems.size) {
            delay(autoScrollInterval)
            currentIndex = (currentIndex + 1) % displayItems.size
        }
    }

    FocusableItem(
        onClick = { onItemClick(currentItem) },
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
        shape = RoundedCornerShape(0.dp),
        focusScale = 1.0f,
        focusBorderWidth = 0.dp,
        focusElevation = 0.dp
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred backdrop image with crossfade
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(tween(800)) togetherWith fadeOut(tween(800))
                },
                label = "heroBannerCrossfade"
            ) { index ->
                val item = displayItems.getOrNull(index) ?: return@AnimatedContent
                AsyncImage(
                    model = item.backdropUrlLarge ?: item.backdropUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(16.dp)
                )
            }

            // Left gradient for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 900f
                        )
                    )
            )

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Content info (left side)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 80.dp, end = 400.dp)
            ) {
                // Logo or Title
                if (currentItem.logoUrl != null) {
                    AsyncImage(
                        model = currentItem.logoUrl,
                        contentDescription = currentItem.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .heightIn(max = 80.dp)
                            .fillMaxWidth(0.5f)
                    )
                } else {
                    Text(
                        text = currentItem.title,
                        style = TvTypography.DisplaySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date + Genre tags row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Release date
                    currentItem.releaseDate?.let { date ->
                        Text(
                            text = date,
                            style = TvTypography.BodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Genre tags
                    val genreNames = currentItem.genreNames(genres).take(3)
                    if (genreNames.isNotEmpty()) {
                        Text(
                            text = "·",
                            style = TvTypography.BodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = genreNames.joinToString(", "),
                            style = TvTypography.BodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Overview
                currentItem.overview?.let { overview ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overview,
                        style = TvTypography.BodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rating badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentItem.voteAverage > 0) {
                        RatingBadge(
                            label = "TMDB",
                            score = String.format("%.1f", currentItem.voteAverage),
                            color = Color(0xFF01D277)
                        )
                    }
                }
            }

            // Page indicators (bottom center)
            if (displayItems.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayItems.forEachIndexed { index, _ ->
                        HeroBannerIndicator(isSelected = index == currentIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBadge(
    label: String,
    score: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label $score",
            style = TvTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun HeroBannerIndicator(isSelected: Boolean) {
    val width by animateFloatAsState(
        targetValue = if (isSelected) 20f else 6f,
        animationSpec = tween(300),
        label = "indicatorWidth"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.4f,
        animationSpec = tween(300),
        label = "indicatorAlpha"
    )

    Box(
        modifier = Modifier
            .width(width.dp)
            .height(6.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha))
    )
}
