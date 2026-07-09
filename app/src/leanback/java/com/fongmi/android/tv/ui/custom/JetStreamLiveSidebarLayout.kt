package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.fongmi.android.tv.R

class JetStreamLiveSidebarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.LEFT_RIGHT,
            cornerRadii = floatArrayOf(0f, 0f, jetStreamDp(28), jetStreamDp(28), jetStreamDp(28), jetStreamDp(28), 0f, 0f)
        )
        elevation = jetStreamDp(12)
        clipToOutline = true
    }
}

class JetStreamLiveItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLiveItemSurface()
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamLiveLeftRightLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomLeftRightLayout(context, attrs) {

    init {
        applyJetStreamLiveItemSurface()
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

private fun View.applyJetStreamLiveItemSurface() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    background = jetStreamLiveItemBackground()
    clipToOutline = true
    setPadding(left, top, right, bottom)
}

private fun View.jetStreamLiveItemBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamLiveItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamLiveItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_selected), jetStreamLiveItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_checked), jetStreamLiveItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_activated), jetStreamLiveItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(), jetStreamLiveItemDrawable(R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamLiveItemDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(18)
        setColor(jetStreamColor(colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), jetStreamColor(strokeColorRes))
    }
}
