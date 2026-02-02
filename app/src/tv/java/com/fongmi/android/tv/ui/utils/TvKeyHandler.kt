package com.fongmi.android.tv.ui.utils

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * TV Remote Key Handler Utilities
 * Provides common key handling patterns for TV navigation
 */
object TvKeyHandler {

    // Common D-pad keys
    val DPAD_CENTER = KeyEvent.KEYCODE_DPAD_CENTER
    val DPAD_UP = KeyEvent.KEYCODE_DPAD_UP
    val DPAD_DOWN = KeyEvent.KEYCODE_DPAD_DOWN
    val DPAD_LEFT = KeyEvent.KEYCODE_DPAD_LEFT
    val DPAD_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT

    // Media keys
    val MEDIA_PLAY_PAUSE = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
    val MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY
    val MEDIA_PAUSE = KeyEvent.KEYCODE_MEDIA_PAUSE
    val MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT
    val MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS
    val MEDIA_FAST_FORWARD = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
    val MEDIA_REWIND = KeyEvent.KEYCODE_MEDIA_REWIND
    val MEDIA_STOP = KeyEvent.KEYCODE_MEDIA_STOP

    // Function keys
    val MENU = KeyEvent.KEYCODE_MENU
    val INFO = KeyEvent.KEYCODE_INFO
    val BACK = KeyEvent.KEYCODE_BACK
    val HOME = KeyEvent.KEYCODE_HOME
    val SEARCH = KeyEvent.KEYCODE_SEARCH
    val SETTINGS = KeyEvent.KEYCODE_SETTINGS

    // Number keys (for channel input)
    val NUM_0 = KeyEvent.KEYCODE_0
    val NUM_1 = KeyEvent.KEYCODE_1
    val NUM_2 = KeyEvent.KEYCODE_2
    val NUM_3 = KeyEvent.KEYCODE_3
    val NUM_4 = KeyEvent.KEYCODE_4
    val NUM_5 = KeyEvent.KEYCODE_5
    val NUM_6 = KeyEvent.KEYCODE_6
    val NUM_7 = KeyEvent.KEYCODE_7
    val NUM_8 = KeyEvent.KEYCODE_8
    val NUM_9 = KeyEvent.KEYCODE_9

    // Color keys (some remotes have these)
    val PROG_RED = KeyEvent.KEYCODE_PROG_RED
    val PROG_GREEN = KeyEvent.KEYCODE_PROG_GREEN
    val PROG_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW
    val PROG_BLUE = KeyEvent.KEYCODE_PROG_BLUE

    // Volume keys
    val VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP
    val VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN
    val VOLUME_MUTE = KeyEvent.KEYCODE_VOLUME_MUTE

    // Channel keys
    val CHANNEL_UP = KeyEvent.KEYCODE_CHANNEL_UP
    val CHANNEL_DOWN = KeyEvent.KEYCODE_CHANNEL_DOWN

    /**
     * Check if the key is a number key (0-9)
     */
    fun isNumberKey(keyCode: Int): Boolean {
        return keyCode in NUM_0..NUM_9
    }

    /**
     * Get number value from key code
     */
    fun getNumberFromKey(keyCode: Int): Int? {
        return if (isNumberKey(keyCode)) keyCode - NUM_0 else null
    }

    /**
     * Check if this is a navigation key
     */
    fun isNavigationKey(keyCode: Int): Boolean {
        return keyCode in listOf(DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_CENTER)
    }

    /**
     * Check if this is a media control key
     */
    fun isMediaKey(keyCode: Int): Boolean {
        return keyCode in listOf(
            MEDIA_PLAY_PAUSE, MEDIA_PLAY, MEDIA_PAUSE,
            MEDIA_NEXT, MEDIA_PREVIOUS,
            MEDIA_FAST_FORWARD, MEDIA_REWIND, MEDIA_STOP
        )
    }
}

/**
 * Key event handler builder for cleaner key handling code
 */
class TvKeyEventHandler {
    private val handlers = mutableMapOf<Int, () -> Boolean>()
    private var defaultHandler: ((Int) -> Boolean)? = null

    fun onKey(keyCode: Int, handler: () -> Boolean): TvKeyEventHandler {
        handlers[keyCode] = handler
        return this
    }

    fun onKeys(vararg keyCodes: Int, handler: () -> Boolean): TvKeyEventHandler {
        keyCodes.forEach { handlers[it] = handler }
        return this
    }

    fun onDefault(handler: (Int) -> Boolean): TvKeyEventHandler {
        defaultHandler = handler
        return this
    }

    fun handle(keyCode: Int): Boolean {
        return handlers[keyCode]?.invoke() ?: defaultHandler?.invoke(keyCode) ?: false
    }
}

/**
 * Create a key event handler with DSL
 */
fun tvKeyHandler(block: TvKeyEventHandler.() -> Unit): TvKeyEventHandler {
    return TvKeyEventHandler().apply(block)
}

/**
 * Modifier extension for handling TV key events
 */
