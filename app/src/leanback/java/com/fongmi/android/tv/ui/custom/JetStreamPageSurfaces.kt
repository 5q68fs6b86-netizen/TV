package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.VerticalGridView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fongmi.android.tv.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class JetStreamPageRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamPageSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SwipeRefreshLayout(context, attrs) {

    init {
        clipChildren = false
        clipToPadding = false
        setProgressBackgroundColorSchemeResource(R.color.jetstream_surface_container_high)
        setColorSchemeResources(R.color.jetstream_primary)
    }
}

class JetStreamPageProgressLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressLayout(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamPagePanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamPagePanelSurface()
    }
}

class JetStreamPageContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamPageRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamHorizontalGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalGridView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamVerticalGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VerticalGridView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamTypeGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CustomVerticalGridView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamPageContentScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamPageViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomViewPager(context, attrs) {

    init {
        applyJetStreamScrollableSurface()
    }
}

class JetStreamPagePanelScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamPagePanelSurface()
    }
}

class JetStreamProgressPanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressLayout(context, attrs, defStyleAttr) {

    init {
        applyJetStreamPagePanelSurface()
    }
}

class JetStreamProgressStateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        val left = paddingLeft
        val top = paddingTop
        val right = paddingRight
        val bottom = paddingBottom
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(32) }
        )
        elevation = jetStreamDp(12)
        clipToOutline = true
        setPadding(left, top, right, bottom)
    }
}

class JetStreamListItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(18) }
        )
        foreground = jetStreamFocusForeground(cornerRadiusDp = 18, strokeWidthDp = 3)
        elevation = jetStreamDp(4)
        clipToOutline = true
    }
}

class JetStreamListItemContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamHomeLogoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(20) }
        )
        elevation = jetStreamDp(2)
        clipToOutline = true
    }
}

class JetStreamHomeTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomTitleView(context, attrs) {

    init {
        includeFontPadding = false
        setTextColor(ContextCompat.getColor(context, R.color.jetstream_on_surface_variant))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    }
}

class JetStreamMetadataPillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(12) }
        )
        gravity = Gravity.CENTER
        includeFontPadding = false
        minHeight = jetStreamDpInt(24)
        setPadding(jetStreamDpInt(8), jetStreamDpInt(4), jetStreamDpInt(8), jetStreamDpInt(4))
    }
}

class JetStreamHeroActionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = GradientDrawable().apply {
            cornerRadius = jetStreamDp(18)
            setColor(ContextCompat.getColor(context, R.color.jetstream_primary))
        }
        elevation = jetStreamDp(6)
        clipToOutline = true
    }
}

class JetStreamHeroIndicatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(18) }
        )
        elevation = jetStreamDp(4)
        clipToOutline = true
    }
}

class JetStreamFeaturedIndicatorDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        background = jetStreamIndicatorDotBackground()
    }
}

class JetStreamFeaturedVodLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        foreground = jetStreamFocusForeground(cornerRadiusDp = 28, strokeWidthDp = 3)
    }
}

class JetStreamFeaturedLeftScrimView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        background = jetStreamScrimBackground(
            orientation = GradientDrawable.Orientation.LEFT_RIGHT,
            colors = intArrayOf(
                ContextCompat.getColor(context, R.color.jetstream_overlay_surface),
                ContextCompat.getColor(context, R.color.jetstream_overlay_surface_light),
                Color.TRANSPARENT
            ),
            cornerRadiusDp = 28
        )
    }
}

class JetStreamFeaturedBottomScrimView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        background = jetStreamScrimBackground(
            orientation = GradientDrawable.Orientation.TOP_BOTTOM,
            colors = intArrayOf(Color.TRANSPARENT, ContextCompat.getColor(context, R.color.jetstream_overlay_surface)),
            cornerRadiusDp = 28
        )
    }
}

class JetStreamVodPosterLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        foreground = jetStreamFocusForeground(cornerRadiusDp = 16, strokeWidthDp = 3)
    }
}

class JetStreamVodCardRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamPosterImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 16)
        clipToOutline = true
    }
}

class JetStreamPosterOverlayImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 16, colorRes = R.color.jetstream_overlay_surface)
        clipToOutline = true
    }
}

class JetStreamListThumbnailImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 14)
        scaleType = ScaleType.CENTER_CROP
        clipToOutline = true
    }
}

class JetStreamHeroImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 28)
        clipToOutline = true
    }
}

class JetStreamAvatarImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamAvatarPlaceholderDrawable()
        clipToOutline = true
    }
}

