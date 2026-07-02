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
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamBorders
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
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) scaleOnFocus else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "chipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            selected -> colorScheme.secondaryContainer
            focused -> colorScheme.primaryContainer
            else -> colorScheme.surfaceVariant.copy(alpha = 0.68f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "chipBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (focused) colorScheme.primary.copy(alpha = 0.86f) else Color.Transparent,
        animationSpec = JetStreamAnimations.ColorTween,
        label = "chipBorder"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            selected -> colorScheme.onSecondaryContainer
            focused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "chipContent"
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
            .then(
                if (focused) Modifier.border(
                    JetStreamBorders.Medium,
                    borderColor,
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
                    tint = contentColor
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
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
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "filterChipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            selected -> colorScheme.primaryContainer
            focused -> colorScheme.secondaryContainer
            else -> colorScheme.surfaceVariant.copy(alpha = 0.68f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "filterChipBackground"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            selected -> colorScheme.onPrimaryContainer
            focused -> colorScheme.onSecondaryContainer
            else -> colorScheme.onSurfaceVariant
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
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "actionChipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            focused -> colorScheme.primaryContainer
            else -> colorScheme.surfaceVariant.copy(alpha = 0.78f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "actionChipBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            focused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "actionChipContent"
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
            tint = contentColor
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = contentColor,
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
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "iconChipScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            selected -> colorScheme.secondaryContainer
            focused -> colorScheme.primaryContainer
            else -> colorScheme.surfaceVariant.copy(alpha = 0.82f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "iconChipBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            selected -> colorScheme.onSecondaryContainer
            focused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "iconChipContent"
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
            tint = contentColor
        )
    }
}
