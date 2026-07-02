package com.fongmi.android.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.JetStreamAlpha
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamBorders
import com.fongmi.android.tv.ui.theme.JetStreamColors
import com.fongmi.android.tv.ui.theme.JetStreamShapes
import com.fongmi.android.tv.ui.theme.JetStreamSizes
import com.fongmi.android.tv.ui.theme.JetStreamSpacing

/**
 * JetStream Chip Component
 * 标准芯片组件，用于标签、过滤器等
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JetStreamChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    icon: ImageVector? = null,
    scaleOnFocus: Float = JetStreamAnimations.FocusScaleMedium
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) scaleOnFocus else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "chipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            selected -> Color.White.copy(alpha = JetStreamAlpha.BackgroundVeryHigh)
            focused -> Color.White.copy(alpha = JetStreamAlpha.BackgroundHigh)
            else -> Color.White.copy(alpha = JetStreamAlpha.BackgroundLight)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "chipBackground"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (focused) JetStreamAlpha.BackgroundVeryHigh else 0f,
        animationSpec = JetStreamAnimations.FloatTween,
        label = "chipBorder"
    )

    val contentAlpha = when {
        !enabled -> JetStreamAlpha.Disabled
        focused || selected -> 1f
        else -> JetStreamAlpha.Low
    }

    Box(
        modifier = modifier
            .height(JetStreamSizes.ChipMedium)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Chip)
            .background(background)
            .then(
                if (focused) Modifier.border(
                    JetStreamBorders.Medium,
                    Color.White.copy(alpha = borderAlpha),
                    JetStreamShapes.Chip
                )
                else Modifier
            )
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = JetStreamSpacing.ChipHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(JetStreamSizes.IconExtraSmall),
                    tint = Color.White.copy(alpha = contentAlpha)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color.White.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * JetStream Filter Chip
 * 过滤器芯片，带选中状态
 */
@Composable
fun JetStreamFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "filterChipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            focused -> Color.White.copy(alpha = JetStreamAlpha.BackgroundMediumHigh)
            else -> Color.White.copy(alpha = JetStreamAlpha.BackgroundLight)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "filterChipBackground"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> Color.White
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "filterChipText"
    )

    Box(
        modifier = modifier
            .height(JetStreamSizes.ChipMedium)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Chip)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = JetStreamSpacing.ChipHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JetStream Action Chip
 * 操作芯片，用于执行动作
 */
@Composable
fun JetStreamActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "actionChipScale"
    )

    val background by animateColorAsState(
        targetValue = if (focused)
            Color.White.copy(alpha = JetStreamAlpha.BackgroundHigh)
        else
            Color.White.copy(alpha = JetStreamAlpha.BackgroundLightMedium),
        animationSpec = JetStreamAnimations.ColorTween,
        label = "actionChipBackground"
    )

    Row(
        modifier = modifier
            .height(38.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Chip)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(19.dp),
            tint = Color.White.copy(alpha = if (enabled) 1f else JetStreamAlpha.Disabled)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = if (enabled) 1f else JetStreamAlpha.Disabled),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JetStream Icon Chip
 * 纯图标芯片
 */
@Composable
fun JetStreamIconChip(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "iconChipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            selected -> Color.White.copy(alpha = JetStreamAlpha.BackgroundVeryHigh)
            focused -> Color.White.copy(alpha = JetStreamAlpha.BackgroundMediumHigh)
            else -> Color.White.copy(alpha = JetStreamAlpha.BackgroundMedium)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "iconChipBackground"
    )

    Box(
        modifier = modifier
            .size(JetStreamSizes.ButtonMedium)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(CircleShape)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(JetStreamSizes.IconMedium),
            tint = Color.White.copy(alpha = if (enabled) 1f else JetStreamAlpha.Disabled)
        )
    }
}
