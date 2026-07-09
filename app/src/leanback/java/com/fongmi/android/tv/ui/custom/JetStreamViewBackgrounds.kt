package com.fongmi.android.tv.ui.custom

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorRes
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.theme.JetStreamPalette

internal fun View.jetStreamDialogBackground(cornerRadii: FloatArray): GradientDrawable {
    return jetStreamSolidBackground(R.color.jetstream_surface, cornerRadii)
}

internal fun View.jetStreamOverlayBackground(
    orientation: GradientDrawable.Orientation,
    cornerRadii: FloatArray
): GradientDrawable {
    return GradientDrawable(
        orientation,
        intArrayOf(
            jetStreamColor(R.color.jetstream_overlay_surface),
            jetStreamColor(R.color.jetstream_overlay_surface_light)
        )
    ).apply {
        this.cornerRadii = cornerRadii
        setStroke(jetStreamDpInt(1), jetStreamColor(R.color.jetstream_outline_variant))
    }
}

private fun View.jetStreamSolidBackground(
    @ColorRes colorRes: Int,
    cornerRadii: FloatArray
): GradientDrawable {
    return GradientDrawable().apply {
        this.cornerRadii = cornerRadii
        setColor(jetStreamColor(colorRes))
        setStroke(jetStreamDpInt(1), jetStreamColor(R.color.jetstream_outline_variant))
    }
}

internal fun View.jetStreamColor(@ColorRes colorRes: Int): Int {
    return JetStreamPalette.resolveColor(context, colorRes)
}

internal fun View.jetStreamColorStateList(@ColorRes colorRes: Int) = JetStreamPalette.resolveColorStateList(context, colorRes)

internal fun View.jetStreamDp(value: Int): Float {
    return value * resources.displayMetrics.density
}

internal fun View.jetStreamDpInt(value: Int): Int {
    return jetStreamDp(value).toInt()
}
