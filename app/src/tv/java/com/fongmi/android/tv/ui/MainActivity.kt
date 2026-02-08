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
import com.github.catvod.Proxy
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
    private var userFinish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start local server for jar plugins (they need it for dialogs/menus)
        Server.get().start()
        System.out.println("TV_Proxy_Debug: Server.start() done, Server.port=${Server.get().port}, Proxy.getPort()=${Proxy.getPort()}")
        // Initialize X5 WebView engine (needed by jar plugins for custom menus/dialogs)
        Tbs.init()
        // Load configs on startup (like Leanback's HomeActivity.initConfig())
        initConfig()

        setContent {
            TvApp()
        }
    }

    /**
     * Guard against JAR plugins calling Activity.finish().
     *
     * JAR plugins (loaded via DexClassLoader) may call Init.getActivity().finish()
     * during detailContent() to close the current Activity before showing a dialog.
     * In Leanback's multi-Activity architecture, this closes VideoActivity and the
     * dialog shows on HomeActivity. In our single-Activity Compose architecture,
     * this would destroy the ONLY Activity, killing the app.
     *
     * We block external finish() calls and only allow finish when the user
     * explicitly navigates away (back press / task removal).
     */
    override fun finish() {
        if (userFinish) {
            super.finish()
        } else {
            System.out.println("TV_Dialog_Debug: MainActivity.finish() BLOCKED (called by JAR plugin)")
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        userFinish = true
        super.onBackPressed()
        userFinish = false
    }

    override fun finishAffinity() {
        userFinish = true
        super.finishAffinity()
    }

    override fun finishAndRemoveTask() {
        userFinish = true
        super.finishAndRemoveTask()
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