fun Modifier.onTvKeyEvent(
    onKeyDown: ((keyCode: Int) -> Boolean)? = null,
    onKeyUp: ((keyCode: Int) -> Boolean)? = null
): Modifier = this.onKeyEvent { event ->
    val keyCode = event.nativeKeyEvent.keyCode
    when (event.nativeKeyEvent.action) {
        KeyEvent.ACTION_DOWN -> onKeyDown?.invoke(keyCode) ?: false
        KeyEvent.ACTION_UP -> onKeyUp?.invoke(keyCode) ?: false
        else -> false
    }
}

/**
 * Modifier extension for handling specific TV keys
 */
fun Modifier.onTvKey(
    vararg keys: Int,
    onKeyDown: Boolean = true,
    handler: (keyCode: Int) -> Boolean
): Modifier = this.onKeyEvent { event ->
    val keyCode = event.nativeKeyEvent.keyCode
    val isTargetAction = if (onKeyDown) {
        event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
    } else {
        event.nativeKeyEvent.action == KeyEvent.ACTION_UP
    }

    if (isTargetAction && keyCode in keys) {
        handler(keyCode)
    } else {
        false
    }
}

/**
 * Modifier for common back key handling
 */
fun Modifier.onBackKey(handler: () -> Boolean): Modifier =
    onTvKey(TvKeyHandler.BACK) { handler() }

/**
 * Modifier for menu/info key handling
 */
fun Modifier.onMenuKey(handler: () -> Boolean): Modifier =
    onTvKey(TvKeyHandler.MENU, TvKeyHandler.INFO) { handler() }

/**
 * Modifier for search key handling
 */
fun Modifier.onSearchKey(handler: () -> Boolean): Modifier =
    onTvKey(TvKeyHandler.SEARCH) { handler() }

/**
 * Long press detection state
 */
data class LongPressState(
    var keyCode: Int = 0,
    var pressStartTime: Long = 0,
    var isLongPress: Boolean = false
)

/**
 * Seek amounts for player
 */
object SeekAmounts {
    const val SHORT = 10_000L      // 10 seconds
    const val MEDIUM = 30_000L    // 30 seconds
    const val LONG = 60_000L      // 1 minute
    const val VERY_LONG = 300_000L // 5 minutes
}

/**
 * Player key handler helper
 */
data class PlayerKeyConfig(
    val seekShort: Long = SeekAmounts.SHORT,
    val seekLong: Long = SeekAmounts.MEDIUM,
    val longPressThreshold: Long = 500L
)

/**
 * Global shortcut keys configuration
 */
object GlobalShortcuts {
    // Quick actions
    val TOGGLE_FAVORITE = KeyEvent.KEYCODE_BOOKMARK  // 收藏
    val TOGGLE_FULLSCREEN = KeyEvent.KEYCODE_F      // 全屏
    val TOGGLE_SUBTITLE = KeyEvent.KEYCODE_CAPTIONS // 字幕
    val TOGGLE_AUDIO = KeyEvent.KEYCODE_A           // 音轨
    val SHOW_EPG = KeyEvent.KEYCODE_G               // 节目单
    val SHOW_QUALITY = KeyEvent.KEYCODE_Q           // 画质
    val SHOW_SPEED = KeyEvent.KEYCODE_S             // 倍速

    // Alternative keys for remotes without specific buttons
    val ALT_FAVORITE = KeyEvent.KEYCODE_STAR        // * 键收藏
    val ALT_EPG = KeyEvent.KEYCODE_POUND            // # 键节目单
}

/**
 * Key repeat handler for continuous actions
 */
class KeyRepeatHandler(
    private val initialDelay: Long = 500L,
    private val repeatInterval: Long = 100L
) {
    private var lastKeyCode: Int = 0
    private var lastKeyTime: Long = 0
    private var repeatCount: Int = 0

    fun onKeyDown(keyCode: Int): KeyRepeatResult {
        val now = System.currentTimeMillis()
        return if (keyCode == lastKeyCode && now - lastKeyTime < initialDelay + repeatInterval * (repeatCount + 1)) {
            repeatCount++
            lastKeyTime = now
            KeyRepeatResult(isRepeat = true, repeatCount = repeatCount)
        } else {
            lastKeyCode = keyCode
            lastKeyTime = now
            repeatCount = 0
            KeyRepeatResult(isRepeat = false, repeatCount = 0)
        }
    }

    fun onKeyUp(keyCode: Int) {
        if (keyCode == lastKeyCode) {
            lastKeyCode = 0
            repeatCount = 0
        }
    }

    fun reset() {
        lastKeyCode = 0
        lastKeyTime = 0
        repeatCount = 0
    }
}

data class KeyRepeatResult(
    val isRepeat: Boolean,
    val repeatCount: Int
)

/**
 * Double tap detection
 */
class DoubleTapDetector(private val maxInterval: Long = 300L) {
    private var lastTapTime: Long = 0
    private var lastKeyCode: Int = 0

    fun onTap(keyCode: Int): Boolean {
        val now = System.currentTimeMillis()
        val isDoubleTap = keyCode == lastKeyCode && now - lastTapTime < maxInterval
        lastKeyCode = keyCode
        lastTapTime = now
        return isDoubleTap
    }

    fun reset() {
        lastTapTime = 0
        lastKeyCode = 0
    }
}
