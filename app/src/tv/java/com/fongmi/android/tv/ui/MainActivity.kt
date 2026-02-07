package com.fongmi.android.tv.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.config.WallConfig
import com.fongmi.android.tv.server.Server
import com.fongmi.android.tv.ui.navigation.TvNavGraph
import com.fongmi.android.tv.ui.theme.ThemeState
import com.fongmi.android.tv.ui.theme.TvAppTheme
import com.fongmi.android.tv.utils.Notify
import com.fongmi.android.tv.utils.Tbs
import com.fongmi.android.tv.impl.Callback
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the Compose-based TV UI.
 * Uses single Activity architecture with Navigation Compose.
 *
 * NOTE: Uses AppCompatActivity instead of ComponentActivity to support
 * MaterialAlertDialogBuilder used by jar plugins and WebDialog. The Compose
 * setContent extension works with AppCompatActivity via ActivityResultCaller.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var configLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start local server for jar plugins (they need it for dialogs/menus)
        Server.get().start()
        // Initialize X5 WebView engine (needed by jar plugins for custom menus/dialogs)
        Tbs.init()
        // Load configs on startup (like Leanback's HomeActivity.initConfig())
        initConfig()

        setContent {
            TvApp()
        }
    }

    /**
     * Initialize and load configs on startup.
     * Similar to Leanback's HomeActivity.initConfig()
     */
    private fun initConfig() {
        if (configLoading) return
        configLoading = true

        // Wall config
        WallConfig.get().init()

        // Live config - init and load
        LiveConfig.get().init().load()

        // Vod config - init and load with callback
        VodConfig.get().init().load(object : Callback() {
            override fun success() {
                configLoading = false
                // Config loaded successfully
            }

            override fun error(msg: String) {
                configLoading = false
                Notify.show(msg)
            }
        }, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop server when activity is destroyed
        if (isFinishing) {
            Server.get().stop()
        }
    }
}

@Composable
fun TvApp() {
    // Use ThemeState.config to get current theme configuration
    TvAppTheme(config = ThemeState.config) {
        val navController = rememberNavController()

        TvNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}
