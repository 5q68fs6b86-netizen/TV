package com.fongmi.android.tv.ui.custom

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.fongmi.android.tv.ui.theme.JetStreamPalette

object JetStreamAnimator {

    const val FOCUS_DURATION = 180L
    const val PANEL_DURATION = 220L
    const val PAGE_DURATION = 260L
    const val EXIT_DURATION = 160L

    const val FOCUS_SCALE_CARD = 1.08f
    const val FOCUS_SCALE_LIST = 1.05f
    const val FOCUS_SCALE_VIDEO = 1.03f

    private val enterInterpolator = DecelerateInterpolator(1.8f)
    private val exitInterpolator = AccelerateInterpolator(1.2f)

    @JvmStatic
    @JvmOverloads
    fun bindFocus(view: View, scale: Float = FOCUS_SCALE_CARD, elevationDp: Int = 12) {
        view.setOnFocusChangeListener { target, focused ->
            animateFocus(target, focused, scale, elevationDp)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun animateFocus(view: View, focused: Boolean, scale: Float = FOCUS_SCALE_CARD, elevationDp: Int = 12, duration: Long = FOCUS_DURATION) {
        view.animate().cancel()
        view.isSelected = focused
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val shadowColor = if (focused) JetStreamPalette.shadowColor() else Color.TRANSPARENT
            view.outlineAmbientShadowColor = shadowColor
            view.outlineSpotShadowColor = shadowColor
        }
        view.animate()
            .scaleX(if (focused) scale else 1f)
            .scaleY(if (focused) scale else 1f)
            .translationZ(if (focused) dp(view, elevationDp) else 0f)
            .setInterpolator(enterInterpolator)
            .setDuration(duration)
            .start()
    }

    @JvmStatic
    fun reset(view: View) {
        view.animate().cancel()
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = 0f
        view.translationY = 0f
        view.translationZ = 0f
        view.isSelected = false
    }

    @JvmStatic
    @JvmOverloads
    fun show(view: View, fromXDp: Int = 0, fromYDp: Int = 12, duration: Long = PANEL_DURATION) {
        // cancel() alone can still run a previous withEndAction and force GONE after show.
        view.animate().cancel()
        view.animate().setListener(null)
        view.animate().withEndAction(null)
        // 必须在 cancel() 之后采样：上一次 hide 的 withEndAction 会被 cancel 同步触发（置 GONE、alpha=1、位移归零），先采样会误判早退致视图停在 GONE
        val alreadyVisible = view.visibility == View.VISIBLE
        if (alreadyVisible && view.alpha >= 0.99f && view.translationX == 0f && view.translationY == 0f) return
        if (!alreadyVisible) {
            view.alpha = 0f
            view.translationX = dp(view, fromXDp)
            view.translationY = dp(view, fromYDp)
        }
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .translationX(0f)
            .translationY(0f)
            .setInterpolator(enterInterpolator)
            .setDuration(duration)
            .withEndAction(null)
            .start()
    }

    @JvmStatic
    @JvmOverloads
    fun hide(view: View, toXDp: Int = 0, toYDp: Int = 12, finalVisibility: Int = View.GONE, duration: Long = EXIT_DURATION) {
        if (view.visibility != View.VISIBLE) {
            view.visibility = finalVisibility
            return
        }
        view.animate().cancel()
        view.animate().setListener(null)
        view.animate().withEndAction(null)
        view.animate()
            .alpha(0f)
            .translationX(dp(view, toXDp))
            .translationY(dp(view, toYDp))
            .setInterpolator(exitInterpolator)
            .setDuration(duration)
            .withEndAction {
                view.visibility = finalVisibility
                view.alpha = 1f
                view.translationX = 0f
                view.translationY = 0f
            }
            .start()
    }

    private fun dp(view: View, value: Int): Float {
        return value * view.resources.displayMetrics.density
    }
}
