package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fongmi.android.tv.ui.theme.CommonColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvShapes
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * TV Video Card Component
 * Displays video poster with title, optimized for TV viewing and focus navigation.
 *
 * @param title Video title
 * @param imageUrl Poster image URL
 * @param onClick Called when card is clicked
 * @param modifier Modifier for the card
 * @param subtitle Optional subtitle (year, type, etc.)
 * @param badge Optional badge text (HD, 4K, etc.)
 * @param progress Optional playback progress (0.0 - 1.0)
 * @param width Card width
 * @param aspectRatio Card aspect ratio (default 3:4 for posters)
 * @param focusRequester Optional focus requester
 */
@Composable
fun VodCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    progress: Float? = null,
    width: Dp = TvDimens.CardWidthMedium,
    aspectRatio: Float = 3f / 4f,
    focusRequester: FocusRequester? = null
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier.width(width),
        shape = TvShapes.Card,
        focusRequester = focusRequester
    ) { isFocused ->
        Column {
            // Poster image with overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(TvShapes.Card)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                // Poster image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CommonColors.Black70
                                )
                            )
                        )
                )

                // Badge (HD, 4K, etc.)
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(TvShapes.Badge)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = TvTypography.LabelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Progress bar at bottom
                if (progress != null && progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(CommonColors.White30)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // Title and subtitle below image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = TvTypography.CardTitle,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = TvTypography.CardSubtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Wide video card for landscape posters (16:9 ratio).
 */
@Composable
fun WideVodCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    progress: Float? = null,
    width: Dp = TvDimens.WideCardWidth,
    focusRequester: FocusRequester? = null
) {
    VodCard(
        title = title,
        imageUrl = imageUrl,
        onClick = onClick,
        modifier = modifier,
        subtitle = subtitle,
        badge = badge,
        progress = progress,
        width = width,
        aspectRatio = 16f / 9f,
        focusRequester = focusRequester
    )
}

/**
 * Small video card for compact lists.
 */
@Composable
fun SmallVodCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    progress: Float? = null,
    focusRequester: FocusRequester? = null
) {
    VodCard(
        title = title,
        imageUrl = imageUrl,
        onClick = onClick,
        modifier = modifier,
        badge = badge,
        progress = progress,
        width = TvDimens.CardWidthSmall,
        focusRequester = focusRequester
    )
}

/**
 * Large video card for featured content.
 */
@Composable
fun LargeVodCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    progress: Float? = null,
    focusRequester: FocusRequester? = null
) {
    VodCard(
        title = title,
        imageUrl = imageUrl,
        onClick = onClick,
        modifier = modifier,
        subtitle = subtitle,
        badge = badge,
        progress = progress,
        width = TvDimens.CardWidthLarge,
        focusRequester = focusRequester
    )
}
