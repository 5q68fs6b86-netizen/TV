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
import com.fongmi.android.tv.ui.navigation.TvNavGraph
import com.fongmi.android.tv.ui.theme.ThemeState
import com.fongmi.android.tv.ui.theme.TvAppTheme
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TvApp()
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
