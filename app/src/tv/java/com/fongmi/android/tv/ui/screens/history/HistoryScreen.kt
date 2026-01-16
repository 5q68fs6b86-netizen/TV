package com.fongmi.android.tv.ui.screens.history

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.HistoryViewModel

/**
 * History Screen - displays watch history
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onItemClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background)
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
                                color = if (isFocused) TvColors.Primary else TvColors.Surface,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TvColors.Primary
                    )
                    Text(
                        text = "观看历史",
                        style = TvTypography.HeadlineLarge,
                        color = TvColors.TextPrimary
                    )
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TvColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "暂无观看记录",
                            style = TvTypography.BodyLarge,
                            color = TvColors.TextSecondary
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.items, key = { it.key }) { item ->
                        HistoryItem(
                            history = item,
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
private fun HistoryItem(
    history: History,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) TvColors.Primary.copy(alpha = 0.1f) else TvColors.Surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 68.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = history.vodPic,
                    contentDescription = history.vodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play progress overlay
                if (history.duration > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { (history.position.toFloat() / history.duration).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = TvColors.Primary,
                            trackColor = TvColors.Background.copy(alpha = 0.5f)
                        )
                    }
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = TvColors.Background.copy(alpha = 0.7f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TvColors.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = history.vodName ?: "",
                    style = TvTypography.TitleMedium,
                    color = if (isFocused) TvColors.Primary else TvColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!history.vodRemarks.isNullOrEmpty()) {
                        Text(
                            text = history.vodRemarks,
                            style = TvTypography.BodySmall,
                            color = TvColors.TextSecondary
                        )
                    }

                    if (history.duration > 0) {
                        val progress = ((history.position.toFloat() / history.duration) * 100).toInt()
                        Text(
                            text = "已观看 $progress%",
                            style = TvTypography.BodySmall,
                            color = TvColors.Primary
                        )
                    }
                }

                Text(
                    text = history.siteName ?: "",
                    style = TvTypography.BodySmall,
                    color = TvColors.TextSecondary
                )
            }

            // Delete button
            FocusableItem(onClick = onDelete) { deleteIsFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (deleteIsFocused) TvColors.Error else TvColors.Background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (deleteIsFocused) TvColors.OnPrimary else TvColors.TextSecondary
                    )
                }
            }
        }
    }
}