class JetStreamVodScrimView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.TRANSPARENT, ContextCompat.getColor(context, R.color.jetstream_overlay_surface))
        ).apply {
            cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, jetStreamDp(16), jetStreamDp(16), jetStreamDp(16), jetStreamDp(16))
        }
    }
}

class JetStreamVodOvalFocusLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOvalFocusBackground()
    }
}

class JetStreamVodOvalRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamVodTitleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        setTextColor(jetStreamVodTitleTextColor())
    }
}

class JetStreamMediaItemTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamMediaItemTextSurface(attrs, defStyleAttr)
    }
}

class JetStreamMediaItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamMediaItemBackground()
        setPadding(jetStreamDpInt(18), jetStreamDpInt(9), jetStreamDpInt(18), jetStreamDpInt(9))
    }
}

class JetStreamRoundItemTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamRoundItemSurface(attrs, defStyleAttr)
    }
}

class JetStreamRoundTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomTypeView(context, attrs) {

    init {
        applyJetStreamRoundItemSurface(attrs, 0)
    }
}

class JetStreamBodyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamBodyText(attrs, defStyleAttr)
    }
}

class JetStreamLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(attrs, defStyleAttr)
    }
}

class JetStreamSupportingLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_list_supporting_text,
            defaultTextSizeSp = 14f
        )
    }
}

class JetStreamChipTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipTextSurface(cornerRadiusDp = 18, horizontalPaddingDp = 12, minHeightDp = 36, attrs = attrs, defStyleAttr = defStyleAttr)
    }
}

class JetStreamChipRoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipTextSurface(cornerRadiusDp = 28, horizontalPaddingDp = 14, minHeightDp = 40, attrs = attrs, defStyleAttr = defStyleAttr)
    }
}

class JetStreamChipRoundLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipContainerSurface()
    }
}

class JetStreamItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamItemSurface()
    }
}

class JetStreamSearchFieldLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamSearchBackground(cornerRadiusDp = 28)
    }
}

class JetStreamSearchIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSearchIconSurface()
    }
}

class JetStreamSearchMicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomMic(context, attrs) {

    init {
        applyJetStreamSearchIconSurface()
    }
}

class JetStreamSearchInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomSearchView(context, attrs) {

    init {
        background = null
        includeFontPadding = false
        setHintTextColor(ContextCompat.getColor(context, R.color.jetstream_list_supporting_text))
        setTextColor(ContextCompat.getColor(context, R.color.jetstream_list_title_text))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
    }
}

class JetStreamKeyboardTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamKeyboardTextSurface(attrs, defStyleAttr)
    }
}

class JetStreamKeyboardIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamKeyboardSurface()
    }
}

class JetStreamChipRoundIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipIconSurface()
    }
}

class JetStreamInlineIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        adjustViewBounds = true
        scaleType = ScaleType.CENTER_INSIDE
        alpha = 0.9f
    }
}

class JetStreamLogoImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        adjustViewBounds = true
        scaleType = ScaleType.FIT_CENTER
        alpha = 0.96f
    }
}

class JetStreamControlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamControlSurface()
    }
}

class JetStreamButtonTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamButtonSurface(attrs, defStyleAttr)
    }
}

class JetStreamControlUpDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomUpDownView(context, attrs) {

    init {
        applyJetStreamControlSurface()
    }
}

private fun ViewGroup.applyJetStreamScrollableSurface() {
    clipChildren = false
    clipToPadding = false
    overScrollMode = View.OVER_SCROLL_NEVER
}

private fun View.applyJetStreamPagePanelSurface() {
    background = jetStreamOverlayBackground(
        orientation = GradientDrawable.Orientation.TL_BR,
        cornerRadii = FloatArray(8) { jetStreamDp(28) }
    )
    elevation = jetStreamDp(8)
    clipToOutline = true
}

private fun MaterialTextView.applyJetStreamMediaItemTextSurface(attrs: AttributeSet?, defStyleAttr: Int) {
    background = jetStreamMediaItemBackground()
    gravity = Gravity.CENTER
    includeFontPadding = false
    minHeight = jetStreamDpInt(42)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr, defaultTextSizeSp = 16f)
    if (hasNoPadding()) setPadding(jetStreamDpInt(18), jetStreamDpInt(9), jetStreamDpInt(18), jetStreamDpInt(9))
}

