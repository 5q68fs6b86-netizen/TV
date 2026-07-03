package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.fongmi.android.tv.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class JetStreamDialogScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(28) }
        )
        elevation = jetStreamDp(12)
        clipToOutline = true
    }
}

class JetStreamSheetSurfaceLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.BOTTOM_TOP,
            cornerRadii = floatArrayOf(jetStreamDp(28), jetStreamDp(28), jetStreamDp(28), jetStreamDp(28), 0f, 0f, 0f, 0f)
        )
        elevation = jetStreamDp(10)
        clipToOutline = true
    }
}

class JetStreamDialogSurfaceLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(28) }
        )
        elevation = jetStreamDp(12)
        clipToOutline = true
    }
}

class JetStreamDialogRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(28) }
        )
        elevation = jetStreamDp(12)
        clipToOutline = true
    }
}

class JetStreamDialogEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CustomEditText(context, attrs) {

    init {
        applyJetStreamDialogInputSurface()
    }
}

class JetStreamDialogTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogInputSurface()
    }
}

class JetStreamSubtitleIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSubtitleIconSurface()
    }
}

class JetStreamDialogButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        clipToOutline = true
    }
}

class JetStreamDialogToggleGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButtonToggleGroup(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamFilterChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Chip(context, attrs, defStyleAttr) {

    init {
        minimumHeight = maxOf(minimumHeight, jetStreamDpInt(40))
        clipToOutline = true
    }
}

class JetStreamFilterChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialSwitch(context, attrs, defStyleAttr) {

    init {
        minimumWidth = maxOf(minimumWidth, jetStreamDpInt(52))
        minimumHeight = maxOf(minimumHeight, jetStreamDpInt(32))
    }
}

class JetStreamSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }
}

class JetStreamSliderTitleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogTextDefaults(attrs, defStyleAttr, R.color.jetstream_slider_title_text, 16f)
    }
}

class JetStreamSliderValueTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogTextDefaults(attrs, defStyleAttr, R.color.jetstream_slider_value_text, 14f)
    }
}

class JetStreamCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCheckBox(context, attrs, defStyleAttr)

class JetStreamCircularProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CircularProgressIndicator(context, attrs, defStyleAttr) {

    init {
        isIndeterminate = true
    }
}

class JetStreamDanmakuSectionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogSectionSurface()
    }
}

class JetStreamSliderSectionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogSectionSurface()
    }
}

open class JetStreamSettingControlRowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSettingControlRowSurface()
    }
}

class JetStreamDanmakuControlRowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : JetStreamSettingControlRowLayout(context, attrs, defStyleAttr)

class JetStreamSheetToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSheetToolbarSurface()
    }
}

class JetStreamDialogActionRailLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogActionRailSurface()
    }
}

class JetStreamDialogButtonRowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamDialogButtonRowSurface()
    }
}

class JetStreamDialogContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }
}

class JetStreamDialogSpacerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)

class JetStreamSheetChipRowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        applyJetStreamSheetChipRowSurface()
    }
}

class JetStreamDialogListRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CustomRecyclerView(context, attrs, defStyleAttr) {

    init {
        background = jetStreamOverlayBackground(
            orientation = GradientDrawable.Orientation.TL_BR,
            cornerRadii = FloatArray(8) { jetStreamDp(22) }
        )
        elevation = jetStreamDp(6)
        clipToOutline = true
    }
}

private fun TextView.applyJetStreamDialogInputSurface() {
    includeFontPadding = false
    setHintTextColor(ContextCompat.getColor(context, R.color.jetstream_list_supporting_text))
    setTextColor(ContextCompat.getColor(context, R.color.jetstream_list_title_text))
    background = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamDialogInputDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(), jetStreamDialogInputDrawable(R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
}

private fun MaterialTextView.applyJetStreamDialogTextDefaults(
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defaultTextColorRes: Int,
    defaultTextSizeSp: Float
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

private fun View.applyJetStreamSubtitleIconSurface() {
    background = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused, android.R.attr.state_pressed), jetStreamSubtitleIconDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamSubtitleIconDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_focused), jetStreamSubtitleIconDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(), jetStreamSubtitleIconDrawable(R.color.jetstream_surface_container_high, R.color.jetstream_outline_variant, 1))
    }
    minimumWidth = jetStreamDpInt(40)
    minimumHeight = jetStreamDpInt(40)
}

