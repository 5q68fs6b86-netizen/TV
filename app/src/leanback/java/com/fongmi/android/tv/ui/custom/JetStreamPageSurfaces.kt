package com.fongmi.android.tv.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
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
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.NestedScrollView
import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.VerticalGridView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fongmi.android.tv.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RelativeCornerSize
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
        setFillViewport(true)
        applyJetStreamPagePanelSurface()
        overScrollMode = View.OVER_SCROLL_NEVER
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
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
        background = jetStreamHomeLogoBackground()
        foreground = jetStreamHomeLogoForeground()
        scaleType = ScaleType.CENTER_CROP
        elevation = jetStreamDp(1)
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
        minHeight = jetStreamDpInt(24)
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_on_surface,
            defaultTextSizeSp = 12f
        )
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 12)
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_CARD, 12)
    }
}

class JetStreamPosterImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(jetStreamDp(16)).build()
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 16)
        scaleType = ScaleType.CENTER_CROP
        clipToOutline = true
    }
}

class JetStreamPosterOverlayImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(jetStreamDp(16)).build()
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 16, colorRes = R.color.jetstream_overlay_surface)
        scaleType = ScaleType.CENTER
        clipToOutline = true
    }
}

class JetStreamListThumbnailImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(jetStreamDp(14)).build()
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
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(jetStreamDp(28)).build()
        background = jetStreamImagePlaceholderDrawable(cornerRadiusDp = 28)
        scaleType = ScaleType.CENTER_CROP
        clipToOutline = true
    }
}

class JetStreamAvatarImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    init {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(RelativeCornerSize(0.5f)).build()
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_CARD, 12)
    }
}

class JetStreamVodTitleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamVodTitleText(attrs, defStyleAttr)
    }
}

class JetStreamMediaItemTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamMediaItemTextSurface(attrs, defStyleAttr)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamRoundItemTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamRoundItemSurface(attrs, defStyleAttr)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamRoundTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomTypeView(context, attrs) {

    init {
        applyJetStreamRoundItemSurface(attrs, 0)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
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

class JetStreamLargeBodyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamBodyText(attrs, defStyleAttr, defaultTextSizeSp = 18f)
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

class JetStreamLargeLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(attrs, defStyleAttr, defaultTextSizeSp = 18f)
    }
}

class JetStreamStrongLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(attrs, defStyleAttr, defaultTextStyle = Typeface.BOLD)
    }
}

class JetStreamFunctionLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_control_text,
            defaultTextSizeSp = 18f
        )
    }
}

class JetStreamHomeClockTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_on_surface_variant,
            defaultTextSizeSp = 14f
        )
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

class JetStreamChannelNumberTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        fontFeatureSettings = "tnum"
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_list_supporting_text,
            defaultTextSizeSp = 14f
        )
    }
}

class JetStreamLargeSupportingLabelTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_list_supporting_text,
            defaultTextSizeSp = 16f
        )
    }
}

class JetStreamHeroTitleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_on_surface,
            defaultTextSizeSp = 36f
        )
        applyJetStreamTextShadow()
    }
}

class JetStreamHeroMetaTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_on_surface_variant,
            defaultTextSizeSp = 16f
        )
        applyJetStreamTextShadow()
    }
}

class JetStreamHeroActionTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamLabelText(
            attrs,
            defStyleAttr,
            defaultTextColorRes = R.color.jetstream_on_primary,
            defaultTextSizeSp = 14f,
            defaultTextStyle = Typeface.BOLD
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamChipRoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipTextSurface(cornerRadiusDp = 28, horizontalPaddingDp = 14, minHeightDp = 40, attrs = attrs, defStyleAttr = defStyleAttr)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamMediumChipRoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipTextSurface(
            cornerRadiusDp = 28,
            horizontalPaddingDp = 14,
            minHeightDp = 40,
            attrs = attrs,
            defStyleAttr = defStyleAttr,
            defaultTextSizeSp = 16f
        )
    }
}

class JetStreamLargeChipRoundTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipTextSurface(
            cornerRadiusDp = 28,
            horizontalPaddingDp = 14,
            minHeightDp = 40,
            attrs = attrs,
            defStyleAttr = defStyleAttr,
            defaultTextSizeSp = 18f
        )
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
        scaleType = ScaleType.CENTER_INSIDE
    }
}

class JetStreamFileIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSearchIconSurface()
        applyJetStreamFileIconTint()
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
        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.letterSpacing), 0, 0)
        val hasLetterSpacing = typedArray.hasValue(0)
        typedArray.recycle()
        if (!hasLetterSpacing) letterSpacing = 0f
        setHintTextColor(ContextCompat.getColor(context, R.color.jetstream_list_supporting_text))
        setTextColor(ContextCompat.getColor(context, R.color.jetstream_list_title_text))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        if (hasNoPadding()) setPadding(jetStreamDpInt(10), 0, jetStreamDpInt(10), 0)
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
        applyJetStreamControlIconTint()
        scaleType = ScaleType.CENTER
    }
}

class JetStreamChipRoundIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamChipIconSurface()
        applyJetStreamControlIconTint()
    }
}

class JetStreamFunctionIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        adjustViewBounds = true
        scaleType = ScaleType.CENTER_INSIDE
        alpha = 0.9f
        applyJetStreamControlIconTint()
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
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamButtonTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamButtonSurface(attrs, defStyleAttr)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamPrimaryButtonTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamButtonSurface(attrs, defStyleAttr)
        setTypeface(typeface, Typeface.BOLD)
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamControlUpDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomUpDownView(context, attrs) {

    init {
        applyJetStreamControlSurface()
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

class JetStreamNumericControlUpDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomUpDownView(context, attrs) {

    init {
        fontFeatureSettings = "tnum"
        applyJetStreamControlSurface()
        JetStreamAnimator.bindFocus(this, JetStreamAnimator.FOCUS_SCALE_LIST, 8)
    }
}

private fun ViewGroup.applyJetStreamScrollableSurface() {
    clipChildren = false
    clipToPadding = false
    overScrollMode = View.OVER_SCROLL_NEVER
}

private fun View.applyJetStreamPagePanelSurface() {
    if (this is ViewGroup) {
        clipChildren = false
        clipToPadding = false
    }
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

@SuppressLint("ResourceType")
private fun MaterialTextView.applyJetStreamBodyText(
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defaultTextSizeSp: Float = 16f
) {
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
    if (!hasTextSize) setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultTextSizeSp)
    if (!hasLetterSpacing) letterSpacing = 0.02f
    if (!hasLineSpacingExtra) setLineSpacing(jetStreamDp(6), 1f)
    includeFontPadding = false
}

@SuppressLint("ResourceType")
private fun MaterialTextView.applyJetStreamLabelText(
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defaultTextColorRes: Int = R.color.jetstream_list_title_text,
    defaultTextSizeSp: Float = 16f,
    defaultTextStyle: Int? = null
) {
    val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.textAppearance, android.R.attr.textStyle), defStyleAttr, 0)
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasTextAppearance = typedArray.hasValue(2)
    val hasTextStyle = typedArray.hasValue(3)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(ContextCompat.getColor(context, defaultTextColorRes))
    if (!hasTextSize && !hasTextAppearance) setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultTextSizeSp)
    if (defaultTextStyle != null && !hasTextStyle) setTypeface(typeface, defaultTextStyle)
    includeFontPadding = false
}

@SuppressLint("ResourceType")
private fun MaterialTextView.applyJetStreamVodTitleText(attrs: AttributeSet?, defStyleAttr: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.textAppearance, android.R.attr.textStyle), defStyleAttr, 0)
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasTextAppearance = typedArray.hasValue(2)
    val hasTextStyle = typedArray.hasValue(3)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(jetStreamVodTitleTextColor())
    if (!hasTextSize && !hasTextAppearance) setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    if (!hasTextStyle) setTypeface(typeface, Typeface.BOLD)
    includeFontPadding = false
}

private fun MaterialTextView.applyJetStreamChipTextSurface(
    cornerRadiusDp: Int,
    horizontalPaddingDp: Int,
    minHeightDp: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defaultTextSizeSp: Float = 14f
) {
    background = jetStreamChipBackground(cornerRadiusDp)
    minHeight = jetStreamDpInt(minHeightDp)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr, defaultTextSizeSp = defaultTextSizeSp)
    if (hasNoPadding()) setPadding(jetStreamDpInt(horizontalPaddingDp), jetStreamDpInt(8), jetStreamDpInt(horizontalPaddingDp), jetStreamDpInt(8))
}

private fun MaterialTextView.applyJetStreamControlSurface() {
    background = null
    minHeight = jetStreamDpInt(40)
    applyJetStreamControlTextDefaults(null, 0)
    if (hasNoPadding()) setPadding(jetStreamDpInt(8), jetStreamDpInt(8), jetStreamDpInt(8), jetStreamDpInt(8))
    isFocusable = true
    isFocusableInTouchMode = true
    gravity = Gravity.CENTER
    includeFontPadding = false
    if (nextFocusUpId == View.NO_ID) nextFocusUpId = androidx.media3.ui.R.id.exo_progress
    setSingleLine(true)
}

private fun MaterialTextView.applyJetStreamButtonSurface(attrs: AttributeSet?, defStyleAttr: Int) {
    background = jetStreamButtonBackground()
    gravity = Gravity.CENTER
    includeFontPadding = false
    minHeight = jetStreamDpInt(40)
    applyJetStreamControlTextDefaults(attrs, defStyleAttr)
    if (hasNoPadding()) setPadding(jetStreamDpInt(16), jetStreamDpInt(9), jetStreamDpInt(16), jetStreamDpInt(9))
}

@SuppressLint("ResourceType")
private fun MaterialTextView.applyJetStreamControlTextDefaults(attrs: AttributeSet?, defStyleAttr: Int, defaultTextSizeSp: Float = 14f) {
    val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor, android.R.attr.textSize, android.R.attr.textAppearance), defStyleAttr, 0)
    val hasTextColor = typedArray.hasValue(0)
    val hasTextSize = typedArray.hasValue(1)
    val hasTextAppearance = typedArray.hasValue(2)
    typedArray.recycle()

    if (!hasTextColor) setTextColor(ContextCompat.getColor(context, R.color.jetstream_control_text))
    if (!hasTextSize && !hasTextAppearance) setTextSize(TypedValue.COMPLEX_UNIT_SP, defaultTextSizeSp)
}

private fun MaterialTextView.applyJetStreamTextShadow() {
    setShadowLayer(2f, 1.5f, 1.5f, ContextCompat.getColor(context, R.color.jetstream_overlay_surface))
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

private fun AppCompatImageView.applyJetStreamControlIconTint() {
    if (ImageViewCompat.getImageTintList(this) == null) {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.jetstream_control_text)))
    }
}

private fun AppCompatImageView.applyJetStreamFileIconTint() {
    if (ImageViewCompat.getImageTintList(this) == null) {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.jetstream_file_icon_tint)))
    }
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

private fun View.jetStreamHomeLogoBackground(): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(ContextCompat.getColor(context, R.color.jetstream_surface_container_high))
    }
}

private fun View.jetStreamHomeLogoForeground(): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.TRANSPARENT)
        setStroke(jetStreamDpInt(1), ContextCompat.getColor(context, R.color.jetstream_outline_variant))
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
