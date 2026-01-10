package com.fongmi.android.tv.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.fongmi.android.tv.App
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.player.danmu.Parser
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.ui.widget.DanmakuView

/**
 * Danmaku state holder for Compose
 */
class DanmakuState {
    var danmakuView: DanmakuView? = null
        internal set
    var danmakuContext: DanmakuContext? = null
        internal set
    var isEnabled: Boolean = Setting.isDanmu()
        private set
    var isPrepared: Boolean = false
        internal set
    var currentDanmuUrl: String = ""
        internal set

    /**
     * Toggle danmaku visibility
     */
    fun toggle(): Boolean {
        isEnabled = !isEnabled
        Setting.putDanmu(isEnabled)
        if (isEnabled) {
            danmakuView?.show()
        } else {
            danmakuView?.hide()
        }
        return isEnabled
    }

    /**
     * Show danmaku
     */
    fun show() {
        isEnabled = true
        Setting.putDanmu(true)
        danmakuView?.show()
    }

    /**
     * Hide danmaku
     */
    fun hide() {
        isEnabled = false
        Setting.putDanmu(false)
        danmakuView?.hide()
    }

    /**
     * Load danmaku from URL
     */
    fun load(danmuUrl: String) {
        if (danmuUrl.isEmpty() || danmuUrl == currentDanmuUrl) return
        currentDanmuUrl = danmuUrl

        danmakuView?.release()
        val view = danmakuView ?: return
        val context = danmakuContext ?: return

        App.execute {
            view.prepare(Parser(danmuUrl), context)
        }
    }

    /**
     * Sync position with player
     */
    fun seekTo(position: Long) {
        danmakuView?.seekTo(position)
    }

    /**
     * Pause danmaku
     */
    fun pause() {
        danmakuView?.pause()
    }

    /**
     * Resume danmaku
     */
    fun resume() {
        danmakuView?.resume()
    }

    /**
     * Release resources
     */
    fun release() {
        danmakuView?.release()
        isPrepared = false
        currentDanmuUrl = ""
    }

    /**
     * Update settings (line count, alpha, size, speed)
     */
    fun updateSettings() {
        val context = danmakuContext ?: return
        val range = floatArrayOf(2.4f, 1.8f, 1.2f, 0.8f)
        val speed = range[Setting.getDanmuSpeed()]
        val alpha = Setting.getDanmuAlpha() / 100.0f
        val sizeScale = 1.2f * Setting.getDanmuSize()
        val maxLine = Setting.getDanmuLine(3)

        val maxLines = hashMapOf(
            BaseDanmaku.TYPE_FIX_TOP to maxLine,
            BaseDanmaku.TYPE_SCROLL_RL to maxLine,
            BaseDanmaku.TYPE_SCROLL_LR to maxLine,
            BaseDanmaku.TYPE_FIX_BOTTOM to maxLine
        )

        context.setMaximumLines(maxLines)
            .setScrollSpeedFactor(speed)
            .setDanmakuTransparency(alpha)
            .setScaleTextSize(sizeScale)
    }

    /**
     * Increase line count
     */
    fun increaseLines(): Int {
        var line = Setting.getDanmuLine(3)
        line = minOf(line + 1, 15)
        Setting.putDanmuLine(line)
        updateSettings()
        return line
    }

    /**
     * Decrease line count
     */
    fun decreaseLines(): Int {
        var line = Setting.getDanmuLine(3)
        line = maxOf(line - 1, 1)
        Setting.putDanmuLine(line)
        updateSettings()
        return line
    }
}

/**
 * Remember danmaku state
 */
@Composable
fun rememberDanmakuState(): DanmakuState {
    return remember { DanmakuState() }
}

/**
 * Danmaku overlay composable
 * Wraps DanmakuView in Compose
 */
@Composable
fun DanmakuOverlay(
    state: DanmakuState,
    modifier: Modifier = Modifier,
    danmuUrl: String = "",
    isPlaying: Boolean = true,
    currentPosition: Long = 0L
) {
    val context = LocalContext.current

    // Track last seek position to avoid redundant seeks
    var lastSyncPosition by remember { mutableLongStateOf(0L) }

    // Create DanmakuContext once
    val danmakuContext = remember {
        DanmakuContext.create().apply {
            setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDanmakuMargin(8)
        }
    }

    // Load danmaku when URL changes
    LaunchedEffect(danmuUrl) {
        if (danmuUrl.isNotEmpty()) {
            state.load(danmuUrl)
        }
    }

    // Sync playback state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            state.resume()
        } else {
            state.pause()
        }
    }

    // Sync position (only when significant change)
    LaunchedEffect(currentPosition) {
        val diff = kotlin.math.abs(currentPosition - lastSyncPosition)
        if (diff > 2000) { // Sync if more than 2 seconds difference
            state.seekTo(currentPosition)
            lastSyncPosition = currentPosition
        }
    }

    AndroidView(
        factory = { ctx ->
            DanmakuView(ctx).apply {
                state.danmakuView = this
                state.danmakuContext = danmakuContext

                // Setup callback
                setCallback(object : DrawHandler.Callback {
                    override fun prepared() {
                        state.isPrepared = true
                        state.updateSettings()
                        if (state.isEnabled) {
                            show()
                        }
                        start()
                    }

                    override fun updateTimer(timer: DanmakuTimer?) {}
                    override fun danmakuShown(danmaku: BaseDanmaku?) {}
                    override fun drawingFinished() {}
                })
            }
        },
        modifier = modifier,
        onRelease = {
            state.release()
        }
    )

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            state.release()
        }
    }
}
