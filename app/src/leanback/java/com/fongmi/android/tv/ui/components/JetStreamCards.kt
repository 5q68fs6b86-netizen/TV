package com.fongmi.android.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.fongmi.android.tv.ui.theme.JetStreamAlpha
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamBorders
import com.fongmi.android.tv.ui.theme.JetStreamColors
import com.fongmi.android.tv.ui.theme.JetStreamShapes
import com.fongmi.android.tv.ui.theme.JetStreamSpacing

/**
 * JetStream Card Component
 * 标准卡片组件，支持聚焦、选中、点击等交互状态
 */
@Composable
fun JetStreamCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    selected: Boolean = false,
    shape: Shape = JetStreamShapes.Card,
    backgroundColor: Color = JetStreamColors.CardBackground,
    focusedBackgroundColor: Color = JetStreamColors.CardBackgroundFocused,
    selectedBackgroundColor: Color = JetStreamColors.CardBackgroundSelected,
    borderColor: Color = Color.White.copy(alpha = JetStreamAlpha.BackgroundLight),
    focusedBorderColor: Color = Color.White.copy(alpha = JetStreamAlpha.BackgroundMedium),
    scaleOnFocus: Float = JetStreamAnimations.FocusScaleMedium,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) scaleOnFocus else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "cardScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            focused -> focusedBackgroundColor
            selected -> selectedBackgroundColor
            else -> backgroundColor
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "cardBackground"
    )

    val border by animateColorAsState(
        targetValue = if (focused) focusedBorderColor else borderColor,
        animationSpec = JetStreamAnimations.ColorTween,
        label = "cardBorder"
    )

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(shape)
            .background(background)
            .border(JetStreamBorders.Thin, border, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

/**
 * JetStream Surface Card
 * 带渐变背景的表面卡片
 */
@Composable
fun JetStreamSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape = JetStreamShapes.Card,
    gradient: Brush = Brush.horizontalGradient(
        listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.03f)
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "surfaceCardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(shape)
            .background(gradient)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

/**
 * JetStream Glass Card
 * 毛玻璃效果卡片，带阴影和边框
 */
@Composable
fun JetStreamGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = JetStreamShapes.Card,
    backgroundColor: Color = Color.Black.copy(alpha = JetStreamAlpha.ScrimMedium),
    borderColor: Color = Color.White.copy(alpha = JetStreamAlpha.BackgroundLight),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(JetStreamBorders.Thin, borderColor, shape)
    ) {
        content()
    }
}

/**
 * JetStream Elevated Card
 * 高亮卡片，用于重要内容
 */
@Composable
fun JetStreamElevatedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape = JetStreamShapes.Card,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "elevatedCardScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (focused)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = JetStreamAnimations.ColorTween,
        label = "elevatedCardBackground"
    )

    Column(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(JetStreamSpacing.CardPadding)
    ) {
        content()
    }
}

/**
 * JetStream Outlined Card
 * 带边框的卡片
 */
@Composable
fun JetStreamOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape = JetStreamShapes.Card,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "outlinedCardScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (focused)
            MaterialTheme.colorScheme.outline
        else
            MaterialTheme.colorScheme.outlineVariant,
        animationSpec = JetStreamAnimations.ColorTween,
        label = "outlinedCardBorder"
    )

    Row(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(shape)
            .border(JetStreamBorders.Medium, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(JetStreamSpacing.CardPadding)
    ) {
        content()
    }
}
