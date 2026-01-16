package com.fongmi.android.tv.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TV App Color System
 * Supports dual themes (Aurora/Sakura) with dark/light modes
 */

// ============================================================================
// AURORA THEME - Purple-Blue Gradient (Netflix/Disney+ Style)
// ============================================================================

object AuroraColorsDark {
    // Primary
    val Primary = Color(0xFF7C4DFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF4A148C)
    val OnPrimaryContainer = Color(0xFFE8DDFF)

    // Secondary
    val Secondary = Color(0xFFB388FF)
    val OnSecondary = Color(0xFF1A0033)
    val SecondaryContainer = Color(0xFF4A148C)
    val OnSecondaryContainer = Color(0xFFE8DDFF)

    // Tertiary / Accent
    val Tertiary = Color(0xFFFF4081)
    val OnTertiary = Color(0xFFFFFFFF)
    val Accent = Color(0xFFB388FF)

    // Background & Surface
    val Background = Color(0xFF0A0A0F)
    val OnBackground = Color(0xFFE8E8EC)
    val Surface = Color(0xFF121218)
    val OnSurface = Color(0xFFE8E8EC)
    val SurfaceVariant = Color(0xFF1E1E28)
    val OnSurfaceVariant = Color(0xFFC0C0C8)
    
    // Surface Containers
    val SurfaceContainer = Color(0xFF1A1A22)
    val SurfaceContainerLow = Color(0xFF141418)
    val SurfaceContainerHigh = Color(0xFF242430)
    val SurfaceContainerHighest = Color(0xFF2E2E3A)

    // Outline
    val Outline = Color(0xFF4A4A5E)
    val OutlineVariant = Color(0xFF2A2A3A)

    // Error
    val Error = Color(0xFFFF5252)
    val OnError = Color(0xFFFFFFFF)
    
    // Focus
    val FocusBorder = Color(0xFFB388FF)
    val FocusGlow = Color(0x4D7C4DFF)
}

object AuroraColorsLight {
    // Primary
    val Primary = Color(0xFF6200EE)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE8DDFF)
    val OnPrimaryContainer = Color(0xFF1A0033)

    // Secondary
    val Secondary = Color(0xFF7C4DFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE8DDFF)
    val OnSecondaryContainer = Color(0xFF1A0033)

    // Tertiary / Accent
    val Tertiary = Color(0xFFC51162)
    val OnTertiary = Color(0xFFFFFFFF)
    val Accent = Color(0xFF6200EE)

    // Background & Surface
    val Background = Color(0xFFFAFAFA)
    val OnBackground = Color(0xFF1A1A1A)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1A1A)
    val SurfaceVariant = Color(0xFFF0F0F5)
    val OnSurfaceVariant = Color(0xFF4A4A4A)
    
    // Surface Containers
    val SurfaceContainer = Color(0xFFF5F5F8)
    val SurfaceContainerLow = Color(0xFFFAFAFC)
    val SurfaceContainerHigh = Color(0xFFEEEEF2)
    val SurfaceContainerHighest = Color(0xFFE8E8EE)

    // Outline
    val Outline = Color(0xFFB0B0B8)
    val OutlineVariant = Color(0xFFD8D8E0)

    // Error
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
    
    // Focus
    val FocusBorder = Color(0xFF6200EE)
    val FocusGlow = Color(0x336200EE)
}

// ============================================================================
// SAKURA THEME - Bilibili Style (Pink + Blue)
// ============================================================================

object SakuraColorsDark {
    // Primary (Bilibili Pink)
    val Primary = Color(0xFFFB7299)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF6D2F42)
    val OnPrimaryContainer = Color(0xFFFFE0E8)

    // Secondary (Bilibili Blue)
    val Secondary = Color(0xFF00A1D6)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFF004D66)
    val OnSecondaryContainer = Color(0xFFCCEEFF)

    // Tertiary / Accent
    val Tertiary = Color(0xFF00A1D6)
    val OnTertiary = Color(0xFFFFFFFF)
    val Accent = Color(0xFF00A1D6)

    // Background & Surface
    val Background = Color(0xFF18181B)
    val OnBackground = Color(0xFFE8E8EC)
    val Surface = Color(0xFF222225)
    val OnSurface = Color(0xFFE8E8EC)
    val SurfaceVariant = Color(0xFF2C2C30)
    val OnSurfaceVariant = Color(0xFFC0C0C8)
    
    // Surface Containers
    val SurfaceContainer = Color(0xFF262628)
    val SurfaceContainerLow = Color(0xFF1E1E20)
    val SurfaceContainerHigh = Color(0xFF303033)
    val SurfaceContainerHighest = Color(0xFF3A3A3E)

    // Outline
    val Outline = Color(0xFF505055)
    val OutlineVariant = Color(0xFF35353A)

    // Error
    val Error = Color(0xFFFF5252)
    val OnError = Color(0xFFFFFFFF)
    
    // Focus
    val FocusBorder = Color(0xFFFB7299)
    val FocusGlow = Color(0x4DFB7299)
}

