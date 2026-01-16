package com.fongmi.android.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme as TvMaterialTheme

/**
 * Create Aurora Dark color scheme
 */
private fun auroraColorsDark() = darkColorScheme(
    primary = AuroraColorsDark.Primary,
    onPrimary = AuroraColorsDark.OnPrimary,
    primaryContainer = AuroraColorsDark.PrimaryContainer,
    onPrimaryContainer = AuroraColorsDark.OnPrimaryContainer,
    secondary = AuroraColorsDark.Secondary,
    onSecondary = AuroraColorsDark.OnSecondary,
    secondaryContainer = AuroraColorsDark.SecondaryContainer,
    onSecondaryContainer = AuroraColorsDark.OnSecondaryContainer,
    tertiary = AuroraColorsDark.Tertiary,
    onTertiary = AuroraColorsDark.OnTertiary,
    error = AuroraColorsDark.Error,
    onError = AuroraColorsDark.OnError,
    background = AuroraColorsDark.Background,
    onBackground = AuroraColorsDark.OnBackground,
    surface = AuroraColorsDark.Surface,
    onSurface = AuroraColorsDark.OnSurface,
    surfaceVariant = AuroraColorsDark.SurfaceVariant,
    onSurfaceVariant = AuroraColorsDark.OnSurfaceVariant,
    outline = AuroraColorsDark.Outline,
    outlineVariant = AuroraColorsDark.OutlineVariant,
    surfaceContainerLowest = AuroraColorsDark.SurfaceContainerLow,
    surfaceContainerLow = AuroraColorsDark.SurfaceContainerLow,
    surfaceContainer = AuroraColorsDark.SurfaceContainer,
    surfaceContainerHigh = AuroraColorsDark.SurfaceContainerHigh,
    surfaceContainerHighest = AuroraColorsDark.SurfaceContainerHighest
)

/**
 * Create Aurora Light color scheme
 */
private fun auroraColorsLight() = lightColorScheme(
    primary = AuroraColorsLight.Primary,
    onPrimary = AuroraColorsLight.OnPrimary,
    primaryContainer = AuroraColorsLight.PrimaryContainer,
    onPrimaryContainer = AuroraColorsLight.OnPrimaryContainer,
    secondary = AuroraColorsLight.Secondary,
    onSecondary = AuroraColorsLight.OnSecondary,
    secondaryContainer = AuroraColorsLight.SecondaryContainer,
    onSecondaryContainer = AuroraColorsLight.OnSecondaryContainer,
    tertiary = AuroraColorsLight.Tertiary,
    onTertiary = AuroraColorsLight.OnTertiary,
    error = AuroraColorsLight.Error,
    onError = AuroraColorsLight.OnError,
    background = AuroraColorsLight.Background,
    onBackground = AuroraColorsLight.OnBackground,
    surface = AuroraColorsLight.Surface,
    onSurface = AuroraColorsLight.OnSurface,
    surfaceVariant = AuroraColorsLight.SurfaceVariant,
    onSurfaceVariant = AuroraColorsLight.OnSurfaceVariant,
    outline = AuroraColorsLight.Outline,
    outlineVariant = AuroraColorsLight.OutlineVariant,
    surfaceContainerLowest = AuroraColorsLight.SurfaceContainerLow,
    surfaceContainerLow = AuroraColorsLight.SurfaceContainerLow,
    surfaceContainer = AuroraColorsLight.SurfaceContainer,
    surfaceContainerHigh = AuroraColorsLight.SurfaceContainerHigh,
    surfaceContainerHighest = AuroraColorsLight.SurfaceContainerHighest
)

/**
 * Create Sakura Dark color scheme
 */
