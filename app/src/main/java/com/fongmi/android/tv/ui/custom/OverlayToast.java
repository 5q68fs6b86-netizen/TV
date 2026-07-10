package com.fongmi.android.tv.ui.custom;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.fongmi.android.tv.R;
import com.google.android.material.textview.MaterialTextView;

/**
 * JetStream 风格的应用内提示浮层：深色圆角胶囊，底部居中淡入淡出。
 * 附着在当前 Activity 的 decorView 上，替代系统 Toast 的样式。
 */
public class OverlayToast {

    private static final long DURATION = 2800;

    private static MaterialTextView current;
    private static final Runnable hide = OverlayToast::hide;

    public static void show(Activity activity, String text) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        MaterialTextView view = current;
        if (view != null && view.getParent() == decor) {
            view.setText(text);
        } else {
            detach();
            view = create(activity, text);
            decor.addView(view, params(view));
            view.setAlpha(0f);
            view.setTranslationY(dp(view, 16));
            view.animate().alpha(1f).translationY(0f).setDuration(220).setInterpolator(new DecelerateInterpolator(1.6f)).start();
            current = view;
        }
        view.removeCallbacks(hide);
        view.postDelayed(hide, DURATION);
    }

    private static void hide() {
        MaterialTextView view = current;
        if (view == null) return;
        view.animate().alpha(0f).translationY(dp(view, 12)).setDuration(180).withEndAction(OverlayToast::detach).start();
    }

    private static void detach() {
        MaterialTextView view = current;
        current = null;
        if (view == null) return;
        view.removeCallbacks(hide);
        view.animate().cancel();
        if (view.getParent() instanceof ViewGroup) ((ViewGroup) view.getParent()).removeView(view);
    }

    private static MaterialTextView create(Activity activity, String text) {
        MaterialTextView view = new MaterialTextView(activity);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        view.setTextColor(ContextCompat.getColor(activity, R.color.jetstream_on_surface));
        view.setMaxLines(3);
        applyBrandTypeface(activity, view);
        view.setElevation(dp(view, 8));
        int horizontal = (int) dp(view, 22);
        int vertical = (int) dp(view, 12);
        view.setPadding(horizontal, vertical, horizontal, vertical);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(view, 24));
        background.setColor(ContextCompat.getColor(activity, R.color.jetstream_surface_container_high));
        background.setStroke((int) dp(view, 1), ContextCompat.getColor(activity, R.color.jetstream_outline_variant));
        view.setBackground(background);
        return view;
    }

    private static void applyBrandTypeface(Activity activity, MaterialTextView view) {
        // 字体资源只存在于 leanback 变体，动态查找避免 mobile 编译依赖
        int fontId = activity.getResources().getIdentifier("misans", "font", activity.getPackageName());
        if (fontId == 0) return;
        try {
            Typeface typeface = ResourcesCompat.getFont(activity, fontId);
            if (typeface != null) view.setTypeface(typeface);
        } catch (Exception ignored) {
        }
    }

    private static FrameLayout.LayoutParams params(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = (int) dp(view, 56);
        params.leftMargin = (int) dp(view, 48);
        params.rightMargin = (int) dp(view, 48);
        return params;
    }

    private static float dp(View view, int value) {
        return value * view.getResources().getDisplayMetrics().density;
    }
}
