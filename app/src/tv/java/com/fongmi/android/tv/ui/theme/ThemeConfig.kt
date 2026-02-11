package com.fongmi.android.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.github.catvod.utils.Prefers

/**
 * App Theme Type
 */
enum class AppTheme(val displayName: String) {
    AURORA("Aurora"),      // Purple-blue gradient
    SAKURA("Sakura")       // Bilibili-style pink
}

/**
 * Theme Mode
 */
enum class ThemeMode(val displayName: String) {
    DARK("深色"),
    LIGHT("浅色"),
    SYSTEM("跟随系统")
}

/**
 * Theme Configuration
 */
data class ThemeConfig(
    val theme: AppTheme = AppTheme.SAKURA,
    val mode: ThemeMode = ThemeMode.DARK
) {
    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_MODE = "theme_mode"

        /**
         * Load theme config from preferences
         */
        fun load(): ThemeConfig {
            val themeOrdinal = Prefers.getInt(KEY_THEME, AppTheme.SAKURA.ordinal)
            val modeOrdinal = Prefers.getInt(KEY_MODE, ThemeMode.DARK.ordinal)
            return ThemeConfig(
                theme = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.SAKURA },
                mode = ThemeMode.entries.getOrElse(modeOrdinal) { ThemeMode.DARK }
            )
        }

        /**
         * Save theme config to preferences
         */
        fun save(config: ThemeConfig) {
            Prefers.put(KEY_THEME, config.theme.ordinal)
            Prefers.put(KEY_MODE, config.mode.ordinal)
        }
    }
}

/**
 * Global theme state holder
 */
object ThemeState {
    var config by mutableStateOf(ThemeConfig())
        private set

    fun update(newConfig: ThemeConfig) {
        config = newConfig
        ThemeConfig.save(newConfig)
    }

    fun setTheme(theme: AppTheme) {
        update(config.copy(theme = theme))
    }

    fun setMode(mode: ThemeMode) {
        update(config.copy(mode = mode))
    }

    fun initialize() {
        try {
            config = ThemeConfig.load()
        } catch (e: Exception) {
            // If loading fails, use default config
            config = ThemeConfig()
        }
    }
}

/**
 * Composition local for theme config
 */
val LocalThemeConfig = staticCompositionLocalOf { ThemeConfig() }

