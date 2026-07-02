package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
    startColor: Color? = null,
    endColor: Color? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedStartColor = startColor ?: colorScheme.background.copy(alpha = 0.20f)
    val resolvedEndColor = endColor ?: colorScheme.surface.copy(alpha = 0.92f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(resolvedStartColor, resolvedEndColor)
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
    startColor: Color? = null,
    middleColor: Color? = null,
    endColor: Color? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedStartColor = startColor ?: colorScheme.surface.copy(alpha = 0.88f)
    val resolvedMiddleColor = middleColor ?: colorScheme.primaryContainer.copy(alpha = 0.24f)
    val resolvedEndColor = endColor ?: colorScheme.tertiaryContainer.copy(alpha = 0.12f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(resolvedStartColor, resolvedMiddleColor, resolvedEndColor)
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorScheme.background.copy(alpha = 0.20f),
                        colorScheme.surface.copy(alpha = 0.92f)
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorScheme.background.copy(alpha = 0.66f),
                        colorScheme.surface.copy(alpha = 0.14f),
                        colorScheme.background.copy(alpha = 0.54f)
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorScheme.background.copy(alpha = 0.82f),
                        colorScheme.surface.copy(alpha = 0.46f),
                        colorScheme.background.copy(alpha = 0.78f)
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
    edgeColor: Color? = null
) {
    val resolvedEdgeColor = edgeColor ?: MaterialTheme.colorScheme.background.copy(alpha = 0.60f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val radius = maxOf(size.width, size.height) / 1.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(centerColor, resolvedEdgeColor),
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colorScheme.background.copy(alpha = 0.54f),
                        colorScheme.surface.copy(alpha = 0.92f)
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface.copy(alpha = 0.92f),
                        colorScheme.background.copy(alpha = 0.54f),
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
