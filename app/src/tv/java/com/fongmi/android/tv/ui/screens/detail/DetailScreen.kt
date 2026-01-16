package com.fongmi.android.tv.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.ui.components.FocusableButton
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.DetailViewModel
import com.fongmi.android.tv.ui.viewmodel.PlayUrlState

/**
 * Detail Screen for video information and episode selection
 */
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onPlayClick: (String, Map<String, String>?, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFlag by viewModel.selectedFlag.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val playUrlState by viewModel.playUrlState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val history by viewModel.history.collectAsState()

    // Handle play URL ready
    LaunchedEffect(playUrlState) {
        if (playUrlState is PlayUrlState.Ready) {
            val state = playUrlState as PlayUrlState.Ready
            onPlayClick(state.url, state.headers, state.vodName, state.episodeName)
            viewModel.clearPlayState()
        }
    }

    val focusRequester = remember { FocusRequester() }

    when {
        uiState.isLoading -> {
            DetailLoadingState()
        }
        uiState.error != null -> {
            DetailErrorState(
                message = uiState.error ?: "Unknown error",
                onRetry = { viewModel.retry() },
                onBack = onBackClick
            )
        }
        uiState.vod != null -> {
            val vod = uiState.vod!!

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Blurred background from poster
                AsyncImage(
                    model = vod.vodPic,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(60.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Dark overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TvColors.Background.copy(alpha = 0.95f),
                                    TvColors.Background.copy(alpha = 0.85f),
                                    TvColors.Background.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
                    .padding(top = TvDimens.ScreenPaddingVertical)
            ) {
                // Top section: Poster + Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Poster
                    AsyncImage(
                        model = vod.vodPic,
                        contentDescription = vod.vodName,
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    // Info column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Title
                            Text(
                                text = vod.vodName ?: "",
                                style = TvTypography.HeadlineLarge,
                                color = TvColors.TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Meta info
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (!vod.vodYear.isNullOrEmpty()) {
                                    MetaChip(text = vod.vodYear)
                                }
                                if (!vod.vodArea.isNullOrEmpty()) {
                                    MetaChip(text = vod.vodArea)
                                }
                                if (!vod.vodRemarks.isNullOrEmpty()) {
                                    MetaChip(text = vod.vodRemarks)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Description
                            Text(
                                text = vod.vodContent?.replace("<[^>]*>".toRegex(), "") ?: "",
                                style = TvTypography.BodyMedium,
                                color = TvColors.TextSecondary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Play button - shows "Continue" if has history
                            val hasHistory = history != null && (history?.position ?: 0) > 0
                            val progressText = viewModel.getProgressText()

                            FocusableButton(
                                onClick = { viewModel.play() },
                                modifier = Modifier.focusRequester(focusRequester)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                                    )
                                    Column {
                                        Text(
                                            text = when {
                                                playUrlState is PlayUrlState.Loading -> "加载中..."
                                                hasHistory -> "继续播放"
                                                else -> "立即播放"
                                            },
                                            style = TvTypography.LabelLarge,
                                            color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                                        )
                                        if (hasHistory && progressText.isNotEmpty()) {
                                            Text(
                                                text = progressText,
                                                style = TvTypography.LabelSmall,
                                                color = if (isFocused) TvColors.OnPrimary.copy(alpha = 0.7f) else TvColors.TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Favorite button
                            FocusableButton(
                                onClick = { viewModel.toggleFavorite() }
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                                    )
                                    Text(
                                        text = if (isFavorite) "已收藏" else "收藏",
                                        style = TvTypography.LabelLarge,
                                        color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Play sources (Flags)
                val flags = viewModel.getFlags()
                if (flags.isNotEmpty()) {
                    Text(
                        text = "播放源",
                        style = TvTypography.TitleMedium,
                        color = TvColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(flags) { flag ->
                            FlagChip(
                                flag = flag,
                                isSelected = flag == selectedFlag,
                                onClick = { viewModel.selectFlag(flag) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Episodes
                val episodes = viewModel.getEpisodes()
                if (episodes.isNotEmpty()) {
                    Text(
                        text = "选集",
                        style = TvTypography.TitleMedium,
                        color = TvColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(episodes) { episode ->
                            EpisodeCard(
                                episode = episode,
                                isSelected = episode == selectedEpisode,
                                onClick = {
                                    viewModel.selectEpisode(episode)
                                    viewModel.play()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = TvColors.Surface,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = TvTypography.LabelSmall,
            color = TvColors.TextSecondary
        )
    }
}

@Composable
private fun FlagChip(
    flag: Flag,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(
        onClick = onClick
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.3f)
                        else -> TvColors.Surface
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 2.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = flag.show ?: flag.flag ?: "",
                style = TvTypography.LabelMedium,
                color = when {
                    isFocused -> TvColors.OnPrimary
                    isSelected -> TvColors.Primary
                    else -> TvColors.TextPrimary
                }
            )
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(
        onClick = onClick
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.3f)
                        else -> TvColors.Surface
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 2.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.name ?: "",
                style = TvTypography.LabelMedium,
                color = when {
                    isFocused -> TvColors.OnPrimary
                    isSelected -> TvColors.Primary
                    else -> TvColors.TextPrimary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DetailLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = TvColors.Primary)
            Text(
                text = "加载中...",
                style = TvTypography.BodyMedium,
                color = TvColors.TextSecondary
            )
        }
    }
}

@Composable
fun DetailErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = TvTypography.BodyLarge,
                color = TvColors.TextSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FocusableButton(onClick = onRetry) { isFocused ->
                    Text(
                        text = "重试",
                        style = TvTypography.LabelLarge,
                        color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                FocusableButton(onClick = onBack) { isFocused ->
                    Text(
                        text = "返回",
                        style = TvTypography.LabelLarge,
                        color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
