package com.fongmi.android.tv.ui.screens.player

import android.view.KeyEvent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvTypography
import kotlinx.coroutines.delay

/**
 * Player Screen with ExoPlayer integration
 */
@Composable
fun PlayerScreen(
    url: String,
    vodName: String,
    episodeName: String,
    headers: Map<String, String>? = null,
    onBackClick: () -> Unit,
    onNextEpisode: (() -> Unit)? = null,
    onPreviousEpisode: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Player state
    var isPlaying by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Create ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = if (headers.isNullOrEmpty()) {
                MediaItem.fromUri(url)
            } else {
                MediaItem.Builder()
                    .setUri(url)
                    .build()
            }
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
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
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (showControls) {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            } else {
                                showControls = true
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            showControls = true
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            showControls = true
                            exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            showControls = true
                            true
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            if (showControls) {
                                showControls = false
                                true
                            } else {
                                onBackClick()
                                true
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

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TvColors.Primary)
            }
        }

        // Controls overlay
        if (showControls) {
            PlayerControls(
                vodName = vodName,
                episodeName = episodeName,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                progress = progress,
                onPlayPause = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekBack = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                },
                onSeekForward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                },
                onPreviousEpisode = onPreviousEpisode,
                onNextEpisode = onNextEpisode,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun PlayerControls(
    vodName: String,
    episodeName: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    progress: Float,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onPreviousEpisode: (() -> Unit)?,
    onNextEpisode: (() -> Unit)?,
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
        // Top bar - Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Text(
                text = vodName,
                style = TvTypography.HeadlineMedium,
                color = Color.White
            )
            if (episodeName.isNotEmpty()) {
                Text(
                    text = episodeName,
                    style = TvTypography.BodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Center controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous episode
            if (onPreviousEpisode != null) {
                PlayerControlButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    onClick = onPreviousEpisode
                )
            }

            // Rewind
            PlayerControlButton(
                icon = Icons.Default.FastRewind,
                contentDescription = "Rewind 10s",
                onClick = onSeekBack
            )

            // Play/Pause
            PlayerControlButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = onPlayPause,
                isLarge = true
            )

            // Fast forward
            PlayerControlButton(
                icon = Icons.Default.FastForward,
                contentDescription = "Forward 10s",
                onClick = onSeekForward
            )

            // Next episode
            if (onNextEpisode != null) {
                PlayerControlButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    onClick = onNextEpisode
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
                    .height(4.dp),
                color = TvColors.Primary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = TvTypography.LabelMedium,
                    color = Color.White
                )
                Text(
                    text = formatTime(duration),
                    style = TvTypography.LabelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PlayerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isLarge: Boolean = false
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = if (isFocused) TvColors.Primary else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                )
                .padding(if (isLarge) 20.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier
                    .width(if (isLarge) 48.dp else 32.dp)
                    .height(if (isLarge) 48.dp else 32.dp)
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