object SakuraColorsLight {
    // Primary (Bilibili Pink)
    val Primary = Color(0xFFFB7299)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFE0E8)
    val OnPrimaryContainer = Color(0xFF4A0F22)

    // Secondary (Bilibili Blue)
    val Secondary = Color(0xFF00A1D6)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFCCEEFF)
    val OnSecondaryContainer = Color(0xFF003348)

    // Tertiary / Accent
    val Tertiary = Color(0xFF0087B3)
    val OnTertiary = Color(0xFFFFFFFF)
    val Accent = Color(0xFF00A1D6)

    // Background & Surface
    val Background = Color(0xFFF4F4F4)
    val OnBackground = Color(0xFF1A1A1A)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1A1A)
    val SurfaceVariant = Color(0xFFF0F0F2)
    val OnSurfaceVariant = Color(0xFF4A4A4A)
    
    // Surface Containers
    val SurfaceContainer = Color(0xFFF8F8F8)
    val SurfaceContainerLow = Color(0xFFFCFCFC)
    val SurfaceContainerHigh = Color(0xFFF0F0F0)
    val SurfaceContainerHighest = Color(0xFFE8E8E8)

    // Outline
    val Outline = Color(0xFFB0B0B5)
    val OutlineVariant = Color(0xFFD8D8DD)

    // Error
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
    
    // Focus
    val FocusBorder = Color(0xFFFB7299)
    val FocusGlow = Color(0x33FB7299)
}

// ============================================================================
// COMMON COLORS (Theme-independent)
// ============================================================================

object CommonColors {
    // Basic
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00000000)

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

    // Status
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)

    // Scrim
    val Scrim = Color(0x80000000)
    val ScrimLight = Color(0x4D000000)
    val ScrimHeavy = Color(0xB3000000)
}

// ============================================================================
// LEGACY TvColors (For backward compatibility, uses Aurora Dark by default)
// ============================================================================

object TvColors {
    val Primary = AuroraColorsDark.Primary
    val OnPrimary = AuroraColorsDark.OnPrimary
    val PrimaryContainer = AuroraColorsDark.PrimaryContainer
    val OnPrimaryContainer = AuroraColorsDark.OnPrimaryContainer
    val Secondary = AuroraColorsDark.Secondary
    val OnSecondary = AuroraColorsDark.OnSecondary
    val SecondaryContainer = AuroraColorsDark.SecondaryContainer
    val OnSecondaryContainer = AuroraColorsDark.OnSecondaryContainer
    val Tertiary = AuroraColorsDark.Tertiary
    val OnTertiary = AuroraColorsDark.OnTertiary
    val Error = AuroraColorsDark.Error
    val OnError = AuroraColorsDark.OnError
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)
    val Background = AuroraColorsDark.Background
    val OnBackground = AuroraColorsDark.OnBackground
    val Surface = AuroraColorsDark.Surface
    val OnSurface = AuroraColorsDark.OnSurface
    val SurfaceVariant = AuroraColorsDark.SurfaceVariant
    val OnSurfaceVariant = AuroraColorsDark.OnSurfaceVariant
    val SurfaceContainer = AuroraColorsDark.SurfaceContainer
    val SurfaceContainerLow = AuroraColorsDark.SurfaceContainerLow
    val SurfaceContainerHigh = AuroraColorsDark.SurfaceContainerHigh
    val SurfaceContainerHighest = AuroraColorsDark.SurfaceContainerHighest
    val Outline = AuroraColorsDark.Outline
    val OutlineVariant = AuroraColorsDark.OutlineVariant
    val InverseSurface = Color(0xFFE0E0E0)
    val InverseOnSurface = Color(0xFF1A1A2E)
    val InversePrimary = Color(0xFFB388FF)
    
    // White/Black with alpha (for backward compatibility)
    val White = CommonColors.White
    val Black = CommonColors.Black
    val White10 = CommonColors.White10
    val White20 = CommonColors.White20
    val White30 = CommonColors.White30
    val White40 = CommonColors.White40
    val White50 = CommonColors.White50
    val White60 = CommonColors.White60
    val White70 = CommonColors.White70
    val White80 = CommonColors.White80
    val White90 = CommonColors.White90
    val Black10 = CommonColors.Black10
    val Black20 = CommonColors.Black20
    val Black30 = CommonColors.Black30
    val Black40 = CommonColors.Black40
    val Black50 = CommonColors.Black50
    val Black60 = CommonColors.Black60
    val Black70 = CommonColors.Black70
    val Black80 = CommonColors.Black80
    val Black90 = CommonColors.Black90
    
    val FocusBorder = AuroraColorsDark.FocusBorder
    val FocusGlow = AuroraColorsDark.FocusGlow
    val FocusRing = AuroraColorsDark.FocusBorder
    val CardBackground = SurfaceContainer
    val CardBackgroundElevated = SurfaceContainerHigh
    val CardStroke = OutlineVariant
    val CardStrokeFocused = Primary
    val VideoProgressPlayed = Primary
    val VideoProgressBuffered = Color(0x4DB388FF)
    val VideoProgressBackground = CommonColors.White30
    val VideoControlTint = CommonColors.White
    val StatusSuccess = CommonColors.Success
    val StatusWarning = CommonColors.Warning
    val StatusError = Error
    val StatusInfo = CommonColors.Info
    val TextPrimary = OnSurface
    val TextSecondary = OnSurfaceVariant
    val TextDisabled = CommonColors.White40
    val TextHint = CommonColors.White50
    val Scrim = CommonColors.Scrim
    val ScrimLight = CommonColors.ScrimLight
    val ScrimHeavy = CommonColors.ScrimHeavy
}

