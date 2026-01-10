package com.fongmi.android.tv.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.ui.base.BaseActivity;

/**
 * LiveActivity stub for TV Compose UI.
 * This will be replaced with Compose-based implementation.
 * TODO: Implement full Compose-based Live TV screen
 */
public class LiveActivity extends BaseActivity {

    public static void start(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, LiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected ViewBinding getBinding() {
        // TV flavor uses Compose, not ViewBinding
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Replace with Compose UI implementation
        // For now, this is a placeholder that the navigation system
        // will redirect to the Compose-based LiveScreen
    }
}
