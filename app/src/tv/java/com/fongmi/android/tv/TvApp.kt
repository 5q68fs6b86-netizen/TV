package com.fongmi.android.tv

import android.content.Context
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.config.WallConfig
import com.fongmi.android.tv.ui.activity.CrashActivity
import com.fongmi.android.tv.ui.theme.ThemeState
import com.github.catvod.Init
import dagger.hilt.android.HiltAndroidApp

/**
 * TV Application with Hilt dependency injection.
 * Extends App to ensure all Java code using App.get(), App.execute(), App.gson() etc. works correctly.
 *
 * NOTE: Overrides shouldClearActivityOnPause to keep activity reference during pause/stop.
 * This is needed because TV variant uses single Activity architecture and jar plugins
 * may create dialogs that require a valid Activity context even when paused.
 */
@HiltAndroidApp
class TvApp : App() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        tvInstance = this
        Init.set(base)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize configs (must call init() before load())
        initConfigs()
        // Initialize TV Compose specific components
        ThemeState.initialize()
        // Override crash handler with TV-specific activity
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .errorActivity(CrashActivity::class.java)
            .apply()
    }

    /**
     * Don't clear activity reference on pause/stop.
     * TV variant uses single Activity architecture, and jar plugins may need
     * the activity reference to create dialogs even when the activity is paused
     * (e.g., when a system dialog or external activity is shown).
     */
    override fun shouldClearActivityOnPause(): Boolean = false

    private fun initConfigs() {
        // Initialize config singletons with empty lists
        // This prevents NPE when load() calls clear()
        WallConfig.get().init()
        LiveConfig.get().init()
        VodConfig.get().init()
    }

    companion object {
        private lateinit var tvInstance: TvApp

        @JvmStatic
        fun get(): TvApp = tvInstance
    }
}
