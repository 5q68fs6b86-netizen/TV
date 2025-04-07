package com.fongmi.android.tv.ui.base;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Animatable; // 引入 Animatable
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log; // 引入 Log
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // 引入 Nullable
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources; // 用于安全获取 Drawable
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide; // 引入 Glide
import com.bumptech.glide.request.target.CustomTarget; // 引入 CustomTarget
import com.bumptech.glide.request.transition.Transition; // 引入 Transition
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import me.jessyan.autosize.AutoSizeCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity"; // 添加日志标签

    // 用于存储 Glide 的 CustomTarget，方便在 onDestroy 中清理 (可选但推荐)
    private CustomTarget<Drawable> wallpaperTarget;

    protected abstract ViewBinding getBinding();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getBinding().getRoot()); // setContentView 会触发 refreshWall
        EventBus.getDefault().register(this);
        Util.hideSystemUI(this);
        setBackCallback();
        initView();
        initEvent();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        refreshWall(); // 在这里调用，确保 getWindow() 已经准备好
    }

    protected Activity getActivity() {
        return this;
    }

    protected boolean customWall() {
        // 默认启用自定义壁纸，子类可以重写返回 false 来禁用
        return true;
    }

    protected boolean handleBack() {
        // 返回 false 表示返回键回调默认不处理，由系统处理或 onBackPress 处理
        return false;
    }

    protected void initView() {
        // 子类实现具体的 View 初始化
    }

    protected void initEvent() {
        // 子类实现具体的事件监听设置
    }

    protected void onBackPress() {
        // 子类实现返回键被按下时的具体逻辑
        // 如果 handleBack() 返回 true, 这里默认会被调用且消耗事件
        // 如果 handleBack() 返回 false, 需要调用 super.onBackPressed() 或 finish()
        if (!getOnBackPressedDispatcher().hasEnabledCallbacks()) {
             // 如果没有启用回调处理（比如handleBack返回false且子类没重写启用），
             // 则执行默认的返回操作或关闭 Activity
             super.onBackPressed(); // 或者 finish();
        }
    }

    protected boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    protected boolean isGone(View view) {
        return view.getVisibility() == View.GONE;
    }

    protected void notifyItemChanged(RecyclerView view, ArrayObjectAdapter adapter) {
        if (view != null && adapter != null && !view.isComputingLayout()) {
            adapter.notifyArrayItemRangeChanged(0, adapter.size());
        }
    }

    protected void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        if (view != null && adapter != null && !view.isComputingLayout()) {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    }

    private void setBackCallback() {
        // handleBack() 控制回调是否一开始就启用。
        // 如果为 false, 它不会阻止返回事件，但 onBackPress 仍会被调用，
        // 此时需要在 onBackPress 中决定是否消耗事件或调用 super.onBackPressed()。
        // 如果为 true, 则默认会调用 onBackPress 并消耗事件。
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(handleBack()) {
            @Override
            public void handleOnBackPressed() {
                // 无论初始状态如何，当返回键按下时，都会调用 onBackPress
                onBackPress();
            }
        });
    }

    private void refreshWall() {
        // 先取消之前的 Glide 请求 (如果有的话)
        if (wallpaperTarget != null) {
            Glide.with(this).clear(wallpaperTarget);
            wallpaperTarget = null; // 清空引用
        }

        try {
            if (!customWall()) {
                // 如果禁用了自定义壁纸，确保设置一个默认背景（防止透明）
                 getWindow().setBackgroundDrawable(getDefaultWallpaperDrawable());
                 return;
            }

            File file = FileUtil.getWall(Setting.getWall()); // 获取壁纸文件
            Log.d(TAG, "Attempting to load wallpaper from: " + (file != null ? file.getAbsolutePath() : "null"));

            if (file != null && file.exists() && file.length() > 0) {
                // 文件存在，使用 Glide 加载
                Log.d(TAG, "File exists, loading with Glide...");
                wallpaperTarget = Glide.with(this) // 使用 Activity 作为 Context
                        .load(file)   // 加载文件 (Glide 会自动处理 GIF, WebP, PNG, JPG 等)
                        .placeholder(getDefaultWallpaperDrawable()) // 加载时显示默认壁纸
                        .error(getDefaultWallpaperDrawable()) // 加载失败时显示默认壁纸
                        .into(new CustomTarget<Drawable>() { // 使用 CustomTarget 来接收 Drawable
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                Log.d(TAG, "Wallpaper resource ready: " + resource.getClass().getSimpleName());
                                // 资源加载成功
                                getWindow().setBackgroundDrawable(resource);
                                // 关键: 如果 Drawable 是可动画的 (GIF, Animated WebP), 启动动画
                                if (resource instanceof Animatable) {
                                    Log.d(TAG, "Starting animatable wallpaper.");
                                    ((Animatable) resource).start();
                                }
                                // 加载成功后清除 target 引用，防止内存泄漏
                                wallpaperTarget = null;
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                Log.d(TAG, "Wallpaper load cleared.");
                                // 当 Glide 清除 Target 时调用 (例如 Activity 销毁或新的加载开始)
                                // 在这里可以设置回占位符或默认背景
                                if (getWindow() != null) { // 检查 Window 是否仍然有效
                                    getWindow().setBackgroundDrawable(placeholder != null ? placeholder : getDefaultWallpaperDrawable());
                                }
                                wallpaperTarget = null; // 清除引用
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Log.e(TAG, "Failed to load wallpaper.");
                                // 明确处理加载失败的情况
                                // errorDrawable 是在 .error() 中设置的 Drawable
                                 if (getWindow() != null) {
                                     getWindow().setBackgroundDrawable(errorDrawable != null ? errorDrawable : getDefaultWallpaperDrawable());
                                }
                                wallpaperTarget = null; // 清除引用
                            }
                        });
            } else {
                // 文件不存在或无效
                Log.w(TAG, "Wallpaper file does not exist or is empty. Loading fallback.");
                String filename = (file != null) ? file.getName() : null;
                getWindow().setBackgroundDrawable(getDefaultWallpaperDrawable(filename));
            }
        } catch (Exception e) {
            // 捕获任何其他异常，设置绝对默认壁纸
            Log.e(TAG, "Error refreshing wallpaper", e);
            try {
                getWindow().setBackgroundDrawable(getDefaultWallpaperDrawable()); // 使用辅助方法获取绝对默认
            } catch (Exception innerEx) {
                Log.e(TAG, "Error setting default wallpaper after exception", innerEx);
            }
        }
    }

    // 提取获取默认/备用壁纸的逻辑到一个辅助方法，更清晰且可重用
    private Drawable getDefaultWallpaperDrawable() {
        return getDefaultWallpaperDrawable(null); // 调用重载方法
    }

    private Drawable getDefaultWallpaperDrawable(@Nullable String filenameFromSetting) {
        Drawable fallbackDrawable = null;
        // 1. 尝试根据设置中的文件名从资源加载
        if (filenameFromSetting != null && !filenameFromSetting.isEmpty()) {
             Log.d(TAG, "Trying to load fallback from resource name: " + filenameFromSetting);
            try {
                // 假设 ResUtil.getDrawable 返回 drawable 资源的 ID
                // 需要确保 ResUtil.getDrawable 能处理文件名到资源 ID 的转换
                // 例如，它可能需要去除扩展名等操作
                int resId = ResUtil.getDrawable(filenameFromSetting);
                if (resId != 0) { // 假设 0 表示未找到
                    fallbackDrawable = AppCompatResources.getDrawable(this, resId);
                     Log.d(TAG, "Loaded fallback from resource ID: " + resId);
                } else {
                    Log.w(TAG, "Fallback resource not found for name: " + filenameFromSetting);
                }
            } catch (Resources.NotFoundException | NumberFormatException e) {
                 Log.w(TAG, "Exception finding fallback resource by name: " + filenameFromSetting, e);
                 // 忽略资源未找到或文件名无法解析为 ID 的异常
            }
        }

        // 2. 如果按名称查找失败或没有提供文件名，则加载绝对默认壁纸
        if (fallbackDrawable == null) {
             Log.d(TAG, "Loading absolute default wallpaper: R.drawable.wallpaper_1");
            try {
                fallbackDrawable = AppCompatResources.getDrawable(this, R.drawable.wallpaper_1);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Absolute default wallpaper resource (R.drawable.wallpaper_1) not found!", e);
                // 最后的保险：如果连默认 drawable 都没有，可以返回 null 或一个颜色 Drawable
                // return new android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK);
            }
        }
        return fallbackDrawable;
    }


    private Resources hackResources(Resources resources) {
        // 保留屏幕适配逻辑
        try {
            AutoSizeCompat.autoConvertDensityOfGlobal(resources); // 调用 AndroidAutoSize
            return resources;
        } catch (Exception ignored) {
             Log.w(TAG,"AutoSizeCompat failed.", ignored);
            return resources;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event != null && event.getType() == RefreshEvent.Type.WALL) {
            Log.d(TAG, "Received WALL refresh event.");
            refreshWall();
        }
    }

    @Override
    public Resources getResources() {
        // 应用屏幕适配
        return hackResources(super.getResources());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Util.hideSystemUI(this); // 保持全屏
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Util.hideSystemUI(this); // 重新获得焦点时确保全屏
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理 Glide 请求，防止内存泄漏
        if (wallpaperTarget != null) {
            Glide.with(this).clear(wallpaperTarget);
            Log.d(TAG, "Cleared Glide wallpaper target in onDestroy.");
        }
        // 注销 EventBus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            Log.d(TAG, "Unregistered from EventBus in onDestroy.");
        }
    }
}