package com.fongmi.android.tv.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.LiveViewModel

/**
 * Live TV Screen - displays live TV groups and channels
 */
@Composable
fun LiveScreen(
    viewModel: LiveViewModel = hiltViewModel(),
    onChannelClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
                .padding(top = TvDimens.ScreenPaddingVertical),
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
                                shape = CircleShape
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TvColors.Primary
                    )
                    Text(
                        text = "电视直播",
                        style = TvTypography.HeadlineLarge,
                        color = TvColors.TextPrimary
                    )
                }
            }

            // Refresh button
            FocusableItem(onClick = { viewModel.refresh() }) { isFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isFocused) TvColors.Primary else TvColors.Surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextSecondary
                    )
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = TvColors.Primary)
                        Text(
                            text = "加载直播源中...",
                            style = TvTypography.BodyMedium,
                            color = TvColors.TextSecondary
                        )
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = TvColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = uiState.error ?: "加载失败",
                            style = TvTypography.BodyLarge,
                            color = TvColors.TextSecondary
                        )
                        FocusableItem(onClick = { viewModel.refresh() }) { isFocused ->
                            Text(
                                text = "重试",
                                style = TvTypography.LabelLarge,
                                color = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary,
                                modifier = Modifier
                                    .background(
                                        color = if (isFocused) TvColors.Primary else TvColors.Surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            uiState.groups.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = TvColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "暂无直播源",
                            style = TvTypography.BodyLarge,
                            color = TvColors.TextSecondary
                        )
                        Text(
                            text = "请在设置中配置直播源",
                            style = TvTypography.BodyMedium,
                            color = TvColors.TextSecondary
                        )
                    }
                }
            }
            else -> {
                // Two-column layout: Groups | Channels
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
                ) {
                    // Groups column
                    LazyColumn(
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(uiState.groups) { group ->
                            GroupItem(
                                group = group,
                                isSelected = group == uiState.selectedGroup,
                                onClick = { viewModel.selectGroup(group) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Channels column
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(viewModel.getChannels()) { channel ->
                            ChannelItem(
                                channel = channel,
                                isSelected = channel == uiState.selectedChannel,
                                onClick = {
                                    viewModel.selectChannel(channel)
                                    val url = viewModel.getChannelUrl(channel)
                                    if (url.isNotEmpty()) {
                                        onChannelClick(url)
                                    }
                                }
                            )
                        }
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
private fun GroupItem(
    group: Group,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.2f)
                        else -> TvColors.Surface
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = group.name ?: "",
                style = TvTypography.TitleSmall,
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
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> TvColors.Primary.copy(alpha = 0.1f)
                        isSelected -> TvColors.Primary.copy(alpha = 0.05f)
                        else -> TvColors.Surface
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Channel logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TvColors.Background),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logo.isNullOrEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TvColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Channel info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = channel.name ?: "",
                    style = TvTypography.TitleSmall,
                    color = if (isFocused) TvColors.Primary else TvColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!channel.number.isNullOrEmpty()) {
                    Text(
                        text = "频道 ${channel.number}",
                        style = TvTypography.BodySmall,
                        color = TvColors.TextSecondary
                    )
                }
            }

            // Play indicator
            if (isFocused) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TvColors.Primary
                )
            }
        }
    }
}
