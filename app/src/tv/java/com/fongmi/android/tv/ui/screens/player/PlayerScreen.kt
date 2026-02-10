package com.fongmi.android.tv.ui.screens.player

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.ui.components.DanmakuOverlay
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.rememberDanmakuState
import com.fongmi.android.tv.ui.dialog.CastDialog
import com.fongmi.android.tv.ui.dialog.DecodeDialog
import com.fongmi.android.tv.ui.dialog.DisplayDialog
import com.fongmi.android.tv.ui.dialog.EpisodeDialog
import com.fongmi.android.tv.ui.dialog.MultiTrackDialog
import com.fongmi.android.tv.ui.dialog.PlayerDialog
import com.fongmi.android.tv.ui.dialog.SpeedDialog
import com.fongmi.android.tv.ui.dialog.TrackInfo
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.utils.TvKeyHandler
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

    // Number input for quick episode selection
    var numberInput by remember { mutableStateOf("") }
    var showNumberInput by remember { mutableStateOf(false) }

    val sidebarFocusRequester = remember { FocusRequester() }

    // Danmaku state
    val danmakuState = rememberDanmakuState()

    // Subtitle state
    var isSubtitleEnabled by remember { mutableStateOf(true) }

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

    // Control subtitle track based on isSubtitleEnabled
    LaunchedEffect(isSubtitleEnabled) {
        val trackSelector = exoPlayer.trackSelector
        if (trackSelector is androidx.media3.exoplayer.trackselection.DefaultTrackSelector) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setTrackTypeDisabled(
                        androidx.media3.common.C.TRACK_TYPE_TEXT,
                        !isSubtitleEnabled
                    )
            )
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

            override fun onTracksChanged(tracks: Tracks) {
                val audioList = mutableListOf<TrackInfo>()
                val subtitleList = mutableListOf<TrackInfo>()

                tracks.groups.forEachIndexed { groupIndex, group ->
                    val trackType = group.type
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val isSelected = group.isTrackSelected(i)
                        val name = format.label ?: "轨道 ${i + 1}"
                        val language = format.language

                        when (trackType) {
                            C.TRACK_TYPE_AUDIO -> {
                                audioList.add(TrackInfo(groupIndex * 100 + i, name, language, isSelected))
                            }
                            C.TRACK_TYPE_TEXT -> {
                                subtitleList.add(TrackInfo(groupIndex * 100 + i, name, language, isSelected))
                            }
                        }
                    }
                }
                viewModel.updateTracks(audioList, subtitleList)
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

    // Auto-clear number input after delay
    LaunchedEffect(numberInput) {
        if (numberInput.isNotEmpty()) {
            showNumberInput = true
            delay(2000) // 2 seconds to complete input
            if (numberInput.isNotEmpty()) {
                val inputNum = numberInput.toIntOrNull()
                if (inputNum != null && inputNum > 0) {
                    if (uiState.isLive) {
                        // Live mode: jump to channel number
                        val targetIndex = inputNum - 1 // Convert to 0-based index
                        viewModel.switchToChannel(targetIndex)
                    } else {
                        // VOD mode: jump to episode number
                        val episodes = uiState.currentFlag?.episodes ?: emptyList()
                        val targetIndex = inputNum - 1 // Convert to 0-based index
                        if (targetIndex in episodes.indices) {
                            viewModel.selectEpisode(episodes[targetIndex])
                        }
                    }
                }
                numberInput = ""
                showNumberInput = false
            }
        }
    }

    // Helper function to handle number key input
    fun handleNumberKey(num: Int): Boolean {
        // Support number input for both VOD episodes and live channels
        if (!uiState.hasEpisodes && !uiState.hasChannels) return false
        numberInput += num.toString()
        showControls = true
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    val keyCode = event.nativeKeyEvent.keyCode
                    when {
                        // Number keys for quick episode selection
                        TvKeyHandler.isNumberKey(keyCode) -> {
                            val num = TvKeyHandler.getNumberFromKey(keyCode)
                            if (num != null) handleNumberKey(num) else false
                        }
                        // Navigation and control keys
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER -> {
                            if (showEpisodeSidebar) {
                                false // Let focus handle it
                            } else if (showControls) {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                true
                            } else {
                                showControls = true
                                true
                            }
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (showEpisodeSidebar) {
                                showEpisodeSidebar = false
                                true
                            } else {
                                showControls = true
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                                true
                            }
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!showEpisodeSidebar) {
                                showControls = true
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                            }
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                            if (!showEpisodeSidebar) {
                                showControls = true
                                // Long seek forward (30 seconds)
                                exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration))
                            }
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!showEpisodeSidebar) {
                                showControls = true
                                // Long seek backward (30 seconds)
                                exoPlayer.seekTo((exoPlayer.currentPosition - 30000).coerceAtLeast(0))
                            }
                            true
                        }
                        // Media control keys
                        keyCode == TvKeyHandler.MEDIA_PLAY_PAUSE -> {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            showControls = true
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_PLAY -> {
                            exoPlayer.play()
                            showControls = true
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_PAUSE -> {
                            exoPlayer.pause()
                            showControls = true
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_FAST_FORWARD -> {
                            exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration))
                            showControls = true
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_REWIND -> {
                            exoPlayer.seekTo((exoPlayer.currentPosition - 30000).coerceAtLeast(0))
                            showControls = true
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_NEXT -> {
                            viewModel.playNext()
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_PREVIOUS -> {
                            viewModel.playPrevious()
                            true
                        }
                        keyCode == TvKeyHandler.MEDIA_STOP -> {
                            exoPlayer.stop()
                            viewModel.clearState()
                            onBackClick()
                            true
                        }
                        // Channel keys for episode/channel navigation
                        keyCode == TvKeyHandler.CHANNEL_UP -> {
                            showControls = true
                            if (uiState.isLive) {
                                viewModel.nextChannel()
                            } else {
                                viewModel.playNext()
                            }
                            true
                        }
                        keyCode == TvKeyHandler.CHANNEL_DOWN -> {
                            showControls = true
                            if (uiState.isLive) {
                                viewModel.previousChannel()
                            } else {
                                viewModel.playPrevious()
                            }
                            true
                        }
                        // Menu/Info key
                        keyCode == TvKeyHandler.MENU || keyCode == TvKeyHandler.INFO -> {
                            if (uiState.hasEpisodes) {
                                showEpisodeSidebar = !showEpisodeSidebar
                                showControls = true
                            }
                            true
                        }
                        // Back key
                        keyCode == TvKeyHandler.BACK -> {
                            when {
                                numberInput.isNotEmpty() -> {
                                    numberInput = ""
                                    showNumberInput = false
                                    true
                                }
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

        // Danmaku overlay
        if (uiState.hasDanmu) {
            DanmakuOverlay(
                state = danmakuState,
                modifier = Modifier.fillMaxSize(),
                danmuUrl = uiState.danmuUrl,
                isPlaying = isPlaying,
                currentPosition = currentPosition
            )
        }

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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                hasPrevious = if (uiState.isLive) {
                    uiState.flatChannelIndex > 0
                } else {
                    uiState.currentEpisodeIndex > 0
                },
                hasNext = if (uiState.isLive) {
                    uiState.flatChannelIndex < uiState.totalChannels - 1
                } else {
                    uiState.currentFlag?.episodes?.let {
                        uiState.currentEpisodeIndex < it.size - 1
                    } ?: false
                },
                hasDanmu = uiState.hasDanmu,
                isDanmuEnabled = danmakuState.isEnabled,
                isLive = uiState.isLive,
                channelNumber = if (uiState.isLive) uiState.flatChannelIndex + 1 else 0,
                totalChannels = uiState.totalChannels,
                groupName = uiState.currentGroup?.name ?: "",
                onPlayPause = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekBack = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                },
                onSeekForward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                },
                onPrevious = {
                    if (uiState.isLive) viewModel.previousChannel() else viewModel.playPrevious()
                },
                onNext = {
                    if (uiState.isLive) viewModel.nextChannel() else viewModel.playNext()
                },
                onShowEpisodes = {
                    showEpisodeSidebar = true
                },
                onToggleDanmu = {
                    danmakuState.toggle()
                },
                isSubtitleEnabled = isSubtitleEnabled,
                onToggleSubtitle = {
                    isSubtitleEnabled = !isSubtitleEnabled
                },
                onBackClick = {
                    viewModel.clearState()
                    onBackClick()
                }
            )
        }

        // Number input overlay for quick episode/channel selection
        AnimatedVisibility(
            visible = showNumberInput && numberInput.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (uiState.isLive) "跳转到频道" else "跳转到第",
                        style = TvTypography.BodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = numberInput,
                        style = TvTypography.HeadlineLarge.copy(fontSize = 48.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!uiState.isLive) {
                        Text(
                            text = "集",
                            style = TvTypography.BodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
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

    // ========== Dialogs ==========

    // Speed Dialog
    if (uiState.showSpeedDialog) {
        SpeedDialog(
            currentSpeed = uiState.currentSpeed,
            onDismiss = { viewModel.dismissSpeedDialog() },
            onSelect = { speed ->
                viewModel.setSpeed(speed)
                exoPlayer.setPlaybackSpeed(speed)
            }
        )
    }

    // Player Dialog
    if (uiState.showPlayerDialog) {
        PlayerDialog(
            selectedIndex = uiState.currentPlayer,
            onDismiss = { viewModel.dismissPlayerDialog() },
            onSelect = { player -> viewModel.setPlayer(player) }
        )
    }

    // Decode Dialog
    if (uiState.showDecodeDialog) {
        DecodeDialog(
            selectedIndex = uiState.currentDecode,
            onDismiss = { viewModel.dismissDecodeDialog() },
            onSelect = { decode -> viewModel.setDecode(decode) }
        )
    }

    // Episode Dialog
    if (uiState.showEpisodeDialog && uiState.hasEpisodes) {
        val episodes = uiState.currentFlag?.episodes ?: emptyList()
        EpisodeDialog(
            episodes = episodes,
            onDismiss = { viewModel.dismissEpisodeDialog() },
            onSelect = { episode ->
                viewModel.selectEpisode(episode)
                viewModel.dismissEpisodeDialog()
            }
        )
    }

    // Display Dialog
    if (uiState.showDisplayDialog) {
        DisplayDialog(
            selectedIndex = uiState.currentScale,
            onDismiss = { viewModel.dismissDisplayDialog() },
            onSelect = { scale -> viewModel.setScale(scale) }
        )
    }

    // Track Dialog (Audio/Subtitle)
    if (uiState.showTrackDialog) {
        MultiTrackDialog(
            audioTracks = uiState.audioTracks,
            subtitleTracks = uiState.subtitleTracks,
            onDismiss = { viewModel.dismissTrackDialog() },
            onSelectAudio = { index ->
                viewModel.selectAudioTrack(index)
                // Apply audio track selection to ExoPlayer
                val trackSelector = exoPlayer.trackSelector
                if (trackSelector is androidx.media3.exoplayer.trackselection.DefaultTrackSelector) {
                    val params = trackSelector.buildUponParameters()
                    if (index >= 0) {
                        params.setPreferredAudioLanguage(uiState.audioTracks.find { it.index == index }?.language)
                    }
                    trackSelector.setParameters(params)
                }
            },
            onSelectSubtitle = { index ->
                viewModel.selectSubtitleTrack(index)
                // Apply subtitle track selection to ExoPlayer
                val trackSelector = exoPlayer.trackSelector
                if (trackSelector is androidx.media3.exoplayer.trackselection.DefaultTrackSelector) {
                    val params = trackSelector.buildUponParameters()
                    if (index < 0) {
                        params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    } else {
                        params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        params.setPreferredTextLanguage(uiState.subtitleTracks.find { it.index == index }?.language)
                    }
                    trackSelector.setParameters(params)
                }
            }
        )
    }

    // Cast Dialog
    if (uiState.showCastDialog) {
        CastDialog(
            devices = uiState.castDevices,
            isScanning = uiState.isCastScanning,
            onDismiss = { viewModel.dismissCastDialog() },
            onSelect = { device -> viewModel.connectCastDevice(device) },
            onRefresh = { viewModel.scanCastDevices() },
            onDisconnect = { viewModel.disconnectCast() }
        )
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
    hasDanmu: Boolean = false,
    isDanmuEnabled: Boolean = false,
    isLive: Boolean = false,
    channelNumber: Int = 0,
    totalChannels: Int = 0,
    groupName: String = "",
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShowEpisodes: () -> Unit,
    onToggleDanmu: () -> Unit = {},
    isSubtitleEnabled: Boolean = true,
    onToggleSubtitle: () -> Unit = {},
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Channel number badge for live TV
                    if (isLive && channelNumber > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format("%03d", channelNumber),
                                style = TvTypography.TitleMedium,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = vodName,
                        style = TvTypography.HeadlineMedium,
                        color = Color.White
                    )
                }
                if (isLive && groupName.isNotEmpty()) {
                    Text(
                        text = groupName,
                        style = TvTypography.BodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else if (!isLive && episodeName.isNotEmpty() && episodeName != "直播") {
                    Text(
                        text = episodeName,
                        style = TvTypography.BodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Top right actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Subtitle toggle button
                TopActionButton(
                    icon = Icons.Default.ClosedCaption,
                    label = if (isSubtitleEnabled) "字幕开" else "字幕关",
                    isActive = isSubtitleEnabled,
                    onClick = onToggleSubtitle
                )
                if (hasDanmu) {
                    TopActionButton(
                        icon = Icons.Default.Subtitles,
                        label = if (isDanmuEnabled) "弹幕开" else "弹幕关",
                        isActive = isDanmuEnabled,
                        onClick = onToggleDanmu
                    )
                }
                if (hasEpisodes) {
                    TopActionButton(
                        icon = Icons.AutoMirrored.Filled.List,
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
            // Previous episode/channel
            if (hasPrevious) {
                PlayerControlButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = if (isLive) "上一频道" else "上一集",
                    onClick = onPrevious
                )
            }

            if (!isLive) {
                // Rewind (VOD only)
                PlayerControlButton(
                    icon = Icons.Default.FastRewind,
                    contentDescription = "后退10秒",
                    onClick = onSeekBack
                )
            }

            // Play/Pause
            PlayerControlButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                onClick = onPlayPause,
                isLarge = true
            )

            if (!isLive) {
                // Fast forward (VOD only)
                PlayerControlButton(
                    icon = Icons.Default.FastForward,
                    contentDescription = "快进10秒",
                    onClick = onSeekForward
                )
            }

            // Next episode/channel
            if (hasNext) {
                PlayerControlButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = if (isLive) "下一频道" else "下一集",
                    onClick = onNext
                )
            }
        }

        // Bottom bar - Progress (VOD) or Channel info (Live)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            if (isLive) {
                // Live TV info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Text(
                            text = "直播中",
                            style = TvTypography.LabelMedium,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "频道 $channelNumber / $totalChannels",
                        style = TvTypography.LabelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "频道+/- 切换频道 | 数字键快速跳转",
                    style = TvTypography.LabelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            } else {
                // VOD Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
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
}

@Composable
private fun TopActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> Color.White.copy(alpha = 0.2f)
                    },
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
                        isFocused -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
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
                        isFocused -> MaterialTheme.colorScheme.primary
                        isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
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
                    isPlaying -> MaterialTheme.colorScheme.primary
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
                    tint = if (isFocused) Color.White else MaterialTheme.colorScheme.primary,
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
    FocusableItem(
        onClick = onClick,
        shape = CircleShape
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
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
