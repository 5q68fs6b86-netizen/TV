package com.fongmi.android.tv.ui.screens.player

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * Enhanced Player Screen with episode sidebar and ViewModel
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Player state
    var isPlaying by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showEpisodeSidebar by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Episode selection state
    var selectedFlagIndex by remember { mutableIntStateOf(uiState.currentFlagIndex) }

    val sidebarFocusRequester = remember { FocusRequester() }

    // Current URL (tracks changes from ViewModel)
    val currentUrl = uiState.url

    // Create ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Update media when URL changes
    LaunchedEffect(currentUrl) {
        if (currentUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(currentUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            isLoading = true
        }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isLoading = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration
                }
                // Auto play next on end
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.playNext()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Update progress periodically
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
            delay(1000)
        }
    }

    // Auto-hide controls
    LaunchedEffect(showControls, showEpisodeSidebar) {
        if (showControls && !showEpisodeSidebar) {
            delay(5000)
            showControls = false
        }
    }

    // Focus sidebar when shown
    LaunchedEffect(showEpisodeSidebar) {
        if (showEpisodeSidebar) {
            delay(100)
            sidebarFocusRequester.requestFocus()
        }
    }

    // Update selectedFlagIndex when uiState changes
    LaunchedEffect(uiState.currentFlagIndex) {
        selectedFlagIndex = uiState.currentFlagIndex
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (showEpisodeSidebar) {
                                // Let focus handle it
                                false
                            } else if (showControls) {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                true
                            } else {
                                showControls = true
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (showEpisodeSidebar) {
                                showEpisodeSidebar = false
                                true
                            } else {
                                showControls = true
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                                true
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!showEpisodeSidebar) {
                                showControls = true
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!showEpisodeSidebar) {
                                showControls = true
                            }
                            // Let focus system handle vertical navigation
                            false
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            viewModel.playNext()
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            viewModel.playPrevious()
                            true
                        }
                        KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_INFO -> {
                            // Show episode sidebar
                            if (uiState.hasEpisodes) {
                                showEpisodeSidebar = !showEpisodeSidebar
                                showControls = true
                            }
                            true
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            when {
                                showEpisodeSidebar -> {
                                    showEpisodeSidebar = false
                                    true
                                }
                                showControls -> {
                                    showControls = false
                                    true
                                }
                                else -> {
                                    viewModel.clearState()
                                    onBackClick()
                                    true
                                }
                            }
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // Video player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator (initial or episode change)
        if (isLoading || uiState.isLoadingEpisode) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = TvColors.Primary)
                    if (uiState.isLoadingEpisode) {
                        Text(
                            text = "加载中...",
                            style = TvTypography.BodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Error message
        uiState.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .background(
                        color = Color.Red.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = error,
                    style = TvTypography.BodyMedium,
                    color = Color.White
                )
            }
        }

        // Controls overlay
        if (showControls) {
            PlayerControlsOverlay(
                vodName = uiState.vodName,
                episodeName = uiState.episodeName,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                progress = progress,
                hasEpisodes = uiState.hasEpisodes,
                hasPrevious = uiState.currentEpisodeIndex > 0,
                hasNext = uiState.currentFlag?.episodes?.let {
                    uiState.currentEpisodeIndex < it.size - 1
                } ?: false,
                onPlayPause = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekBack = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                },
                onSeekForward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                },
                onPrevious = { viewModel.playPrevious() },
                onNext = { viewModel.playNext() },
                onShowEpisodes = {
                    showEpisodeSidebar = true
                },
                onBackClick = {
                    viewModel.clearState()
                    onBackClick()
                }
            )
        }

        // Episode sidebar
        AnimatedVisibility(
            visible = showEpisodeSidebar && uiState.hasEpisodes,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            EpisodeSidebar(
                flags = uiState.flags,
                currentFlagIndex = selectedFlagIndex,
                currentEpisodeIndex = uiState.currentEpisodeIndex,
                focusRequester = sidebarFocusRequester,
                onFlagSelect = { index ->
                    selectedFlagIndex = index
                    viewModel.selectFlag(index)
                },
                onEpisodeSelect = { episode ->
                    showEpisodeSidebar = false
                    viewModel.selectEpisode(episode)
                },
                onClose = {
                    showEpisodeSidebar = false
                }
            )
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    vodName: String,
    episodeName: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    progress: Float,
    hasEpisodes: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShowEpisodes: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        // Top bar - Title and actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vodName,
                    style = TvTypography.HeadlineMedium,
                    color = Color.White
                )
                if (episodeName.isNotEmpty() && episodeName != "直播") {
                    Text(
                        text = episodeName,
                        style = TvTypography.BodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Top right actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (hasEpisodes) {
                    TopActionButton(
                        icon = Icons.Default.List,
                        label = "选集",
                        onClick = onShowEpisodes
                    )
                }
            }
        }

        // Center controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous episode
            if (hasPrevious) {
                PlayerControlButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "上一集",
                    onClick = onPrevious
                )
            }

            // Rewind
            PlayerControlButton(
                icon = Icons.Default.FastRewind,
                contentDescription = "后退10秒",
                onClick = onSeekBack
            )

            // Play/Pause
            PlayerControlButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                onClick = onPlayPause,
                isLarge = true
            )

            // Fast forward
            PlayerControlButton(
                icon = Icons.Default.FastForward,
                contentDescription = "快进10秒",
                onClick = onSeekForward
            )

            // Next episode
            if (hasNext) {
                PlayerControlButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "下一集",
                    onClick = onNext
                )
            }
        }

        // Bottom bar - Progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TvColors.Primary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Time and hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                    style = TvTypography.LabelMedium,
                    color = Color.White
                )

                if (hasEpisodes) {
                    Text(
                        text = "按 菜单键 选集",
                        style = TvTypography.LabelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .background(
                    color = if (isFocused) TvColors.Primary else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = TvTypography.LabelMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EpisodeSidebar(
    flags: List<Flag>,
    currentFlagIndex: Int,
    currentEpisodeIndex: Int,
    focusRequester: FocusRequester,
    onFlagSelect: (Int) -> Unit,
    onEpisodeSelect: (Episode) -> Unit,
    onClose: () -> Unit
) {
    val currentFlag = flags.getOrNull(currentFlagIndex)
    val episodes = currentFlag?.episodes ?: emptyList()
    val listState = rememberLazyListState()

    // Scroll to current episode
    LaunchedEffect(currentEpisodeIndex) {
        if (currentEpisodeIndex >= 0 && currentEpisodeIndex < episodes.size) {
            listState.animateScrollToItem(currentEpisodeIndex)
        }
    }

    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选集",
                style = TvTypography.TitleLarge,
                color = Color.White
            )
            Text(
                text = "${episodes.size}集",
                style = TvTypography.BodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Flag tabs (if multiple)
        if (flags.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(flags) { index, flag ->
                    FlagTab(
                        name = flag.show ?: flag.flag ?: "线路${index + 1}",
                        isSelected = index == currentFlagIndex,
                        onClick = { onFlagSelect(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Episodes list
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(episodes) { index, episode ->
                EpisodeItem(
                    episode = episode,
                    isPlaying = index == currentEpisodeIndex,
                    onClick = { onEpisodeSelect(episode) },
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@Composable
private fun FlagTab(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.3f)
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = name,
                style = TvTypography.LabelMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(onClick = onClick, modifier = modifier) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isPlaying -> TvColors.Primary.copy(alpha = 0.2f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = episode.name ?: "",
                style = TvTypography.BodyMedium,
                color = when {
                    isFocused -> Color.White
                    isPlaying -> TvColors.Primary
                    else -> Color.White.copy(alpha = 0.9f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isPlaying) {
                Icon(
                    imageVector = if (isFocused) Icons.Default.Check else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isFocused) Color.White else TvColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isLarge: Boolean = false
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = if (isFocused) TvColors.Primary else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(if (isLarge) 20.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(if (isLarge) 48.dp else 32.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
