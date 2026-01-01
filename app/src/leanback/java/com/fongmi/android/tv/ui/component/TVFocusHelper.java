package com.fongmi.android.tv.ui.component;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.R;

/**
 * TV 焦点辅助工具类
 * 提供统一的焦点动画效果，遵循 Google TV 设计规范
 * 
 * <p>特点：
 * <ul>
 *   <li>统一的焦点缩放动画</li>
 *   <li>支持 elevation 变化</li>
 *   <li>可配置的动画参数</li>
 *   <li>支持回调监听</li>
 * </ul>
 */
public class TVFocusHelper {

    // 默认动画参数
    private static final float DEFAULT_SCALE_FOCUSED = 1.05f;
    private static final float DEFAULT_SCALE_NORMAL = 1.0f;
    private static final float DEFAULT_ELEVATION_FOCUSED = 12f;
    private static final float DEFAULT_ELEVATION_NORMAL = 4f;
    private static final int DEFAULT_DURATION_MS = 200;
    
    // 动画参数
    private float scaleFocused = DEFAULT_SCALE_FOCUSED;
    private float scaleNormal = DEFAULT_SCALE_NORMAL;
    private float elevationFocused = DEFAULT_ELEVATION_FOCUSED;
    private float elevationNormal = DEFAULT_ELEVATION_NORMAL;
    private int durationMs = DEFAULT_DURATION_MS;
    
    // 当前动画
    private AnimatorSet currentAnimator;
    
    // 回调
    @Nullable
    private OnFocusChangeCallback callback;
    
    /**
     * 焦点变化回调接口
     */
    public interface OnFocusChangeCallback {
        void onFocusGained(View view);
        void onFocusLost(View view);
    }
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private final TVFocusHelper helper;
        
        public Builder() {
            helper = new TVFocusHelper();
        }
        
        public Builder setScaleFocused(float scale) {
            helper.scaleFocused = scale;
            return this;
        }
        
        public Builder setScaleNormal(float scale) {
            helper.scaleNormal = scale;
            return this;
        }
        
        public Builder setElevationFocused(float elevation) {
            helper.elevationFocused = elevation;
            return this;
        }
        
        public Builder setElevationNormal(float elevation) {
            helper.elevationNormal = elevation;
            return this;
        }
        
        public Builder setDuration(int durationMs) {
            helper.durationMs = durationMs;
            return this;
        }
        
        public Builder setCallback(@Nullable OnFocusChangeCallback callback) {
            helper.callback = callback;
            return this;
        }
        
        public TVFocusHelper build() {
            return helper;
        }
    }
    
    /**
     * 创建默认配置的 Helper
     */
    public static TVFocusHelper createDefault() {
        return new Builder().build();
    }
    
    /**
     * 创建卡片专用的 Helper（较大的缩放和 elevation）
     */
    public static TVFocusHelper createForCard() {
        return new Builder()
                .setScaleFocused(1.08f)
                .setElevationFocused(16f)
                .setElevationNormal(4f)
                .build();
    }
    
    /**
     * 创建按钮专用的 Helper（较小的缩放，无 elevation 变化）
     */
    public static TVFocusHelper createForButton() {
        return new Builder()
                .setScaleFocused(1.03f)
                .setElevationFocused(0f)
                .setElevationNormal(0f)
                .build();
    }
    
    /**
     * 创建列表项专用的 Helper（轻微缩放）
     */
    public static TVFocusHelper createForListItem() {
        return new Builder()
                .setScaleFocused(1.02f)
                .setElevationFocused(8f)
                .setElevationNormal(0f)
                .build();
    }
    
    /**
     * 将焦点处理附加到视图
     * 
     * @param view 要附加焦点处理的视图
     */
    public void attachToView(@NonNull View view) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            handleFocusChange(v, hasFocus);
        });
    }
    
    /**
     * 将焦点处理附加到多个视图
     * 
     * @param views 要附加焦点处理的视图列表
     */
    public void attachToViews(@NonNull View... views) {
        for (View view : views) {
            attachToView(view);
        }
    }
    
    /**
     * 处理焦点变化
     * 
     * @param view 焦点变化的视图
     * @param hasFocus 是否获得焦点
     */
    public void handleFocusChange(@NonNull View view, boolean hasFocus) {
        // 取消当前动画
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
        
        // 创建动画
        if (hasFocus) {
            animateFocusGained(view);
            if (callback != null) {
                callback.onFocusGained(view);
            }
        } else {
            animateFocusLost(view);
            if (callback != null) {
                callback.onFocusLost(view);
            }
        }
    }
    
    /**
     * 获得焦点时的动画
     */
    private void animateFocusGained(@NonNull View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, scaleNormal, scaleFocused);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scaleNormal, scaleFocused);
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        if (elevationFocused > 0) {
            ObjectAnimator elevation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, 
                    elevationNormal, elevationFocused);
            animatorSet.playTogether(scaleX, scaleY, elevation);
        } else {
            animatorSet.playTogether(scaleX, scaleY);
        }
        
        animatorSet.setDuration(durationMs);
        animatorSet.setInterpolator(new OvershootInterpolator(1.02f));
        
        currentAnimator = animatorSet;
        animatorSet.start();
    }
    
    /**
     * 失去焦点时的动画
     */
    private void animateFocusLost(@NonNull View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, scaleFocused, scaleNormal);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scaleFocused, scaleNormal);
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        if (elevationFocused > 0) {
            ObjectAnimator elevation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, 
                    elevationFocused, elevationNormal);
            animatorSet.playTogether(scaleX, scaleY, elevation);
        } else {
            animatorSet.playTogether(scaleX, scaleY);
        }
        
        animatorSet.setDuration(durationMs);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        currentAnimator = animatorSet;
        animatorSet.start();
    }
    
    /**
     * 立即应用焦点状态（无动画）
     * 
     * @param view 目标视图
     * @param hasFocus 是否有焦点
     */
    public void applyFocusStateImmediate(@NonNull View view, boolean hasFocus) {
        if (hasFocus) {
            view.setScaleX(scaleFocused);
            view.setScaleY(scaleFocused);
            view.setTranslationZ(elevationFocused);
        } else {
            view.setScaleX(scaleNormal);
            view.setScaleY(scaleNormal);
            view.setTranslationZ(elevationNormal);
        }
    }
    
    /**
     * 重置视图状态
     * 
     * @param view 目标视图
     */
    public void reset(@NonNull View view) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        view.setScaleX(scaleNormal);
        view.setScaleY(scaleNormal);
        view.setTranslationZ(elevationNormal);
    }
    
    /**
     * 从资源获取尺寸值
     */
    public static float getDimenPx(Context context, int dimenResId) {
        return context.getResources().getDimension(dimenResId);
    }
    
    /**
     * 从资源获取动画时长
     */
    public static int getAnimDuration(Context context, int integerResId) {
        return context.getResources().getInteger(integerResId);
    }
    
    // Getters
    public float getScaleFocused() {
        return scaleFocused;
    }
    
    public float getScaleNormal() {
        return scaleNormal;
    }
    
    public float getElevationFocused() {
        return elevationFocused;
    }
    
    public float getElevationNormal() {
        return elevationNormal;
    }
    
    public int getDurationMs() {
        return durationMs;
    }
    
    public void setCallback(@Nullable OnFocusChangeCallback callback) {
        this.callback = callback;
    }
}