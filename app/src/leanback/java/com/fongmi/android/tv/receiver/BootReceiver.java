package com.fongmi.android.tv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.LiveSetting;
import com.fongmi.android.tv.ui.activity.LiveActivity;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !isBootAction(intent.getAction())) return;
        registerCallback(context.getApplicationContext());
    }

    private boolean isBootAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.QUICKBOOT_POWERON".equals(action);
    }

    private void registerCallback(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) manager.registerDefaultNetworkCallback(new BootNetworkCallback(context, manager));
    }

    static class BootNetworkCallback extends ConnectivityManager.NetworkCallback {

        private final ConnectivityManager manager;
        private final Context context;

        BootNetworkCallback(Context context, ConnectivityManager manager) {
            this.context = context;
            this.manager = manager;
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            doJob();
        }

        @Override
        public void onLost(@NonNull Network network) {
        }

        private void doJob() {
            boolean resumeBoot = LiveSetting.isBoot();
            LiveConfig.get().init().load(new Callback() {
                @Override
                public void success() {
                    if (resumeBoot || LiveConfig.get().getHome().isBoot()) LiveActivity.start(context);
                    if (resumeBoot) LiveSetting.putBoot(false);
                }
            });
            manager.unregisterNetworkCallback(this);
        }
    }
}
