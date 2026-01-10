package com.fongmi.android.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme as TvMaterialTheme

/**
 * TV App Dark Color Scheme
 * Optimized for TV viewing experience
 */
private val DarkColorScheme = darkColorScheme(
    primary = TvColors.Primary,
    onPrimary = TvColors.OnPrimary,
    primaryContainer = TvColors.PrimaryContainer,
    onPrimaryContainer = TvColors.OnPrimaryContainer,
    secondary = TvColors.Secondary,
    onSecondary = TvColors.OnSecondary,
    secondaryContainer = TvColors.SecondaryContainer,
    onSecondaryContainer = TvColors.OnSecondaryContainer,
    tertiary = TvColors.Tertiary,
    onTertiary = TvColors.OnTertiary,
    tertiaryContainer = TvColors.TertiaryContainer,
    onTertiaryContainer = TvColors.OnTertiaryContainer,
    error = TvColors.Error,
    onError = TvColors.OnError,
    errorContainer = TvColors.ErrorContainer,
    onErrorContainer = TvColors.OnErrorContainer,
    background = TvColors.Background,
    onBackground = TvColors.OnBackground,
    surface = TvColors.Surface,
    onSurface = TvColors.OnSurface,
    surfaceVariant = TvColors.SurfaceVariant,
    onSurfaceVariant = TvColors.OnSurfaceVariant,
    outline = TvColors.Outline,
    outlineVariant = TvColors.OutlineVariant,
    inverseSurface = TvColors.InverseSurface,
    inverseOnSurface = TvColors.InverseOnSurface,
    inversePrimary = TvColors.InversePrimary,
    surfaceContainerLowest = TvColors.SurfaceContainerLow,
    surfaceContainerLow = TvColors.SurfaceContainerLow,
    surfaceContainer = TvColors.SurfaceContainer,
    surfaceContainerHigh = TvColors.SurfaceContainerHigh,
    surfaceContainerHighest = TvColors.SurfaceContainerHighest
)

/**
 * TV App Light Color Scheme (for future use)
 */
private val LightColorScheme = lightColorScheme(
    primary = TvColors.Primary,
    onPrimary = TvColors.OnPrimary,
    primaryContainer = TvColors.PrimaryContainer,
    onPrimaryContainer = TvColors.OnPrimaryContainer,
    secondary = TvColors.Secondary,
    onSecondary = TvColors.OnSecondary,
    secondaryContainer = TvColors.SecondaryContainer,
    onSecondaryContainer = TvColors.OnSecondaryContainer,
    tertiary = TvColors.Tertiary,
    onTertiary = TvColors.OnTertiary,
    tertiaryContainer = TvColors.TertiaryContainer,
    onTertiaryContainer = TvColors.OnTertiaryContainer,
    error = TvColors.Error,
    onError = TvColors.OnError,
    errorContainer = TvColors.ErrorContainer,
    onErrorContainer = TvColors.OnErrorContainer
)

/**
 * Local composition for TV-specific design tokens
 */
val LocalTvTypography = staticCompositionLocalOf { TvTypography }
val LocalTvShapes = staticCompositionLocalOf { TvShapes }
val LocalTvDimens = staticCompositionLocalOf { TvDimens }
val LocalTvColors = staticCompositionLocalOf { TvColors }

/**
 * TV App Theme
 * Always uses dark theme for optimal TV viewing experience
 *
 * @param darkTheme Always true for TV (10-foot viewing)
 * @param content Composable content
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvAppTheme(
    darkTheme: Boolean = true, // TV always uses dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalTvTypography provides TvTypography,
        LocalTvShapes provides TvShapes,
        LocalTvDimens provides TvDimens,
        LocalTvColors provides TvColors
    ) {
        TvMaterialTheme(
            colorScheme = androidx.tv.material3.darkColorScheme(
                primary = TvColors.Primary,
                onPrimary = TvColors.OnPrimary,
                primaryContainer = TvColors.PrimaryContainer,
                onPrimaryContainer = TvColors.OnPrimaryContainer,
                secondary = TvColors.Secondary,
                onSecondary = TvColors.OnSecondary,
                secondaryContainer = TvColors.SecondaryContainer,
                onSecondaryContainer = TvColors.OnSecondaryContainer,
                tertiary = TvColors.Tertiary,
                onTertiary = TvColors.OnTertiary,
                tertiaryContainer = TvColors.TertiaryContainer,
                onTertiaryContainer = TvColors.OnTertiaryContainer,
                error = TvColors.Error,
                onError = TvColors.OnError,
                errorContainer = TvColors.ErrorContainer,
                onErrorContainer = TvColors.OnErrorContainer,
                background = TvColors.Background,
                onBackground = TvColors.OnBackground,
                surface = TvColors.Surface,
                onSurface = TvColors.OnSurface,
                surfaceVariant = TvColors.SurfaceVariant,
                onSurfaceVariant = TvColors.OnSurfaceVariant,
                border = TvColors.Outline,
                borderVariant = TvColors.OutlineVariant,
                scrim = TvColors.Scrim
            )
        ) {
            // Also provide Material3 theme for components that use it
            MaterialTheme(
                colorScheme = colorScheme,
                content = content
            )
        }
    }
}

/**
 * Extension property to access TV typography from composition
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
}
