package com.fongmi.android.tv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fongmi.android.tv.ui.theme.CommonColors
import com.fongmi.android.tv.ui.theme.TvTypography
import kotlinx.coroutines.delay

/**
 * Banner item data
 */
data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val imageUrl: String?,
    val siteKey: String? = null,
    val vodId: String? = null
)

/**
 * Hero Banner Carousel for TV Home Screen
 * 
 * @param items Banner items to display
 * @param onItemClick Called when banner item is clicked
 * @param modifier Modifier for the banner
 * @param height Banner height
 * @param autoScrollInterval Auto scroll interval in milliseconds (0 to disable)
 */
@Composable
fun HeroBanner(
    items: List<BannerItem>,
    onItemClick: (BannerItem) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    autoScrollInterval: Long = 5000L
) {
    if (items.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentItem = items.getOrNull(currentIndex) ?: return

    // Auto scroll effect
    if (autoScrollInterval > 0 && items.size > 1) {
        LaunchedEffect(currentIndex, items.size) {
            delay(autoScrollInterval)
            currentIndex = (currentIndex + 1) % items.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Blurred background image
        AsyncImage(
            model = currentItem.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
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

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text content (left side)
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Subtitle/Category
                if (currentItem.subtitle != null) {
                    Text(
                        text = currentItem.subtitle,
                        style = TvTypography.LabelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Title
                Text(
                    text = currentItem.title,
                    style = TvTypography.DisplaySmall.copy(fontWeight = FontWeight.Bold),
                    color = CommonColors.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (currentItem.description != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentItem.description,
                        style = TvTypography.BodyMedium,
                        color = CommonColors.White70,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Play button
                FocusableItem(
                    onClick = { onItemClick(currentItem) },
                    shape = RoundedCornerShape(8.dp)
                ) { isFocused ->
                    Button(
                        onClick = { onItemClick(currentItem) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFocused) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                CommonColors.White,
                            contentColor = if (isFocused) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                CommonColors.Black
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "播放",
                            style = TvTypography.LabelLarge
                        )
                    }
                }
            }

            // Featured image (right side)
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                AsyncImage(
                    model = currentItem.imageUrl,
                    contentDescription = currentItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(height - 80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }

        // Page indicators
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { index, _ ->
                    PageIndicator(
                        isSelected = index == currentIndex,
                        onClick = { currentIndex = index }
                    )
                }
            }
        }
    }
}

/**
 * Page indicator dot
 */
@Composable
private fun PageIndicator(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val width by animateFloatAsState(
        targetValue = if (isSelected) 24f else 8f,
        animationSpec = tween(300),
        label = "indicatorWidth"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.5f,
        animationSpec = tween(300),
        label = "indicatorAlpha"
    )

    Box(
        modifier = Modifier
            .width(width.dp)
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}
