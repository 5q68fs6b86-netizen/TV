package com.fongmi.android.tv.ui.theme

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.fongmi.android.tv.App
import com.fongmi.android.tv.impl.CustomTarget
import com.fongmi.android.tv.utils.ImgUtil

/**
 * 内容氛围总线：由当前浏览内容的海报驱动页面背景的辉光颜色与模糊底图。
 * 全局单例，[com.fongmi.android.tv.ui.custom.JetStreamPageBackgroundView] 观察并平滑过渡；
 * 未推送过内容时保持主题默认辉光。
 */
object JetStreamAmbient {

    private val colorState = mutableStateOf<Int?>(null)
    private val backdropState = mutableStateOf<Bitmap?>(null)
    private var currentUrl: String? = null
    private var pending: CustomTarget<Bitmap>? = null

    val color: Int? get() = colorState.value
    val backdrop: Bitmap? get() = backdropState.value

    @JvmStatic
    fun push(url: String?) {
        if (url.isNullOrEmpty() || url == currentUrl) return
        currentUrl = url
        pending?.let { Glide.with(App.get()).clear(it) }
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (url != currentUrl) return
                // 模糊后生成新位图（同时脱离 Glide 位图池），放大显示时无锯齿
                val safe = softBlur(resource)
                backdropState.value = safe
                Palette.from(safe).maximumColorCount(8).generate { palette ->
                    if (url != currentUrl || palette == null) return@generate
                    val swatch = palette.vibrantSwatch ?: palette.dominantSwatch ?: return@generate
                    colorState.value = swatch.rgb
                }
            }
        }
        pending = target
        try {
            Glide.with(App.get()).asBitmap().disallowHardwareConfig().load(ImgUtil.getUrl(url)).override(96, 54).into(target)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 对小图做两轮盒式模糊（近似高斯），96×54 尺寸下开销可忽略；
     * 放大铺屏后为平滑柔焦，消除直接上采样的锯齿/色块。
     */
    private fun softBlur(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        repeat(2) {
            boxBlur(pixels, width, height, radius = 4, horizontal = true)
            boxBlur(pixels, width, height, radius = 4, horizontal = false)
        }
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun boxBlur(pixels: IntArray, width: Int, height: Int, radius: Int, horizontal: Boolean) {
        val lineCount = if (horizontal) height else width
        val lineLength = if (horizontal) width else height
        val window = radius * 2 + 1
        val buffer = IntArray(lineLength)
        for (line in 0 until lineCount) {
            var sumR = 0; var sumG = 0; var sumB = 0
            fun pixelAt(i: Int): Int {
                val clamped = i.coerceIn(0, lineLength - 1)
                return if (horizontal) pixels[line * width + clamped] else pixels[clamped * width + line]
            }
            for (i in -radius..radius) {
                val p = pixelAt(i)
                sumR += (p shr 16) and 0xFF
                sumG += (p shr 8) and 0xFF
                sumB += p and 0xFF
            }
            for (i in 0 until lineLength) {
                buffer[i] = 0xFF shl 24 or (sumR / window shl 16) or (sumG / window shl 8) or (sumB / window)
                val outgoing = pixelAt(i - radius)
                val incoming = pixelAt(i + radius + 1)
                sumR += ((incoming shr 16) and 0xFF) - ((outgoing shr 16) and 0xFF)
                sumG += ((incoming shr 8) and 0xFF) - ((outgoing shr 8) and 0xFF)
                sumB += (incoming and 0xFF) - (outgoing and 0xFF)
            }
            for (i in 0 until lineLength) {
                if (horizontal) pixels[line * width + i] = buffer[i] else pixels[i * width + line] = buffer[i]
            }
        }
    }
}
