package com.fongmi.android.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.R

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
 * JetStream 品牌字体：MiSans（子集化，常用字覆盖，罕见字回退系统字体）
 */
val JetStreamFontFamily = FontFamily(
    Font(R.font.misans_regular, FontWeight.Normal),
    Font(R.font.misans_medium, FontWeight.Medium),
    Font(R.font.misans_semibold, FontWeight.SemiBold)
)

/**
 * JetStream Typography
 * 统一的文字排版样式
 */
val JetStreamTypography = Typography(
    // Display styles - 大标题
    displayLarge = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - 标题
    headlineLarge = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - 副标题
    titleLarge = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - 正文
    bodyLarge = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - 标签
    labelLarge = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetStreamFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * JetStream Theme Composable
 * 应用主题的组合函数
 */
@Composable
fun JetStreamTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = remember(JetStreamThemeController.version) { JetStreamPalette.colorScheme() }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JetStreamTypography,
        content = content
    )
}
