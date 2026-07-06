package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.components.JetStreamControlScrim
import com.fongmi.android.tv.ui.components.JetStreamInfoScrim
import com.fongmi.android.tv.ui.theme.JetStreamTheme
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToLong

class JetStreamVodControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    interface Listener {
        fun onPlayPause()
        fun onPrevious()
        fun onNext()
        fun onRepeat()
        fun onCommand(key: String)
        fun onCommandLongClick(key: String)
        fun onSeekTo(positionMs: Long)
        fun onShowControls()
    }

    private data class CommandState(
        val label: String,
        val visible: Boolean,
        val selected: Boolean
    )

    private data class CommandGroupState(
        val key: String,
        @param:DrawableRes val icon: Int,
        val label: String,
        val visible: Boolean,
        val commandKeys: List<String>
    )

    private var listener: Listener? = null
    private var mediaPlayer by mutableStateOf<Player?>(null)
    private var mediaTitle by mutableStateOf("")
    private var secondaryText by mutableStateOf("")
    private var tertiaryText by mutableStateOf("")
    private var playing by mutableStateOf(false)
    private var repeating by mutableStateOf(false)
    private var previousVisible by mutableStateOf(true)
    private var nextVisible by mutableStateOf(true)
    private var repeatVisible by mutableStateOf(true)
    private var controlPanelVisible by mutableStateOf(false)
    private var topInfoVisible by mutableStateOf(false)
    private var centerInfoVisible by mutableStateOf(false)
    private var showTopInfoSubtitle by mutableStateOf(false)
    private var infoSize by mutableStateOf("")
    private var infoClock by mutableStateOf("")
    private var infoAction by mutableStateOf(ACTION_PLAY)
    private var infoPosition by mutableStateOf("")
    private var infoDuration by mutableStateOf("")
    private var activeGroup by mutableStateOf<String?>(null)
    private val commandGroups = mutableStateListOf(
        CommandGroupState(GROUP_PLAYLIST, R.drawable.msr_auto_awesome_motion, "Playlist", true, PLAYLIST_COMMANDS),
        CommandGroupState(GROUP_CAPTIONS, R.drawable.msr_closed_caption, "Captions", true, CAPTION_COMMANDS),
        CommandGroupState(GROUP_SETTINGS, R.drawable.msr_settings, "Settings", true, SETTINGS_COMMANDS)
    )
    private val commands = mutableStateMapOf<String, CommandState>()

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    @Composable
    override fun Content() {
        JetStreamControls()
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setPlayer(player: Player?) {
        mediaPlayer = player
    }

    fun setMediaTitle(title: CharSequence?, secondary: CharSequence?, tertiary: CharSequence?) {
        mediaTitle = title?.toString().orEmpty()
        secondaryText = secondary?.toString().orEmpty()
        tertiaryText = tertiary?.toString().orEmpty()
    }

    fun setPlaybackState(isPlaying: Boolean, isRepeating: Boolean) {
        playing = isPlaying
        repeating = isRepeating
    }

    fun setTransportActions(previous: Boolean, next: Boolean, repeat: Boolean) {
        previousVisible = previous
        nextVisible = next
        repeatVisible = repeat
    }

    fun setTopActions(playlist: Boolean, captions: Boolean, settings: Boolean) {
        setCommandGroupVisibility(GROUP_PLAYLIST, playlist)
        setCommandGroupVisibility(GROUP_CAPTIONS, captions)
        setCommandGroupVisibility(GROUP_SETTINGS, settings)
    }

    fun setTopInfoSubtitleVisible(visible: Boolean) {
        showTopInfoSubtitle = visible
    }

    fun setControlsVisible(visible: Boolean) {
        controlPanelVisible = visible
    }

    fun isControlsVisible(): Boolean {
        return controlPanelVisible
    }

    fun setInfoState(
        topVisible: Boolean,
        centerVisible: Boolean,
        size: CharSequence?,
        clock: CharSequence?,
        action: String,
        position: CharSequence?,
        duration: CharSequence?
    ) {
        topInfoVisible = topVisible
        centerInfoVisible = centerVisible
        infoSize = size?.toString().orEmpty()
        infoClock = clock?.toString().orEmpty()
        infoAction = action
        infoPosition = position?.toString().orEmpty()
        infoDuration = duration?.toString().orEmpty()
    }

    fun isInfoVisible(): Boolean {
        return topInfoVisible || centerInfoVisible
    }

    fun isCenterInfoVisible(): Boolean {
        return centerInfoVisible
    }

    fun setCommand(key: String, label: CharSequence?, visible: Boolean, selected: Boolean) {
        commands[key] = CommandState(label?.toString().orEmpty(), visible, selected)
    }

    fun setCommandGroup(key: String, @DrawableRes icon: Int, label: CharSequence?, visible: Boolean, vararg commandKeys: String) {
        val group = CommandGroupState(key, icon, label?.toString().orEmpty(), visible, commandKeys.toList())
        val index = commandGroups.indexOfFirst { it.key == key }
        if (index >= 0) commandGroups[index] = group else commandGroups.add(group)
        if (!visible && activeGroup == key) activeGroup = null
    }

    fun showGroup(group: String?) {
        activeGroup = group
    }

    @Composable
    private fun JetStreamControls() {
        val player = mediaPlayer
        var positionMs by remember(player) { mutableLongStateOf(normalize(player?.currentPosition)) }
        var durationMs by remember(player) { mutableLongStateOf(normalize(player?.duration)) }
        var polledPlaying by remember(player, playing) { mutableStateOf(playing) }

        LaunchedEffect(player) {
            while (isActive) {
                positionMs = normalize(player?.currentPosition)
                durationMs = normalize(player?.duration)
                polledPlaying = player?.isPlaying ?: playing
                delay(300)
            }
        }

        JetStreamTheme {
            Box(Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = controlPanelVisible,
                    enter = fadeIn(tween(JetStreamAnimations.DurationPanel)) + slideInVertically(
                        animationSpec = tween(JetStreamAnimations.DurationPanel),
                        initialOffsetY = { it / 5 }
                    ),
                    exit = fadeOut(tween(JetStreamAnimations.DurationExit)) + slideOutVertically(
                        animationSpec = tween(JetStreamAnimations.DurationExit),
                        targetOffsetY = { it / 6 }
                    )
                ) {
                    JetStreamControlScrim(modifier = Modifier.fillMaxSize()) {
                        InfoOverlay()
                        ControlPanel(polledPlaying, positionMs, durationMs)
                    }
                }
                AnimatedVisibility(
                    visible = !controlPanelVisible && isInfoVisible(),
                    enter = fadeIn(tween(JetStreamAnimations.DurationShort)) + scaleIn(
                        animationSpec = tween(JetStreamAnimations.DurationShort),
                        initialScale = 0.98f
                    ),
                    exit = fadeOut(tween(JetStreamAnimations.DurationExit)) + scaleOut(
                        animationSpec = tween(JetStreamAnimations.DurationExit),
                        targetScale = 0.98f
                    )
                ) {
                    JetStreamInfoScrim(modifier = Modifier.fillMaxSize()) {
                        InfoOverlay()
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.ControlPanel(isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 56.dp)
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            HeaderRow(isPlaying)
            Spacer(Modifier.height(16.dp))
            SeekerRow(
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs
            )
            MorePanel()
        }
    }

    @Composable
    private fun BoxScope.InfoOverlay() {
        if (topInfoVisible) TopInfo()
        if (centerInfoVisible) CenterInfo()
    }

    @Composable
    private fun BoxScope.TopInfo() {
        val colorScheme = MaterialTheme.colorScheme
        val subtitle = subtitleText()
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 32.dp)
            ) {
                Text(
                    text = mediaTitle,
                    color = colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showTopInfoSubtitle && subtitle.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (infoSize.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = infoSize,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (infoClock.isNotEmpty()) {
                Text(
                    text = infoClock,
                    color = colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    private fun BoxScope.CenterInfo() {
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .background(colorScheme.surface.copy(alpha = 0.82f))
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = centerInfoIcon()),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = infoPosition,
                    color = colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = " / ",
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    fontSize = 18.sp,
                    maxLines = 1
                )
                Text(
                    text = infoDuration,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }
        }
    }

    @DrawableRes
    private fun centerInfoIcon(): Int {
        return when (infoAction) {
            ACTION_FORWARD -> R.drawable.msr_fast_forward
            ACTION_REWIND -> R.drawable.msr_fast_rewind
            else -> R.drawable.msr_play_arrow
        }
    }

    @Composable
    private fun HeaderRow(isPlaying: Boolean) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            MediaTitle(Modifier.weight(1f))
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (previousVisible) {
                    ControlIcon(R.drawable.msr_skip_previous, isPlaying, true, false, "Previous") {
                        listener?.onPrevious()
                    }
                }
                if (nextVisible) {
                    ControlIcon(R.drawable.msr_skip_next, isPlaying, true, false, "Next") {
                        listener?.onNext()
                    }
                }
                if (repeatVisible) {
                    ControlIcon(
                        icon = if (repeating) R.drawable.msr_repeat_one else R.drawable.msr_repeat,
                        isPlaying = isPlaying,
                        enabled = true,
                        selected = repeating,
                        contentDescription = "Repeat"
                    ) {
                        listener?.onRepeat()
                    }
                }
                commandGroups.filter { it.visible }.forEach { group ->
                    ControlIcon(group.icon, isPlaying, true, activeGroup == group.key, group.label) {
                        toggleGroup(group.key)
                    }
                }
            }
        }
    }

    @Composable
    private fun MediaTitle(modifier: Modifier) {
        val colorScheme = MaterialTheme.colorScheme
        val subtitle = subtitleText()
        Column(modifier = modifier.padding(end = 24.dp)) {
            Text(
                text = mediaTitle,
                color = colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun SeekerRow(isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlIcon(
                icon = if (isPlaying) R.drawable.msr_pause else R.drawable.msr_play_arrow,
                isPlaying = isPlaying,
                enabled = true,
                selected = false,
                contentDescription = "Play/Pause"
            ) {
                listener?.onPlayPause()
            }
            Spacer(Modifier.width(12.dp))
            ControllerText(formatTime(positionMs))
            ControllerIndicator(
                progress = progress(positionMs, durationMs),
                durationMs = durationMs,
                modifier = Modifier.weight(1f)
            )
            ControllerText(formatTime(durationMs))
        }
    }

    @Composable
    private fun MorePanel() {
        val group = activeGroup ?: return
        val groupState = commandGroups.firstOrNull { it.key == group && it.visible } ?: return
        val visibleCommands = groupState.commandKeys.mapNotNull { key ->
            commands[key]?.takeIf { it.visible && it.label.isNotEmpty() }?.let { key to it }
        }
        if (visibleCommands.isEmpty()) return

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleCommands.forEach { (key, state) ->
                CommandChip(
                    state = state,
                    onClick = { listener?.onCommand(key) },
                    onLongClick = { listener?.onCommandLongClick(key) }
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun CommandChip(state: CommandState, onClick: () -> Unit, onLongClick: () -> Unit) {
        val colorScheme = MaterialTheme.colorScheme
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(
            if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "chipScale"
        )
        val contentColor by animateColorAsState(
            targetValue = when {
                state.selected || focused -> colorScheme.onSurface
                else -> colorScheme.onSurfaceVariant
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "chipContent"
        )
        Box(
            modifier = Modifier
                .height(36.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        listener?.onShowControls()
                        onClick()
                    },
                    onLongClick = {
                        listener?.onShowControls()
                        onLongClick()
                    }
                )
                .padding(horizontal = JetStreamSpacing.ButtonHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.label,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = if (state.selected || focused) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        LaunchedEffect(focused) {
            if (focused) listener?.onShowControls()
        }
    }

    @Composable
    private fun ControlIcon(
        @DrawableRes icon: Int,
        isPlaying: Boolean,
        enabled: Boolean,
        selected: Boolean,
        contentDescription: String,
        onClick: () -> Unit
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(
            if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "iconScale"
        )
        val background by animateColorAsState(
            targetValue = when {
                selected -> colorScheme.secondaryContainer
                focused -> colorScheme.primaryContainer
                enabled -> colorScheme.surfaceVariant.copy(alpha = 0.82f)
                else -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "iconBackground"
        )
        val contentColor by animateColorAsState(
            targetValue = when {
                !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                selected -> colorScheme.onSecondaryContainer
                focused -> colorScheme.onPrimaryContainer
                else -> colorScheme.onSurfaceVariant
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "iconContent"
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)
                .background(background)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        listener?.onShowControls()
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            if (selected) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        color = contentColor,
                        radius = 2.dp.toPx(),
                        center = Offset(size.width / 2f, size.height - 6.dp.toPx())
                    )
                }
            }
        }
        LaunchedEffect(focused, isPlaying) {
            if (focused && isPlaying) listener?.onShowControls()
        }
    }

    @Composable
    private fun ControllerText(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 1
        )
    }

    @Composable
    private fun ControllerIndicator(progress: Float, durationMs: Long, modifier: Modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        var selected by remember { mutableStateOf(false) }
        var seekProgress by remember { mutableStateOf(progress) }
        val height by animateDpAsState(if (focused) 10.dp else 4.dp, label = "indicatorHeight")
        val colorScheme = MaterialTheme.colorScheme
        val progressColor = if (selected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.92f)
        val trackColor = colorScheme.outlineVariant.copy(alpha = 0.72f)
        val displayProgress = if (selected) seekProgress else progress

        LaunchedEffect(progress, selected) {
            if (!selected) seekProgress = progress
        }
        LaunchedEffect(selected) {
            if (selected) listener?.onShowControls()
        }

        Canvas(
            modifier = modifier
                .height(height)
                .padding(horizontal = 4.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || durationMs <= 0) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionCenter, Key.Enter -> {
                            if (selected) listener?.onSeekTo((durationMs * seekProgress).roundToLong())
                            else seekProgress = progress
                            selected = !selected
                            listener?.onShowControls()
                            true
                        }

                        Key.DirectionLeft -> {
                            selected = true
                            seekProgress = (seekProgress - 0.10f).coerceAtLeast(0f)
                            listener?.onShowControls()
                            true
                        }

                        Key.DirectionRight -> {
                            selected = true
                            seekProgress = (seekProgress + 0.10f).coerceAtMost(1f)
                            listener?.onShowControls()
                            true
                        }

                        Key.Back -> {
                            selected = false
                            false
                        }

                        else -> false
                    }
                }
                .focusable(interactionSource = interactionSource)
        ) {
            val y = size.height / 2f
            drawLine(
                color = trackColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = progressColor,
                start = Offset(0f, y),
                end = Offset(size.width * displayProgress.coerceIn(0f, 1f), y),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    }

    private fun toggleGroup(group: String) {
        activeGroup = if (activeGroup == group) null else group
        listener?.onShowControls()
    }

    private fun setCommandGroupVisibility(key: String, visible: Boolean) {
        val index = commandGroups.indexOfFirst { it.key == key }
        if (index < 0) return
        val group = commandGroups[index]
        commandGroups[index] = group.copy(visible = visible)
        if (!visible && activeGroup == key) activeGroup = null
    }

    private fun subtitleText(): String {
        return buildString {
            append(secondaryText)
            if (secondaryText.isNotEmpty() && tertiaryText.isNotEmpty()) append(" • ")
            append(tertiaryText)
        }
    }

    private fun normalize(value: Long?): Long {
        val time = value ?: 0L
        return if (time == C.TIME_UNSET || time < 0L) 0L else time
    }

    private fun progress(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0L) return 0f
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    companion object {
        const val GROUP_PLAYLIST = "playlist"
        const val GROUP_CAPTIONS = "captions"
        const val GROUP_SETTINGS = "settings"
        const val ACTION_PLAY = "play"
        const val ACTION_FORWARD = "forward"
        const val ACTION_REWIND = "rewind"

        private val PLAYLIST_COMMANDS = listOf("prev", "next", "change", "parse", "replay", "reset")
        private val CAPTION_COMMANDS = listOf("subtitle", "text", "audio", "video", "danmaku")
        private val SETTINGS_COMMANDS = listOf("speed", "scale", "player", "decode", "opening", "ending", "edition", "chapter")
    }
}
