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
                // 复制一份脱离 Glide 位图池，避免展示中被回收
                val safe = resource.copy(resource.config ?: Bitmap.Config.ARGB_8888, false)
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
}