private fun MaterialTextView.applyJetStreamRoundItemSurface(attrs: AttributeSet?, defStyleAttr: Int) {
    background = jetStreamRoundItemBackground()
    gravity = Gravity.CENTER
    includeFontPadding = false
    minHeight = jetStreamDpInt(40)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr, defaultTextSizeSp = 16f)
    if (hasNoPadding()) setPadding(jetStreamDpInt(14), jetStreamDpInt(8), jetStreamDpInt(14), jetStreamDpInt(8))
}

private fun MaterialTextView.applyJetStreamBodyText(attrs: AttributeSet?, defStyleAttr: Int) {
    val typedArray = context.obtainStyledAttributes(
        attrs,
        intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.letterSpacing, android.R.attr.lineSpacingExtra),
        defStyleAttr,
        0
    )
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasLetterSpacing = typedArray.hasValue(2)
    val hasLineSpacingExtra = typedArray.hasValue(3)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(ContextCompat.getColor(context, R.color.jetstream_on_surface_variant))
    if (!hasTextSize) setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    if (!hasLetterSpacing) letterSpacing = 0.02f
    if (!hasLineSpacingExtra) setLineSpacing(jetStreamDp(6), 1f)
    includeFontPadding = false
}

private fun MaterialTextView.applyJetStreamLabelText(
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defaultTextColorRes: Int = R.color.jetstream_list_title_text,
    defaultTextSizeSp: Float = 16f
) {
    val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.textAppearance), defStyleAttr, 0)
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasTextAppearance = typedArray.hasValue(2)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(ContextCompat.getColor(context, defaultTextColorRes))
    if (!hasTextSize && !hasTextAppearance) setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultTextSizeSp)
    includeFontPadding = false
}

private fun MaterialTextView.applyJetStreamChipTextSurface(cornerRadiusDp: Int, horizontalPaddingDp: Int, minHeightDp: Int, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
    background = jetStreamChipBackground(cornerRadiusDp)
    minHeight = jetStreamDpInt(minHeightDp)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr)
    if (hasNoPadding()) setPadding(jetStreamDpInt(horizontalPaddingDp), jetStreamDpInt(8), jetStreamDpInt(horizontalPaddingDp), jetStreamDpInt(8))
}

private fun MaterialTextView.applyJetStreamControlSurface() {
    applyJetStreamChipTextSurface(cornerRadiusDp = 28, horizontalPaddingDp = 14, minHeightDp = 40)
    gravity = Gravity.CENTER
    includeFontPadding = false
}

private fun MaterialTextView.applyJetStreamButtonSurface(attrs: AttributeSet?, defStyleAttr: Int) {
    background = jetStreamButtonBackground()
    gravity = Gravity.CENTER
    includeFontPadding = false
    minHeight = jetStreamDpInt(40)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr)
    if (hasNoPadding()) setPadding(jetStreamDpInt(16), jetStreamDpInt(9), jetStreamDpInt(16), jetStreamDpInt(9))
}

private fun MaterialTextView.applyJetStreamControlTextDefaults(attrs: AttributeSet?, defStyleAttr: Int, defaultTextSizeSp: Float = 14f) {
    val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.textAppearance), defStyleAttr, 0)
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasTextAppearance = typedArray.hasValue(2)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(ContextCompat.getColor(context, R.color.jetstream_control_text))
    if (!hasTextSize && !hasTextAppearance) setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultTextSizeSp)
}

private fun View.applyJetStreamChipContainerSurface() {
    background = jetStreamChipBackground(cornerRadiusDp = 28)
    minimumHeight = jetStreamDpInt(40)
    if (hasNoPadding()) setPadding(jetStreamDpInt(14), jetStreamDpInt(8), jetStreamDpInt(14), jetStreamDpInt(8))
}

private fun View.applyJetStreamItemSurface() {
    background = jetStreamChipBackground(cornerRadiusDp = 18)
    minimumHeight = jetStreamDpInt(40)
    if (hasNoPadding()) setPadding(jetStreamDpInt(12), jetStreamDpInt(8), jetStreamDpInt(12), jetStreamDpInt(8))
}

private fun AppCompatImageView.applyJetStreamChipIconSurface() {
    background = jetStreamChipBackground(cornerRadiusDp = 28)
    minimumWidth = jetStreamDpInt(40)
    minimumHeight = jetStreamDpInt(40)
    if (hasNoPadding()) setPadding(jetStreamDpInt(14), jetStreamDpInt(8), jetStreamDpInt(14), jetStreamDpInt(8))
}