private fun sakuraColorsDark() = darkColorScheme(
    primary = SakuraColorsDark.Primary,
    onPrimary = SakuraColorsDark.OnPrimary,
    primaryContainer = SakuraColorsDark.PrimaryContainer,
    onPrimaryContainer = SakuraColorsDark.OnPrimaryContainer,
    secondary = SakuraColorsDark.Secondary,
    onSecondary = SakuraColorsDark.OnSecondary,
    secondaryContainer = SakuraColorsDark.SecondaryContainer,
    onSecondaryContainer = SakuraColorsDark.OnSecondaryContainer,
    tertiary = SakuraColorsDark.Tertiary,
    onTertiary = SakuraColorsDark.OnTertiary,
    error = SakuraColorsDark.Error,
    onError = SakuraColorsDark.OnError,
    background = SakuraColorsDark.Background,
    onBackground = SakuraColorsDark.OnBackground,
    surface = SakuraColorsDark.Surface,
    onSurface = SakuraColorsDark.OnSurface,
    surfaceVariant = SakuraColorsDark.SurfaceVariant,
    onSurfaceVariant = SakuraColorsDark.OnSurfaceVariant,
    outline = SakuraColorsDark.Outline,
    outlineVariant = SakuraColorsDark.OutlineVariant,
    surfaceContainerLowest = SakuraColorsDark.SurfaceContainerLow,
    surfaceContainerLow = SakuraColorsDark.SurfaceContainerLow,
    surfaceContainer = SakuraColorsDark.SurfaceContainer,
    surfaceContainerHigh = SakuraColorsDark.SurfaceContainerHigh,
    surfaceContainerHighest = SakuraColorsDark.SurfaceContainerHighest
)

/**
 * Create Sakura Light color scheme
 */
private fun sakuraColorsLight() = lightColorScheme(
    primary = SakuraColorsLight.Primary,
    onPrimary = SakuraColorsLight.OnPrimary,
    primaryContainer = SakuraColorsLight.PrimaryContainer,
    onPrimaryContainer = SakuraColorsLight.OnPrimaryContainer,
    secondary = SakuraColorsLight.Secondary,
    onSecondary = SakuraColorsLight.OnSecondary,
    secondaryContainer = SakuraColorsLight.SecondaryContainer,
    onSecondaryContainer = SakuraColorsLight.OnSecondaryContainer,
    tertiary = SakuraColorsLight.Tertiary,
    onTertiary = SakuraColorsLight.OnTertiary,
    error = SakuraColorsLight.Error,
    onError = SakuraColorsLight.OnError,
    background = SakuraColorsLight.Background,
    onBackground = SakuraColorsLight.OnBackground,
    surface = SakuraColorsLight.Surface,
    onSurface = SakuraColorsLight.OnSurface,
    surfaceVariant = SakuraColorsLight.SurfaceVariant,
    onSurfaceVariant = SakuraColorsLight.OnSurfaceVariant,
    outline = SakuraColorsLight.Outline,
    outlineVariant = SakuraColorsLight.OutlineVariant,
    surfaceContainerLowest = SakuraColorsLight.SurfaceContainerLow,
    surfaceContainerLow = SakuraColorsLight.SurfaceContainerLow,
    surfaceContainer = SakuraColorsLight.SurfaceContainer,
    surfaceContainerHigh = SakuraColorsLight.SurfaceContainerHigh,
    surfaceContainerHighest = SakuraColorsLight.SurfaceContainerHighest
)

/**
 * Get the appropriate color scheme based on theme config
 */
@Composable
private fun getColorScheme(config: ThemeConfig): androidx.compose.material3.ColorScheme {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (config.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }
    
    return when (config.theme) {
        AppTheme.AURORA -> if (isDark) auroraColorsDark() else auroraColorsLight()
        AppTheme.SAKURA -> if (isDark) sakuraColorsDark() else sakuraColorsLight()
    }
}

/**
 * Get focus colors based on current theme
 */
data class FocusColors(
    val border: Color,
    val glow: Color
)

