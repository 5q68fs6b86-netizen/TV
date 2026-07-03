package com.fongmi.android.tv.ui.custom

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.fongmi.android.tv.R

internal fun View.jetStreamOverlayBackground(
    orientation: GradientDrawable.Orientation,
    cornerRadii: FloatArray
): GradientDrawable {
    return GradientDrawable(
        orientation,
        intArrayOf(
            ContextCompat.getColor(context, R.color.jetstream_overlay_surface),
            ContextCompat.getColor(context, R.color.jetstream_overlay_surface_light)
        )
    ).apply {
        this.cornerRadii = cornerRadii
        setStroke(jetStreamDpInt(1), ContextCompat.getColor(context, R.color.jetstream_outline_variant))
    }
}

internal fun View.jetStreamDp(value: Int): Float {
    return value * resources.displayMetrics.density
}

internal fun View.jetStreamDpInt(value: Int): Int {
    return jetStreamDp(value).toInt()
}
