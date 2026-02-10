package com.fongmi.android.tv.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvShapes

// Sentinel value indicating "use theme color at runtime"
private val UseThemeColor = Color.Unspecified

/**
 * A focusable container with TV-optimized focus effects.
 * Provides scale, border, and elevation animations on focus.
 *
 * @param onClick Called when the item is clicked/selected
 * @param modifier Modifier for the container
 * @param onLongClick Called when the item is long-pressed (optional)
 * @param enabled Whether the item is clickable
 * @param shape Shape of the item
 * @param focusRequester Optional focus requester for programmatic focus
 * @param focusScale Scale factor when focused (default 1.08)
 * @param focusBorderWidth Width of the focus border
 * @param focusBorderColor Color of the focus border
 * @param focusElevation Elevation when focused
 * @param content Content of the item
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusableItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = TvShapes.Small,
    focusRequester: FocusRequester? = null,
    focusScale: Float = TvDimens.FocusScaleFactor,
    focusBorderWidth: Dp = TvDimens.FocusBorderWidth,
    focusBorderColor: Color = UseThemeColor,
    focusElevation: Dp = TvDimens.FocusElevation,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val resolvedBorderColor = if (focusBorderColor == UseThemeColor) {
        MaterialTheme.colorScheme.primary
    } else {
        focusBorderColor
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "focus_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) focusElevation else if (focusElevation > 0.dp) 2.dp else 0.dp,
        animationSpec = spring(stiffness = 400f),
        label = "focus_elevation"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) focusBorderWidth else 0.dp,
        animationSpec = spring(stiffness = 400f),
        label = "focus_border"
    )

    // Apply shadow BEFORE scale to prevent misalignment
    // Use graphicsLayer for better shadow rendering with scale
    Box(
        modifier = modifier
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .focusable(interactionSource = interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false  // Don't clip shadow
            )
            .clip(shape)
            .border(
                width = borderWidth,
                color = if (isFocused) resolvedBorderColor else Color.Transparent,
                shape = shape
            )
    ) {
        content(isFocused)
    }
}

/**
 * Simple focusable button with TV focus effects.
 * For circular buttons, use CircleShape and provide explicit background.
 */
@Composable
fun FocusableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = UseThemeColor,
    focusedBackgroundColor: Color = UseThemeColor,
    shape: Shape = TvShapes.Button,
    focusRequester: FocusRequester? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val resolvedBg = if (backgroundColor == UseThemeColor) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        backgroundColor
    }
    val resolvedFocusedBg = if (focusedBackgroundColor == UseThemeColor) {
        MaterialTheme.colorScheme.primary
    } else {
        focusedBackgroundColor
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "button_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 2.dp,
        animationSpec = spring(stiffness = 400f),
        label = "button_elevation"
    )

    Box(
        modifier = modifier
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(if (isFocused) resolvedFocusedBg else resolvedBg)
    ) {
        content(isFocused)
    }
}

/**
 * Focusable chip component for category selection.
 */
@Composable
fun FocusableChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = TvShapes.Chip,
    focusRequester: FocusRequester? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "chip_scale"
    )

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isFocused -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (isFocused && !isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        content(isFocused)
    }
}
