package com.fongmi.android.tv.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.utils.Util;

import org.greenrobot.eventbus.EventBus;

/**
 * Base Activity for TV Compose UI.
 * This is a simplified version that provides compatibility with main source code.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract ViewBinding getBinding();

    /**
     * Override to return true if this Activity uses EventBus.
     * Default is false to avoid crashes for Activities without @Subscribe methods.
     */
    protected boolean useEventBus() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getBinding() != null) {
            setContentView(getBinding().getRoot());
        }
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
        Util.hideSystemUI(this);
        setBackCallback();
        initView();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected Activity getActivity() {
        return this;
    }

    protected boolean handleBack() {
        return false;
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void onBackPress() {
    }

    protected boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    private void setBackCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!handleBack()) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                } else {
                    onBackPress();
                }
            }
        });
    }
}