private fun LinearLayoutCompat.applyJetStreamDialogSectionSurface() {
    background = jetStreamOverlayBackground(
        orientation = GradientDrawable.Orientation.TL_BR,
        cornerRadii = FloatArray(8) { jetStreamDp(24) }
    )
    elevation = jetStreamDp(8)
    clipToOutline = true
    if (paddingLeft == 0 && paddingTop == 0 && paddingRight == 0 && paddingBottom == 0) {
        setPadding(0, jetStreamDpInt(12), 0, jetStreamDpInt(12))
    }
}

private fun LinearLayoutCompat.applyJetStreamSettingControlRowSurface() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    background = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), jetStreamSettingControlRowDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(android.R.attr.state_pressed), jetStreamSettingControlRowDrawable(R.color.jetstream_primary_container, R.color.jetstream_primary, 2))
        addState(intArrayOf(), jetStreamSettingControlRowDrawable(R.color.jetstream_scrim_light, 0, 0))
    }
    minimumHeight = maxOf(minimumHeight, jetStreamDpInt(48))
    clipToOutline = true
    setPadding(left, top, right, bottom)
}

private fun LinearLayoutCompat.applyJetStreamSheetToolbarSurface() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    background = jetStreamSheetToolbarDrawable()
    clipToOutline = true
    setPadding(left, top, right, bottom)
}

private fun LinearLayoutCompat.applyJetStreamDialogActionRailSurface() {
    background = jetStreamOverlayBackground(
        orientation = GradientDrawable.Orientation.TL_BR,
        cornerRadii = FloatArray(8) { jetStreamDp(24) }
    )
    elevation = jetStreamDp(8)
    clipToOutline = true
    if (paddingLeft == 0 && paddingTop == 0 && paddingRight == 0 && paddingBottom == 0) {
        val padding = jetStreamDpInt(10)
        setPadding(padding, padding, padding, padding)
    }
}

private fun LinearLayoutCompat.applyJetStreamDialogButtonRowSurface() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    background = jetStreamDialogRowDrawable()
    minimumHeight = jetStreamDpInt(56)
    clipToOutline = true
    if (left == 0 && top == 0 && right == 0 && bottom == 0) {
        val padding = jetStreamDpInt(8)
        setPadding(padding, padding, padding, padding)
    } else {
        setPadding(left, top, right, bottom)
    }
}

private fun LinearLayoutCompat.applyJetStreamSheetChipRowSurface() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    background = jetStreamDialogRowDrawable()
    minimumHeight = jetStreamDpInt(56)
    clipToOutline = true
    setPadding(left, if (top == 0) jetStreamDpInt(8) else top, right, bottom)
}

private fun View.jetStreamSettingControlRowDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(18)
        setColor(ContextCompat.getColor(context, colorRes))
        if (strokeWidthDp > 0) {
            setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
        } else {
            setStroke(0, Color.TRANSPARENT)
        }
    }
}

private fun View.jetStreamSheetToolbarDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(24)
        setColor(ContextCompat.getColor(context, R.color.jetstream_scrim_light))
    }
}

private fun View.jetStreamDialogRowDrawable(): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(24)
        setColor(ContextCompat.getColor(context, R.color.jetstream_scrim_light))
    }
}

private fun View.jetStreamDialogInputDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        cornerRadius = jetStreamDp(20)
        setColor(ContextCompat.getColor(context, colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
    }
}

private fun View.jetStreamSubtitleIconDrawable(colorRes: Int, strokeColorRes: Int, strokeWidthDp: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(ContextCompat.getColor(context, colorRes))
        setStroke(jetStreamDpInt(strokeWidthDp), ContextCompat.getColor(context, strokeColorRes))
    }
}
