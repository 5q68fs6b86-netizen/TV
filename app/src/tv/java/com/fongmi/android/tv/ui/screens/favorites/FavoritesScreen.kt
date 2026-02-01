package com.fongmi.android.tv.ui.screens.favorites

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.FavoritesViewModel

/**
 * Favorites Screen - displays user's favorite videos
 */
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onItemClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FocusableItem(
                    onClick = onBackClick,
                    modifier = Modifier.focusRequester(focusRequester)
                ) { isFocused ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "我的收藏",
                        style = TvTypography.HeadlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.items.isNotEmpty()) {
                        Text(
                            text = "(${uiState.items.size})",
                            style = TvTypography.TitleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Clear all button
            if (uiState.items.isNotEmpty()) {
                FocusableItem(onClick = { viewModel.clearAll() }) { isFocused ->
                    Row(
                        modifier = Modifier
                            .background(
                                color = if (isFocused) TvColors.Error else TvColors.Surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = if (isFocused) TvColors.OnPrimary else TvColors.TextSecondary
                        )
                        Text(
                            text = "清空",
                            style = TvTypography.LabelMedium,
                            color = if (isFocused) TvColors.OnPrimary else TvColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TvColors.Primary)
                }
            }
            uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TvColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "暂无收藏",
                            style = TvTypography.BodyLarge,
                            color = TvColors.TextSecondary
                        )
                        Text(
                            text = "在详情页点击收藏按钮添加",
                            style = TvTypography.BodyMedium,
                            color = TvColors.TextSecondary
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.items, key = { it.key }) { item ->
                        FavoriteCard(
                            keep = item,
                            onClick = { onItemClick(item.siteKey, item.vodId) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun FavoriteCard(
    keep: Keep,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Column(
            modifier = Modifier
                .background(
                    color = if (isFocused) TvColors.Primary.copy(alpha = 0.1f) else TvColors.Surface,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Poster with delete button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = keep.vodPic,
                    contentDescription = keep.vodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    TvColors.Background.copy(alpha = 0f),
                                    TvColors.Background.copy(alpha = 0.5f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Delete button
                FocusableItem(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) { deleteIsFocused ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (deleteIsFocused) TvColors.Error else TvColors.Background.copy(alpha = 0.7f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (deleteIsFocused) TvColors.OnPrimary else TvColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Focus border
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(TvColors.Primary.copy(alpha = 0.2f))
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = keep.vodName ?: "",
                    style = TvTypography.TitleSmall,
                    color = if (isFocused) TvColors.Primary else TvColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = keep.siteName ?: "",
                    style = TvTypography.BodySmall,
                    color = TvColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
