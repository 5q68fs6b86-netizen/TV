package com.fongmi.android.tv.ui.utils

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay

/**
 * Focus management utilities for TV navigation
 */

/**
 * Remember multiple focus requesters
 */
@Composable
fun rememberFocusRequesters(count: Int): List<FocusRequester> {
    return remember { List(count) { FocusRequester() } }
}

/**
 * Focus requester with auto-request on first composition
 */
@Composable
fun rememberAutoFocusRequester(delayMs: Long = 100): FocusRequester {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(delayMs)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }
    return focusRequester
}

/**
 * Focus state holder for tracking focus across items
 */
class FocusStateHolder {
    var lastFocusedIndex: Int = 0
        private set

    fun onFocusChanged(index: Int, hasFocus: Boolean) {
        if (hasFocus) {
            lastFocusedIndex = index
        }
    }

    fun restoreFocus(focusRequesters: List<FocusRequester>) {
        val index = lastFocusedIndex.coerceIn(0, focusRequesters.size - 1)
        try {
            focusRequesters.getOrNull(index)?.requestFocus()
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberFocusStateHolder(): FocusStateHolder {
    return remember { FocusStateHolder() }
}

/**
 * Modifier for tracking focus index
 */
fun Modifier.trackFocusIndex(
    index: Int,
    holder: FocusStateHolder
): Modifier = this.onFocusChanged { state ->
    holder.onFocusChanged(index, state.hasFocus)
}

/**
 * Grid focus navigation helper
 */
class GridFocusManager(
    private val columns: Int,
    private val itemCount: Int
) {
    fun getNextIndex(currentIndex: Int, direction: FocusDirection): Int? {
        return when (direction) {
            FocusDirection.Up -> {
                val newIndex = currentIndex - columns
                if (newIndex >= 0) newIndex else null
            }
            FocusDirection.Down -> {
                val newIndex = currentIndex + columns
                if (newIndex < itemCount) newIndex else null
            }
            FocusDirection.Left -> {
                if (currentIndex % columns > 0) currentIndex - 1 else null
            }
            FocusDirection.Right -> {
                if (currentIndex % columns < columns - 1 && currentIndex + 1 < itemCount) {
                    currentIndex + 1
                } else null
            }
            else -> null
        }
    }

    fun getRowIndex(index: Int): Int = index / columns
    fun getColumnIndex(index: Int): Int = index % columns
}

/**
 * Focus trap - keeps focus within a container
 */
fun Modifier.focusTrap(): Modifier = this.focusGroup()

/**
 * Circular focus navigation - wraps around at edges
 */
class CircularFocusManager(private val itemCount: Int) {
    fun getNextIndex(currentIndex: Int, forward: Boolean): Int {
        return if (forward) {
            (currentIndex + 1) % itemCount
        } else {
            (currentIndex - 1 + itemCount) % itemCount
        }
    }
}

/**
 * Focus restoration helper for dialogs/overlays
 */
class FocusRestorationHelper {
    private var savedFocusRequester: FocusRequester? = null

    fun saveFocus(focusRequester: FocusRequester) {
        savedFocusRequester = focusRequester
    }

    fun restoreFocus() {
        try {
            savedFocusRequester?.requestFocus()
        } catch (_: Exception) {}
        savedFocusRequester = null
    }
}

@Composable
fun rememberFocusRestorationHelper(): FocusRestorationHelper {
    return remember { FocusRestorationHelper() }
}