@Composable
fun getFocusColors(config: ThemeConfig = ThemeState.config): FocusColors {
    val isDark = when (config.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    return when (config.theme) {
        AppTheme.AURORA -> FocusColors(
            border = if (isDark) AuroraColorsDark.FocusBorder else AuroraColorsLight.FocusBorder,
            glow = if (isDark) AuroraColorsDark.FocusGlow else AuroraColorsLight.FocusGlow
        )
        AppTheme.SAKURA -> FocusColors(
            border = if (isDark) SakuraColorsDark.FocusBorder else SakuraColorsLight.FocusBorder,
            glow = if (isDark) SakuraColorsDark.FocusGlow else SakuraColorsLight.FocusGlow
        )
    }
}

/**
 * Local composition for TV-specific design tokens
 */
val LocalTvTypography = staticCompositionLocalOf { TvTypography }
val LocalTvShapes = staticCompositionLocalOf { TvShapes }
val LocalTvDimens = staticCompositionLocalOf { TvDimens }
val LocalTvColors = staticCompositionLocalOf { TvColors }

/**
 * TV App Theme with dual theme and dark/light mode support
 *
 * @param config Theme configuration (theme type + mode)
 * @param content Composable content
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvAppTheme(
    config: ThemeConfig = ThemeState.config,
    content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme(config)
    val focusColors = getFocusColors(config)
    
    val isDark = when (config.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    CompositionLocalProvider(
        LocalTvTypography provides TvTypography,
        LocalTvShapes provides TvShapes,
        LocalTvDimens provides TvDimens,
        LocalTvColors provides TvColors,
        LocalThemeConfig provides config
    ) {
        TvMaterialTheme(
            colorScheme = if (isDark) {
                androidx.tv.material3.darkColorScheme(
                    primary = colorScheme.primary,
                    onPrimary = colorScheme.onPrimary,
                    primaryContainer = colorScheme.primaryContainer,
                    onPrimaryContainer = colorScheme.onPrimaryContainer,
                    secondary = colorScheme.secondary,
                    onSecondary = colorScheme.onSecondary,
                    secondaryContainer = colorScheme.secondaryContainer,
                    onSecondaryContainer = colorScheme.onSecondaryContainer,
                    tertiary = colorScheme.tertiary,
                    onTertiary = colorScheme.onTertiary,
                    tertiaryContainer = colorScheme.tertiaryContainer,
                    onTertiaryContainer = colorScheme.onTertiaryContainer,
                    error = colorScheme.error,
                    onError = colorScheme.onError,
                    errorContainer = colorScheme.errorContainer,
                    onErrorContainer = colorScheme.onErrorContainer,
                    background = colorScheme.background,
                    onBackground = colorScheme.onBackground,
                    surface = colorScheme.surface,
                    onSurface = colorScheme.onSurface,
                    surfaceVariant = colorScheme.surfaceVariant,
                    onSurfaceVariant = colorScheme.onSurfaceVariant,
                    border = colorScheme.outline,
                    borderVariant = colorScheme.outlineVariant,
                    scrim = CommonColors.Scrim
                )
            } else {
                androidx.tv.material3.lightColorScheme(
                    primary = colorScheme.primary,
                    onPrimary = colorScheme.onPrimary,
                    primaryContainer = colorScheme.primaryContainer,
                    onPrimaryContainer = colorScheme.onPrimaryContainer,
                    secondary = colorScheme.secondary,
                    onSecondary = colorScheme.onSecondary,
                    secondaryContainer = colorScheme.secondaryContainer,
                    onSecondaryContainer = colorScheme.onSecondaryContainer,
                    tertiary = colorScheme.tertiary,
                    onTertiary = colorScheme.onTertiary,
                    tertiaryContainer = colorScheme.tertiaryContainer,
                    onTertiaryContainer = colorScheme.onTertiaryContainer,
                    error = colorScheme.error,
                    onError = colorScheme.onError,
                    errorContainer = colorScheme.errorContainer,
                    onErrorContainer = colorScheme.onErrorContainer,
                    background = colorScheme.background,
                    onBackground = colorScheme.onBackground,
                    surface = colorScheme.surface,
                    onSurface = colorScheme.onSurface,
                    surfaceVariant = colorScheme.surfaceVariant,
                    onSurfaceVariant = colorScheme.onSurfaceVariant,
                    border = colorScheme.outline,
                    borderVariant = colorScheme.outlineVariant,
                    scrim = CommonColors.ScrimLight
                )
            }
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                content = content
            )
        }
    }
}

/**
 * Extension object to access TV theme values
 */
object TvTheme {
    val typography: TvTypography
        @Composable
        get() = LocalTvTypography.current

    val shapes: TvShapes
        @Composable
        get() = LocalTvShapes.current

    val dimens: TvDimens
        @Composable
        get() = LocalTvDimens.current

    val colors: TvColors
        @Composable
        get() = LocalTvColors.current
    
    val config: ThemeConfig
        @Composable
        get() = LocalThemeConfig.current
    
    val focusColors: FocusColors
        @Composable
        get() = getFocusColors()
}

