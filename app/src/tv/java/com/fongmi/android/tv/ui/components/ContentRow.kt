package com.fongmi.android.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * Content item for rows
 */
data class ContentItem(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val subtitle: String? = null,
    val badge: String? = null,
    val progress: Float? = null,
    val siteKey: String? = null,
    val vodId: String? = null
)

/**
 * Content Row for displaying horizontal scrollable content
 * 
 * @param title Row title
 * @param items Content items to display
 * @param onItemClick Called when an item is clicked
 * @param modifier Modifier for the row
 * @param onSeeAllClick Optional callback for "see all" button
 * @param cardWidth Card width
 * @param cardAspectRatio Card aspect ratio
 */
@Composable
fun ContentRow(
    title: String,
    items: List<ContentItem>,
    onItemClick: (ContentItem) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null,
    cardWidth: Dp = TvDimens.CardWidthMedium,
    cardAspectRatio: Float = 3f / 4f
) {
    if (items.isEmpty()) return

    val listState = rememberLazyListState()
    var focusedIndex by remember { mutableIntStateOf(-1) }
    val focusRequesters = remember(items.size) { List(items.size) { FocusRequester() } }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = TvTypography.TitleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (onSeeAllClick != null) {
                FocusableItem(
                    onClick = onSeeAllClick,
                    focusScale = 1.1f
                ) { isFocused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "查看全部",
                            style = TvTypography.LabelMedium,
                            color = if (isFocused) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isFocused) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content items
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->
                EnhancedVodCard(
                    title = item.title,
                    imageUrl = item.imageUrl,
                    subtitle = item.subtitle,
                    badge = item.badge,
                    progress = item.progress,
                    onClick = { onItemClick(item) },
                    width = cardWidth,
                    aspectRatio = cardAspectRatio,
                    focusRequester = focusRequesters.getOrNull(index),
                    onFocusChanged = { focused ->
                        if (focused) focusedIndex = index
                    }
                )
            }
        }
    }
}

/**
 * Wide Content Row (16:9 aspect ratio cards)
 */
@Composable
fun WideContentRow(
    title: String,
    items: List<ContentItem>,
    onItemClick: (ContentItem) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null
) {
    ContentRow(
        title = title,
        items = items,
        onItemClick = onItemClick,
        modifier = modifier,
        onSeeAllClick = onSeeAllClick,
        cardWidth = TvDimens.WideCardWidth,
        cardAspectRatio = 16f / 9f
    )
}

/**
 * Enhanced VodCard with 3D tilt effect
 */
@Composable
private fun EnhancedVodCard(
    title: String,
    imageUrl: String?,
    subtitle: String?,
    badge: String?,
    progress: Float?,
    onClick: () -> Unit,
    width: Dp,
    aspectRatio: Float,
    focusRequester: FocusRequester?,
    onFocusChanged: (Boolean) -> Unit
) {
    var isFocused by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused == 1) 1.08f else 1f,
        animationSpec = tween(200),
        label = "cardScale"
    )

    val rotationY by animateFloatAsState(
        targetValue = if (isFocused == 1) 2f else 0f,
        animationSpec = tween(200),
        label = "cardRotation"
    )

    VodCard(
        title = title,
        imageUrl = imageUrl,
        onClick = onClick,
        subtitle = subtitle,
        badge = badge,
        progress = progress,
        width = width,
        aspectRatio = aspectRatio,
        focusRequester = focusRequester,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.rotationY = rotationY
            }
            .onFocusChanged { state ->
                isFocused = if (state.isFocused) 1 else 0
                onFocusChanged(state.isFocused)
            }
    )
}
