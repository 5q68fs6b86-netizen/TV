package com.fongmi.android.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
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
 * JetStream Button Component
 * 标准按钮组件，支持多种样式
 */
@Composable
fun JetStreamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    scaleOnFocus: Float = JetStreamAnimations.FocusScaleMedium
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) scaleOnFocus else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "buttonScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            focused -> colorScheme.primaryContainer
            else -> colorScheme.surfaceVariant.copy(alpha = 0.78f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "buttonBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            focused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "buttonContent"
    )

    Row(
        modifier = modifier
            .height(JetStreamSizes.ButtonLarge)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Button)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = JetStreamSpacing.ButtonHorizontalPadding, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(JetStreamSizes.IconSmall),
                tint = contentColor
            )
            Spacer(Modifier.width(JetStreamSpacing.IconPadding))
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JetStream Primary Button
 * 主要操作按钮
 */
@Composable
fun JetStreamPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "primaryButtonScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
            focused -> colorScheme.primary
            else -> colorScheme.primaryContainer
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "primaryButtonBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            focused -> colorScheme.onPrimary
            else -> colorScheme.onPrimaryContainer
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "primaryButtonContent"
    )

    Row(
        modifier = modifier
            .height(JetStreamSizes.ButtonLarge)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Button)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = JetStreamSpacing.ButtonHorizontalPadding, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(JetStreamSizes.IconSmall),
                tint = contentColor
            )
            Spacer(Modifier.width(JetStreamSpacing.IconPadding))
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JetStream Icon Button
 * 纯图标按钮
 */
@Composable
fun JetStreamIconButton(
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
        label = "iconButtonScale"
    )

    val background by animateColorAsState(
        targetValue = when {
            selected -> colorScheme.secondaryContainer
            focused -> colorScheme.primaryContainer
            enabled -> colorScheme.surfaceVariant.copy(alpha = 0.82f)
            else -> colorScheme.surfaceVariant.copy(alpha = 0.42f)
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "iconButtonBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            selected -> colorScheme.onSecondaryContainer
            focused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "iconButtonContent"
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

/**
 * JetStream Text Button
 * 纯文本按钮
 */
@Composable
fun JetStreamTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.02f else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "textButtonScale"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            focused -> colorScheme.primary
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "textButtonColor"
    )

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = JetStreamSpacing.Medium, vertical = JetStreamSpacing.Small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JetStream Outlined Button
 * 带边框的按钮
 */
@Composable
fun JetStreamOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
        animationSpec = JetStreamAnimations.ScaleSpring,
        label = "outlinedButtonScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.outlineVariant.copy(alpha = 0.38f)
            focused -> colorScheme.primary
            else -> colorScheme.outlineVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "outlinedButtonBorder"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            focused -> colorScheme.primary
            else -> colorScheme.onSurfaceVariant
        },
        animationSpec = JetStreamAnimations.ColorTween,
        label = "outlinedButtonContent"
    )

    Row(
        modifier = modifier
            .height(JetStreamSizes.ButtonLarge)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .clip(JetStreamShapes.Button)
            .background(Color.Transparent)
            .border(JetStreamBorders.Medium, borderColor, JetStreamShapes.Button)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = JetStreamSpacing.ButtonHorizontalPadding, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(JetStreamSizes.IconSmall),
                tint = contentColor
            )
            Spacer(Modifier.width(JetStreamSpacing.IconPadding))
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