private fun View.applyJetStreamSearchIconSurface() {
    background = jetStreamSearchBackground(cornerRadiusDp = 22)
    minimumWidth = jetStreamDpInt(44)
    minimumHeight = jetStreamDpInt(44)
}

private fun View.applyJetStreamKeyboardSurface() {
    background = jetStreamSearchBackground(cornerRadiusDp = 20)
    minimumWidth = jetStreamDpInt(40)
    minimumHeight = jetStreamDpInt(40)
}

private fun MaterialTextView.applyJetStreamKeyboardTextSurface(attrs: AttributeSet?, defStyleAttr: Int) {
    applyJetStreamKeyboardSurface()
    gravity = Gravity.CENTER
    includeFontPadding = false
    applyJetStreamControlTextDefaults(attrs, defStyleAttr, defaultTextSizeSp = 16f)
}

private fun View.hasNoPadding(): Boolean {
    return paddingLeft == 0 && paddingTop == 0 && paddingRight == 0 && paddingBottom == 0
}

private fun View.jetStreamMediaItemBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamMediaItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamMediaItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_selected), jetStreamMediaItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_checked), jetStreamMediaItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_activated), jetStreamMediaItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(), jetStreamMediaItemDrawable(R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamMediaItemDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(18)
        setColor(ContextCompat.getColor(context, colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
    }
}

private fun View.jetStreamRoundItemBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamRoundItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamRoundItemDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_selected), jetStreamRoundItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_checked), jetStreamRoundItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_activated), jetStreamRoundItemDrawable(R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(), jetStreamRoundItemDrawable(R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamRoundItemDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(28)
        setColor(ContextCompat.getColor(context, colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
    }
}

private fun View.jetStreamChipBackground(cornerRadiusDp: Int): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_selected), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_checked), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(android.R.attr.state_activated), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamChipDrawable(cornerRadiusDp: Int, colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(cornerRadiusDp)
        setColor(ContextCompat.getColor(context, colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
    }
}

private fun View.jetStreamButtonBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamChipDrawable(28, R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamChipDrawable(28, R.color.jetstream_secondary_container, R.color.jetstream_secondary, 1))
        addState(intArrayOf(), jetStreamChipDrawable(28, R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamSearchBackground(cornerRadiusDp: Int): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(), jetStreamChipDrawable(cornerRadiusDp, R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun View.jetStreamFocusForeground(cornerRadiusDp: Int, strokeWidthDp: Int): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamFocusDrawable(cornerRadiusDp, strokeWidthDp))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamFocusDrawable(cornerRadiusDp, strokeWidthDp))
    }
}

private fun View.jetStreamFocusDrawable(cornerRadiusDp: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(cornerRadiusDp)
        setColor(Color.TRANSPARENT)
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, R.color.jetstream_primary))
    }
}

private fun View.jetStreamImagePlaceholderDrawable(cornerRadiusDp: Int, colorRes: Int = R.color.jetstream_surface_container_high): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(cornerRadiusDp)
        setColor(ContextCompat.getColor(context, colorRes))
    }
}

private fun View.jetStreamAvatarPlaceholderDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(ContextCompat.getColor(context, R.color.jetstream_surface_container_high))
    }
}

private fun View.jetStreamOvalFocusBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamOvalFocusDrawable())
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamOvalFocusDrawable())
    }
}

private fun View.jetStreamOvalFocusDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.TRANSPARENT)
        setStroke(jetStreamDpInt(3), ContextCompat.getColor(context, R.color.jetstream_primary))
    }
}

private fun View.jetStreamScrimBackground(
    orientation: GradientDrawable.Orientation,
    colors: IntArray,
    cornerRadiusDp: Int
): GradientDrawable {
    return GradientDrawable(orientation, colors).apply {
        cornerRadius = jetStreamDp(cornerRadiusDp)
    }
}

private fun View.jetStreamVodTitleTextColor(): ColorStateList {
    return ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf()
        ),
        intArrayOf(
            ContextCompat.getColor(context, R.color.jetstream_on_surface),
            ContextCompat.getColor(context, R.color.jetstream_on_surface),
            ContextCompat.getColor(context, R.color.jetstream_on_surface_variant)
        )
    )
}

private fun View.jetStreamIndicatorDotBackground(): StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_selected), jetStreamIndicatorDotDrawable(R.color.jetstream_primary))
        addState(intArrayOf(), jetStreamIndicatorDotDrawable(R.color.jetstream_outline))
    }
}

private fun View.jetStreamIndicatorDotDrawable(colorRes: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(ContextCompat.getColor(context, colorRes))
    }
}
