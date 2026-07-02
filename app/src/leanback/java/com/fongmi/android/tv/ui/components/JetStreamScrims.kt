package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fongmi.android.tv.ui.theme.JetStreamAlpha

/**
 * JetStream Scrim Overlays
 * 遮罩层组件，用于创建不同的背景效果
 */

/**
 * 垂直渐变遮罩 - 从上到下
 */
@Composable
fun JetStreamVerticalScrim(
    modifier: Modifier = Modifier,
    startColor: Color = Color.Black.copy(alpha = JetStreamAlpha.ScrimLight),
    endColor: Color = Color.Black.copy(alpha = JetStreamAlpha.ScrimHeavy),
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(startColor, endColor)
                )
            )
    ) {
        content()
    }
}

/**
 * 水平渐变遮罩 - 从左到右
 */
@Composable
fun JetStreamHorizontalScrim(
    modifier: Modifier = Modifier,
    startColor: Color = Color.White.copy(alpha = 0.16f),
    middleColor: Color = Color.White.copy(alpha = 0.08f),
    endColor: Color = Color.White.copy(alpha = 0.03f),
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(startColor, middleColor, endColor)
                )
            )
    ) {
        content()
    }
}

/**
 * 控制面板遮罩 - 用于播放器控制面板
 */
@Composable
fun JetStreamControlScrim(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimLight),
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimHeavy)
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * 信息遮罩 - 用于显示信息覆盖层
 */
@Composable
fun JetStreamInfoScrim(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.50f),
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimLight),
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimMedium)
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * 页面遮罩 - 用于设置页面等全屏场景
 */
@Composable
fun JetStreamPageScrim(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimHeavy),
                        Color.Black.copy(alpha = 0.46f),
                        Color.Black.copy(alpha = 0.78f)
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * 径向渐变遮罩 - 从中心向外
 */
@Composable
fun JetStreamRadialScrim(
    modifier: Modifier = Modifier,
    centerColor: Color = Color.Transparent,
    edgeColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val radius = maxOf(size.width, size.height) / 1.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(centerColor, edgeColor),
                center = centerOffset,
                radius = radius
            ),
            center = centerOffset,
            radius = radius
        )
    }
}

/**
 * 底部遮罩 - 用于底部渐隐效果
 */
@Composable
fun JetStreamBottomScrim(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimMedium),
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimHeavy)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        content()
    }
}

/**
 * 顶部遮罩 - 用于顶部渐隐效果
 */
@Composable
fun JetStreamTopScrim(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimHeavy),
                        Color.Black.copy(alpha = JetStreamAlpha.ScrimMedium),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        content()
    }
}
