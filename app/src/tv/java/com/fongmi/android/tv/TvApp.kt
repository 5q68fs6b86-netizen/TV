package com.fongmi.android.tv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * TV Application with Hilt dependency injection.
 * This is the entry point for the new Compose-based TV UI.
 */
@HiltAndroidApp
class TvApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any required components here
        initializeApp()
    }

    private fun initializeApp() {
        // TODO: Initialize configurations from App.java
        // - VodConfig
        // - LiveConfig
        // - WallConfig
        // - Server
    }
}
