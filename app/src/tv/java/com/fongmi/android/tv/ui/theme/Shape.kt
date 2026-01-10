package com.fongmi.android.tv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * TV App Shape System
 * Defines consistent corner radii for components
 */
object TvShapes {
    // ========== Standard Shapes ==========
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Full = RoundedCornerShape(50)

    // ========== Component-Specific Shapes ==========

    // Video card shape
    val Card = RoundedCornerShape(12.dp)

    // Category chip shape
    val Chip = RoundedCornerShape(8.dp)

    // Button shapes
    val Button = RoundedCornerShape(8.dp)
    val ButtonSmall = RoundedCornerShape(6.dp)

    // Dialog shape
    val Dialog = RoundedCornerShape(16.dp)

    // Bottom sheet shape
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    // Episode item shape
    val EpisodeItem = RoundedCornerShape(6.dp)

    // Search bar shape
    val SearchBar = RoundedCornerShape(12.dp)

    // Keyboard key shape
    val KeyboardKey = RoundedCornerShape(8.dp)

    // Progress bar shape
    val ProgressBar = RoundedCornerShape(4.dp)

    // Poster image shape (taller cards)
    val Poster = RoundedCornerShape(10.dp)

    // Banner/hero shape
    val Banner = RoundedCornerShape(16.dp)

    // Navigation item shape
    val NavItem = RoundedCornerShape(8.dp)

    // Badge shape
    val Badge = RoundedCornerShape(4.dp)

    // Player control button shape
    val PlayerButton = RoundedCornerShape(50)
}

/**
 * TV App Dimension constants
 * Consistent spacing and sizing values
 */
object TvDimens {
    // ========== Spacing ==========
    val SpacingXxs = 2.dp
    val SpacingXs = 4.dp
    val SpacingSm = 8.dp
    val SpacingMd = 12.dp
    val SpacingLg = 16.dp
    val SpacingXl = 24.dp
    val SpacingXxl = 32.dp
    val SpacingXxxl = 48.dp

    // ========== Content Padding ==========
    val ScreenPaddingHorizontal = 48.dp
    val ScreenPaddingVertical = 32.dp
    val CardPadding = 12.dp
    val ListItemPadding = 16.dp

    // ========== Card Sizes ==========
    val CardWidthSmall = 140.dp
    val CardWidthMedium = 180.dp
    val CardWidthLarge = 220.dp

    val CardHeightSmall = 196.dp  // 3:4 ratio
    val CardHeightMedium = 252.dp // 3:4 ratio
    val CardHeightLarge = 308.dp  // 3:4 ratio

    // Wide card (16:9 ratio)
    val WideCardWidth = 280.dp
    val WideCardHeight = 158.dp

    // ========== Row Heights ==========
    val CategoryRowHeight = 48.dp
    val ContentRowHeight = 280.dp
    val EpisodeRowHeight = 56.dp

    // ========== Icon Sizes ==========
    val IconSizeSmall = 20.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp
    val IconSizeXl = 48.dp

    // ========== Button Sizes ==========
    val ButtonHeightSmall = 36.dp
    val ButtonHeightMedium = 44.dp
    val ButtonHeightLarge = 52.dp

    // ========== Focus Effect ==========
    val FocusScaleFactor = 1.08f
    val FocusBorderWidth = 3.dp
    val FocusElevation = 8.dp

    // ========== Animation Durations ==========
    val AnimDurationFast = 150
    val AnimDurationMedium = 300
    val AnimDurationSlow = 500
}
