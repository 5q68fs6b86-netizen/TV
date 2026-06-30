package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class JetStreamChipRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    interface OnChipClickListener {
        fun onChipClick(position: Int)
    }

    interface OnChipLongClickListener {
        fun onChipLongClick(position: Int)
    }

    private val items = mutableStateListOf<String>()
    private var selectedIndex by mutableIntStateOf(-1)
    private var focusedIndex by mutableIntStateOf(-1)
    private var clickListener: ((Int) -> Unit)? = null
    private var longClickListener: ((Int) -> Unit)? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setItems(texts: List<String>, selected: Int) {
        items.clear()
        items.addAll(texts)
        selectedIndex = selected
        focusedIndex = if (selected >= 0) selected else 0
    }

    fun setItems(texts: List<String>) {
        setItems(texts, -1)
    }

    fun getSelectedPosition(): Int = selectedIndex

    fun setSelectedPosition(position: Int) {
        selectedIndex = position
        setFocusedPosition(position)
    }

    fun setFocusedPosition(position: Int) {
        if (position in 0 until items.size) focusedIndex = position
    }

    fun setNextFocusUp(id: Int) {
        nextFocusUpId = id
    }

    fun setNextFocusDown(id: Int) {
        nextFocusDownId = id
    }

    fun setOnChipClickListener(listener: (Int) -> Unit) {
        clickListener = listener
    }

    fun setOnChipClickListener(listener: OnChipClickListener) {
        clickListener = { pos -> listener.onChipClick(pos) }
    }

    fun setOnChipLongClickListener(listener: (Int) -> Unit) {
        longClickListener = listener
    }

    fun setOnChipLongClickListener(listener: OnChipLongClickListener) {
        longClickListener = { pos -> listener.onChipLongClick(pos) }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        return event.action == KeyEvent.ACTION_DOWN && handleKeyDown(keyCode) || super.onKeyPreIme(keyCode, event)
    }

    private fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (focusedIndex > 0) {
                    focusedIndex--
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (focusedIndex < items.size - 1) {
                    focusedIndex++
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (focusedIndex in 0 until items.size) {
                    clickListener?.invoke(focusedIndex)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    @Composable
    override fun Content() {
        MaterialTheme(colorScheme = darkColorScheme(primary = Color.White, onSurface = Color.White, surface = Color.Transparent)) {
            val listState = rememberLazyListState()
            val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

            LaunchedEffect(focusedIndex) {
                if (focusedIndex in 0 until items.size) {
                    listState.animateScrollToItem(focusedIndex)
                }
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .onPreviewKeyEvent { event ->
                        event.type == KeyEventType.KeyDown && handleKeyDown(event.nativeKeyEvent.keyCode)
                    },
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(items) { index, text ->
                    val requester = focusRequesters.getOrPut(index) { FocusRequester() }
                    Chip(
                        text = text,
                        focused = index == focusedIndex,
                        selected = index == selectedIndex,
                        focusRequester = requester,
                        onClick = { clickListener?.invoke(index) },
                        onLongClick = longClickListener?.let { listener -> { listener(index) } }
                    )

                    LaunchedEffect(focusedIndex) {
                        if (index == focusedIndex) {
                            requester.requestFocus()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Chip(
        text: String,
        focused: Boolean,
        selected: Boolean,
        focusRequester: FocusRequester,
        onClick: () -> Unit,
        onLongClick: (() -> Unit)?
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val scale by animateFloatAsState(if (focused) 1.05f else 1.0f, label = "chipScale")
        val background by animateColorAsState(
            targetValue = when {
                focused -> Color.White.copy(alpha = 0.22f)
                selected -> Color.White.copy(alpha = 0.18f)
                else -> Color.White.copy(alpha = 0.10f)
            },
            label = "chipBackground"
        )
        val borderAlpha by animateFloatAsState(if (focused) 0.30f else 0f, label = "chipBorder")

        Box(
            modifier = Modifier
                .height(36.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(24.dp))
                .background(background)
                .then(
                    if (focused) Modifier.border(1.5.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(24.dp))
                    else Modifier
                )
                .focusRequester(focusRequester)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White.copy(alpha = if (focused || selected) 1f else 0.78f),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
