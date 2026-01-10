package com.fongmi.android.tv.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvShapes

/**
 * A focusable container with TV-optimized focus effects.
 * Provides scale, border, and elevation animations on focus.
 *
 * @param onClick Called when the item is clicked/selected
 * @param modifier Modifier for the container
 * @param enabled Whether the item is clickable
 * @param shape Shape of the item
 * @param focusRequester Optional focus requester for programmatic focus
 * @param focusScale Scale factor when focused (default 1.08)
 * @param focusBorderWidth Width of the focus border
 * @param focusBorderColor Color of the focus border
 * @param focusElevation Elevation when focused
 * @param content Content of the item
 */
@Composable
fun FocusableItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = TvShapes.Card,
    focusRequester: FocusRequester? = null,
    focusScale: Float = TvDimens.FocusScaleFactor,
    focusBorderWidth: Dp = TvDimens.FocusBorderWidth,
    focusBorderColor: Color = TvColors.FocusBorder,
    focusElevation: Dp = TvDimens.FocusElevation,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
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
        targetValue = if (isFocused) focusElevation else 2.dp,
        animationSpec = spring(stiffness = 400f),
        label = "focus_elevation"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) focusBorderWidth else 0.dp,
        animationSpec = spring(stiffness = 400f),
        label = "focus_border"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(elevation = elevation, shape = shape)
            .clip(shape)
            .border(
                width = borderWidth,
                color = if (isFocused) focusBorderColor else Color.Transparent,
                shape = shape
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

/**
 * Simple focusable button with TV focus effects.
 */
@Composable
fun FocusableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = TvColors.SurfaceContainer,
    focusedBackgroundColor: Color = TvColors.Primary,
    shape: Shape = TvShapes.Button,
    focusRequester: FocusRequester? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(if (isFocused) focusedBackgroundColor else backgroundColor)
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
        isSelected -> TvColors.Primary
        isFocused -> TvColors.SurfaceContainerHigh
        else -> TvColors.SurfaceContainer
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
                        color = TvColors.FocusBorder,
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
