package com.fongmi.android.tv

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.fongmi.android.tv.ui.activity.CrashActivity
import com.fongmi.android.tv.utils.LanguageUtil
import com.fongmi.android.tv.utils.Notify
import com.github.catvod.Init
import com.github.catvod.bean.Doh
import com.github.catvod.net.OkHttp
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * TV Application with Hilt dependency injection.
 * This is the entry point for the new Compose-based TV UI.
 * Initialization logic migrated from App.java.
 */
@HiltAndroidApp
class TvApp : Application() {

    private val executor: ExecutorService = Executors.newFixedThreadPool(Constant.THREAD_POOL)
    private val handler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private var currentActivity: Activity? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
        Init.set(base)
    }

    override fun onCreate() {
        super.onCreate()
        initializeApp()
        registerActivityCallbacks()
    }

    private fun initializeApp() {
        // Create notification channels directly (avoid Notify.createChannel which uses App.get())
        createNotificationChannel()

        // Initialize language settings
        LanguageUtil.init(this)

        // Setup logger
        Logger.addLogAdapter(
            AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .methodCount(0)
                    .showThreadInfo(false)
                    .tag("")
                    .build()
            )
        )

        // Configure OkHttp with proxy and DoH settings
        OkHttp.get().setProxy(Setting.getProxy())
        OkHttp.get().setDoh(Doh.objectFrom(Setting.getDoh()))

        // Theme state will initialize with defaults lazily

        // Setup crash handler
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .errorActivity(CrashActivity::class.java)
            .apply()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "default",
                "TV",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun registerActivityCallbacks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity != currentActivity) currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                if (activity != currentActivity) currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity != currentActivity) currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity == currentActivity) currentActivity = null
            }

            override fun onActivityStopped(activity: Activity) {
                if (activity == currentActivity) currentActivity = null
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (activity == currentActivity) currentActivity = null
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    companion object {
        private lateinit var instance: TvApp

        @JvmStatic
        fun get(): TvApp = instance

        @JvmStatic
        fun activity(): Activity? = get().currentActivity

        @JvmStatic
        fun execute(runnable: Runnable) {
            get().executor.execute(runnable)
        }

        @JvmStatic
        fun post(runnable: Runnable) {
            get().handler.post(runnable)
        }

        @JvmStatic
        fun post(runnable: Runnable, delayMillis: Long) {
            get().handler.removeCallbacks(runnable)
            if (delayMillis >= 0) get().handler.postDelayed(runnable, delayMillis)
        }

        @JvmStatic
        fun removeCallbacks(runnable: Runnable) {
            get().handler.removeCallbacks(runnable)
        }
    }
}
