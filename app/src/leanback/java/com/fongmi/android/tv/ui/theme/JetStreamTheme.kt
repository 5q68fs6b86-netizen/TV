package com.fongmi.android.tv.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.setting.Setting

/**
 * JetStream Material Design 3 Color Scheme
 * 统一的深色主题配色方案，遵循 Google JetStream 设计规范
 */
object JetStreamColors {
    // Surface Colors - 表面层级颜色
    val Surface = Color(0xFF1A1C1E)
    val SurfaceContainer = Color(0xFF1E2022)
    val SurfaceContainerHigh = Color(0xFF292B2D)
    val SurfaceContainerHighest = Color(0xFF33353A)
    val OnSurface = Color(0xFFE3E2E6)
    val OnSurfaceVariant = Color(0xFFC4C6CF)

    // Outline Colors - 边框颜色
    val Outline = Color(0xFF8E9099)
    val OutlineVariant = Color(0xFF44464F)

    // Primary Colors - 主色调
    val Primary = Color(0xFFAEBEF4)
    val OnPrimary = Color(0xFF0E2F7A)
    val PrimaryContainer = Color(0xFF2B4690)
    val OnPrimaryContainer = Color(0xFFD9E2FF)

    // Secondary Colors - 次要色调
    val Secondary = Color(0xFFBEC6DC)
    val OnSecondary = Color(0xFF283041)
    val SecondaryContainer = Color(0xFF3F4759)
    val OnSecondaryContainer = Color(0xFFDAE2F9)

    // Tertiary Colors - 第三色调
    val Tertiary = Color(0xFFDEBCDF)
    val OnTertiary = Color(0xFF402843)
    val TertiaryContainer = Color(0xFF583E5A)
    val OnTertiaryContainer = Color(0xFFFBD7FC)

    // Error Colors - 错误色调
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    // Background Colors - 背景颜色
    val Background = Color(0xFF11131A)
    val OnBackground = Color(0xFFE3E2E6)

}

object BilibiliPinkColors {
    val Pink1 = Color(0xFFFF6699)
    val Pink2 = Color(0xFFFFECF1)
    val Pink3 = Color(0xFFFF8CB0)
    val Pink4 = Color(0xFFE84B85)
    val Pink5 = Color(0xFFFFB3CA)
}

object JetStreamThemeController {
    private val refreshToken = mutableIntStateOf(0)

    val version: Int
        get() = refreshToken.intValue

    @JvmStatic
    fun refresh() {
        refreshToken.intValue += 1
    }
}

/**
 * JetStream Typography
 * 统一的文字排版样式
 */
val JetStreamTypography = Typography(
    // Display styles - 大标题
    displayLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - 标题
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - 副标题
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - 正文
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - 标签
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * JetStream Material Theme
 * 统一的 Material Design 3 主题
 */
private val JetStreamColorScheme = darkColorScheme(
    primary = JetStreamColors.Primary,
    onPrimary = JetStreamColors.OnPrimary,
    primaryContainer = JetStreamColors.PrimaryContainer,
    onPrimaryContainer = JetStreamColors.OnPrimaryContainer,

    secondary = JetStreamColors.Secondary,
    onSecondary = JetStreamColors.OnSecondary,
    secondaryContainer = JetStreamColors.SecondaryContainer,
    onSecondaryContainer = JetStreamColors.OnSecondaryContainer,

    tertiary = JetStreamColors.Tertiary,
    onTertiary = JetStreamColors.OnTertiary,
    tertiaryContainer = JetStreamColors.TertiaryContainer,
    onTertiaryContainer = JetStreamColors.OnTertiaryContainer,

    error = JetStreamColors.Error,
    onError = JetStreamColors.OnError,
    errorContainer = JetStreamColors.ErrorContainer,
    onErrorContainer = JetStreamColors.OnErrorContainer,

    background = JetStreamColors.Background,
    onBackground = JetStreamColors.OnBackground,

    surface = JetStreamColors.Surface,
    onSurface = JetStreamColors.OnSurface,
    onSurfaceVariant = JetStreamColors.OnSurfaceVariant,
    surfaceVariant = JetStreamColors.SurfaceContainer,

    outline = JetStreamColors.Outline,
    outlineVariant = JetStreamColors.OutlineVariant
)

private val BilibiliPinkColorScheme = darkColorScheme(
    primary = BilibiliPinkColors.Pink1,
    onPrimary = BilibiliPinkColors.Pink2,
    primaryContainer = BilibiliPinkColors.Pink2,
    onPrimaryContainer = BilibiliPinkColors.Pink4,

    secondary = BilibiliPinkColors.Pink3,
    onSecondary = BilibiliPinkColors.Pink2,
    secondaryContainer = BilibiliPinkColors.Pink4,
    onSecondaryContainer = BilibiliPinkColors.Pink2,

    tertiary = BilibiliPinkColors.Pink5,
    onTertiary = BilibiliPinkColors.Pink4,
    tertiaryContainer = BilibiliPinkColors.Pink5,
    onTertiaryContainer = BilibiliPinkColors.Pink4,

    error = JetStreamColors.Error,
    onError = JetStreamColors.OnError,
    errorContainer = JetStreamColors.ErrorContainer,
    onErrorContainer = JetStreamColors.OnErrorContainer,

    background = JetStreamColors.Background,
    onBackground = JetStreamColors.OnBackground,

    surface = JetStreamColors.Surface,
    onSurface = JetStreamColors.OnSurface,
    onSurfaceVariant = JetStreamColors.OnSurfaceVariant,
    surfaceVariant = JetStreamColors.SurfaceContainer,

    outline = JetStreamColors.Outline,
    outlineVariant = JetStreamColors.OutlineVariant
)

private fun getJetStreamColorScheme() = when (Setting.getThemeColor()) {
    Setting.THEME_BILIBILI_PINK -> BilibiliPinkColorScheme
    else -> JetStreamColorScheme
}

/**
 * JetStream Theme Composable
 * 应用主题的组合函数
 */
@Composable
fun JetStreamTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = remember(JetStreamThemeController.version) { getJetStreamColorScheme() }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JetStreamTypography,
        content = content
    )
}
