package com.fongmi.android.tv.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TV App Color System
 * Based on Material Design 3 with TV-optimized adjustments
 */
object TvColors {
    // ========== Primary Colors ==========
    val Primary = Color(0xFF00696A)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF9CF1F1)
    val OnPrimaryContainer = Color(0xFF004F50)

    // ========== Secondary Colors ==========
    val Secondary = Color(0xFF4A6363)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFCCE8E7)
    val OnSecondaryContainer = Color(0xFF324B4B)

    // ========== Tertiary Colors ==========
    val Tertiary = Color(0xFF4C607C)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFD3E3FF)
    val OnTertiaryContainer = Color(0xFF344863)

    // ========== Error Colors ==========
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    // ========== Surface Colors (Dark Theme for TV) ==========
    val Background = Color(0xFF0F0F1A)
    val OnBackground = Color(0xFFE0E0E0)
    val Surface = Color(0xFF1A1A2E)
    val OnSurface = Color(0xFFE0E0E0)
    val SurfaceVariant = Color(0xFF2A2A3E)
    val OnSurfaceVariant = Color(0xFFB0B0B0)

    // Surface containers
    val SurfaceContainer = Color(0xFF252538)
    val SurfaceContainerLow = Color(0xFF1F1F32)
    val SurfaceContainerHigh = Color(0xFF2F2F45)
    val SurfaceContainerHighest = Color(0xFF3A3A50)

    // ========== Outline Colors ==========
    val Outline = Color(0xFF6F7979)
    val OutlineVariant = Color(0xFF3F4F4F)

    // ========== Inverse Colors ==========
    val InverseSurface = Color(0xFFE0E0E0)
    val InverseOnSurface = Color(0xFF1A1A2E)
    val InversePrimary = Color(0xFF80D4D5)

    // ========== Basic Colors ==========
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // White with alpha
    val White10 = Color(0x1AFFFFFF)
    val White20 = Color(0x33FFFFFF)
    val White30 = Color(0x4DFFFFFF)
    val White40 = Color(0x66FFFFFF)
    val White50 = Color(0x80FFFFFF)
    val White60 = Color(0x99FFFFFF)
    val White70 = Color(0xB3FFFFFF)
    val White80 = Color(0xCCFFFFFF)
    val White90 = Color(0xE6FFFFFF)

    // Black with alpha
    val Black10 = Color(0x1A000000)
    val Black20 = Color(0x33000000)
    val Black30 = Color(0x4D000000)
    val Black40 = Color(0x66000000)
    val Black50 = Color(0x80000000)
    val Black60 = Color(0x99000000)
    val Black70 = Color(0xB3000000)
    val Black80 = Color(0xCC000000)
    val Black90 = Color(0xE6000000)

    // ========== Accent Colors ==========
    // Blue
    val Blue50 = Color(0xFFE3F2FD)
    val Blue200 = Color(0xFF90CAF9)
    val Blue500 = Color(0xFF2196F3)
    val Blue700 = Color(0xFF1976D2)

    // Green
    val Green50 = Color(0xFFE8F5E8)
    val Green500 = Color(0xFF4CAF50)
    val Green700 = Color(0xFF388E3C)

    // Orange
    val Orange50 = Color(0xFFFFF3E0)
    val Orange500 = Color(0xFFFF9800)
    val Orange700 = Color(0xFFF57C00)

    // Red
    val Red50 = Color(0xFFFFEBEE)
    val Red500 = Color(0xFFF44336)
    val Red700 = Color(0xFFD32F2F)

    // Grey
    val Grey100 = Color(0xFFF5F5F5)
    val Grey300 = Color(0xFFE0E0E0)
    val Grey500 = Color(0xFF9E9E9E)
    val Grey700 = Color(0xFF616161)
    val Grey900 = Color(0xFF212121)

    // ========== Focus Colors ==========
    val FocusBorder = Color(0xFF80D4D5)
    val FocusGlow = Color(0x4D80D4D5)
    val FocusRing = Color(0xFF80D4D5)

    // ========== Card Colors ==========
    val CardBackground = SurfaceContainer
    val CardBackgroundElevated = SurfaceContainerHigh
    val CardStroke = OutlineVariant
    val CardStrokeFocused = Primary

    // ========== Video Player Colors ==========
    val VideoProgressPlayed = Primary
    val VideoProgressBuffered = Color(0x4D80D4D5)
    val VideoProgressBackground = Color(0x33FFFFFF)
    val VideoControlTint = White

    // ========== Status Colors ==========
    val StatusSuccess = Green500
    val StatusWarning = Orange500
    val StatusError = Error
    val StatusInfo = Blue500

    // ========== Text Colors ==========
    val TextPrimary = OnSurface
    val TextSecondary = OnSurfaceVariant
    val TextDisabled = Color(0x61FFFFFF)
    val TextHint = Color(0x80FFFFFF)

    // ========== Overlay Colors ==========
    val Scrim = Color(0x80000000)
    val ScrimLight = Color(0x4D000000)
    val ScrimHeavy = Color(0xB3000000)
}
