package com.fongmi.android.tv.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.fongmi.android.tv.R

/**
 * 骨架屏加载行：一排海报形占位卡 + 扫过的微光，替代居中转圈。
 * 纯自绘，无子 View，附着即动画、离屏即停止。
 */
class JetStreamSkeletonRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var animator: ValueAnimator? = null
    private var shimmerProgress = 0f

    private val posterWidth = jetStreamDp(96)
    private val posterHeight = jetStreamDp(128)
    private val titleHeight = jetStreamDp(14)
    private val titleGap = jetStreamDp(10)
    private val spacing = jetStreamDp(18)
    private val cornerPoster = jetStreamDp(16)
    private val cornerTitle = jetStreamDp(7)

    init {
        cardPaint.color = jetStreamColor(R.color.jetstream_surface_container_high)
        isFocusable = false
        isFocusableInTouchMode = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = (posterHeight + titleGap + titleHeight + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val contentWidth = width - paddingLeft - paddingRight
        if (contentWidth <= 0) return
        var x = paddingLeft.toFloat()
        val top = paddingTop.toFloat()
        while (x < paddingLeft + contentWidth) {
            rect.set(x, top, x + posterWidth, top + posterHeight)
            canvas.drawRoundRect(rect, cornerPoster, cornerPoster, cardPaint)
            val titleWidth = posterWidth * 0.72f
            rect.set(x + (posterWidth - titleWidth) / 2f, top + posterHeight + titleGap, x + (posterWidth + titleWidth) / 2f, top + posterHeight + titleGap + titleHeight)
            canvas.drawRoundRect(rect, cornerTitle, cornerTitle, cardPaint)
            x += posterWidth + spacing
        }
        drawShimmer(canvas)
    }

    private fun drawShimmer(canvas: Canvas) {
        val band = width * 0.28f
        val start = -band + (width + band * 2f) * shimmerProgress
        shimmerPaint.shader = LinearGradient(
            start, 0f, start + band, height * 0.4f,
            intArrayOf(0x00FFFFFF, 0x14FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shimmerPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                shimmerProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
