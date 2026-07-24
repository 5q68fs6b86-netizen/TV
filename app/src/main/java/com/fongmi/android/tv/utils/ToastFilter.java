package com.fongmi.android.tv.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fongmi.android.tv.setting.Setting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import top.canyie.pine.Pine;
import top.canyie.pine.PineConfig;
import top.canyie.pine.callback.MethodHook;

/**
 * Settings-driven Toast keyword filter for spider system toasts.
 * <p>
 * Covers spider paths that call platform {@link Toast} directly:
 * <ul>
 *   <li>{@code Toast.makeText(...).show()}</li>
 *   <li>{@code new Toast(ctx); setText(...); show()}</li>
 *   <li>custom view toast whose text lives in a {@link TextView}</li>
 * </ul>
 * Design for API 30+ / Android 11:
 * <ul>
 *   <li>Pine hooks install only when the user enables the filter.</li>
 *   <li>Install once on the main thread; never from {@code JarLoader}/DexClassLoader paths.</li>
 *   <li>App-owned paths ({@link Notify}, {@link com.fongmi.android.tv.ui.custom.OverlayToast})
 *       always use {@link #shouldBlock} without needing hooks.</li>
 *   <li>Hook failure is logged and never rethrows into spider load.</li>
 * </ul>
 */
public final class ToastFilter {

    private static final String TAG = "ToastFilter";
    private static final AtomicBoolean HOOKS_INSTALLED = new AtomicBoolean(false);
    private static final AtomicBoolean HOOKS_FAILED = new AtomicBoolean(false);
    private static final AtomicBoolean INSTALL_SCHEDULED = new AtomicBoolean(false);
    private static final Map<Toast, CharSequence> TEXTS = new WeakHashMap<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ToastFilter() {
    }

    /**
     * Soft entry: schedule Pine install only when filter is enabled.
     * Safe to call from {@link android.app.Application#onCreate()}; no-op when disabled.
     */
    public static void install() {
        if (!Setting.isToastFilter()) return;
        if (HOOKS_INSTALLED.get() || HOOKS_FAILED.get()) return;
        scheduleInstall();
    }

    /**
     * Call when user toggles the setting on, so hooks appear without restart.
     */
    public static void onFilterEnabledChanged() {
        if (!Setting.isToastFilter()) return;
        HOOKS_FAILED.set(false);
        INSTALL_SCHEDULED.set(false);
        scheduleInstall();
    }

    private static void scheduleInstall() {
        if (HOOKS_INSTALLED.get() || HOOKS_FAILED.get()) return;
        if (!INSTALL_SCHEDULED.compareAndSet(false, true)) return;
        Runnable task = () -> {
            try {
                if (!Setting.isToastFilter()) return;
                if (HOOKS_INSTALLED.get() || HOOKS_FAILED.get()) return;
                installPineHooks();
                HOOKS_INSTALLED.set(true);
                Log.i(TAG, "pine toast hooks installed api=" + Build.VERSION.SDK_INT);
            } catch (Throwable t) {
                HOOKS_FAILED.set(true);
                HOOKS_INSTALLED.set(false);
                Log.e(TAG, "install failed (app toasts still filtered via shouldBlock)", t);
            } finally {
                INSTALL_SCHEDULED.set(false);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) task.run();
        else MAIN.post(task);
    }

    public static boolean shouldBlock(CharSequence text) {
        if (!Setting.isToastFilter()) return false;
        if (TextUtils.isEmpty(text)) return false;
        List<String> keys = Setting.getToastFilterKeywords();
        if (keys.isEmpty()) return false;
        String body = text.toString();
        String lower = body.toLowerCase(Locale.ROOT);
        for (String key : keys) {
            if (TextUtils.isEmpty(key)) continue;
            String k = key.trim();
            if (k.isEmpty()) continue;
            if (lower.contains(k.toLowerCase(Locale.ROOT)) || body.contains(k)) {
                Log.i(TAG, "hit key=[" + k + "] text=[" + body + "]");
                return true;
            }
        }
        return false;
    }

    private static void installPineHooks() throws Throwable {
        // Conservative: do not disable platform hidden-api policy globally on API 30+
        // (that path previously re-entered around DexClassLoader and broke jar load).
        PineConfig.debug = false;
        PineConfig.debuggable = false;
        if (Build.VERSION.SDK_INT < 30) {
            PineConfig.disableHiddenApiPolicy = true;
            PineConfig.disableHiddenApiPolicyForPlatformDomain = true;
        } else {
            PineConfig.disableHiddenApiPolicy = false;
            PineConfig.disableHiddenApiPolicyForPlatformDomain = false;
        }
        Pine.ensureInitialized();

        // makeText(Context, CharSequence, int) — most spider promo toasts
        Method makeTextCs = Toast.class.getDeclaredMethod("makeText", Context.class, CharSequence.class, int.class);
        Pine.hook(makeTextCs, new MethodHook() {
            @Override
            public void afterCall(Pine.CallFrame frame) {
                try {
                    Object result = frame.getResult();
                    Object[] args = frame.args;
                    CharSequence text = args != null && args.length > 1 && args[1] instanceof CharSequence
                            ? (CharSequence) args[1] : null;
                    if (result instanceof Toast toast) {
                        remember(toast, text);
                        if (shouldBlock(text)) Log.i(TAG, "makeText marked: " + text);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "makeText hook: " + t.getMessage());
                }
            }
        });

        // makeText(Context, int resId, int)
        try {
            Method makeTextRes = Toast.class.getDeclaredMethod("makeText", Context.class, int.class, int.class);
            Pine.hook(makeTextRes, new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame frame) {
                    try {
                        Object result = frame.getResult();
                        Object[] args = frame.args;
                        if (!(result instanceof Toast toast) || args == null || args.length < 2) return;
                        Context ctx = (Context) args[0];
                        int resId = (Integer) args[1];
                        CharSequence text = null;
                        try {
                            text = ctx.getText(resId);
                        } catch (Throwable ignored) {
                        }
                        remember(toast, text);
                    } catch (Throwable t) {
                        Log.w(TAG, "makeText(res) hook: " + t.getMessage());
                    }
                }
            });
        } catch (NoSuchMethodException ignored) {
        }

        // setText(CharSequence) — new Toast(ctx); toast.setText(...); toast.show()
        try {
            Method setTextCs = Toast.class.getDeclaredMethod("setText", CharSequence.class);
            Pine.hook(setTextCs, new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame frame) {
                    try {
                        Object[] args = frame.args;
                        CharSequence text = args != null && args.length > 0 && args[0] instanceof CharSequence
                                ? (CharSequence) args[0] : null;
                        if (frame.thisObject instanceof Toast toast) {
                            remember(toast, text);
                            if (shouldBlock(text)) Log.i(TAG, "setText marked: " + text);
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, "setText hook: " + t.getMessage());
                    }
                }
            });
        } catch (NoSuchMethodException ignored) {
        }

        // setText(int resId)
        try {
            Method setTextRes = Toast.class.getDeclaredMethod("setText", int.class);
            Pine.hook(setTextRes, new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame frame) {
                    try {
                        if (!(frame.thisObject instanceof Toast toast)) return;
                        Object[] args = frame.args;
                        if (args == null || args.length < 1) return;
                        int resId = (Integer) args[0];
                        CharSequence text = null;
                        try {
                            Context ctx = toast.getView() != null
                                    ? toast.getView().getContext()
                                    : com.fongmi.android.tv.App.get();
                            text = ctx.getText(resId);
                        } catch (Throwable ignored) {
                        }
                        remember(toast, text);
                    } catch (Throwable t) {
                        Log.w(TAG, "setText(res) hook: " + t.getMessage());
                    }
                }
            });
        } catch (NoSuchMethodException ignored) {
        }

        // show() — final gate for spider system toasts
        Method show = Toast.class.getDeclaredMethod("show");
        Pine.hook(show, new MethodHook() {
            @Override
            public void beforeCall(Pine.CallFrame frame) {
                try {
                    Toast toast = (Toast) frame.thisObject;
                    CharSequence text = recall(toast);
                    if (TextUtils.isEmpty(text)) text = extractText(toast);
                    if (shouldBlock(text)) {
                        Log.i(TAG, "show blocked: " + text);
                        frame.setResult(null);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "show hook: " + t.getMessage());
                }
            }
        });
    }

    private static void remember(Toast toast, CharSequence text) {
        if (toast == null) return;
        synchronized (TEXTS) {
            TEXTS.put(toast, text);
        }
    }

    private static CharSequence recall(Toast toast) {
        if (toast == null) return null;
        synchronized (TEXTS) {
            return TEXTS.get(toast);
        }
    }

    /**
     * Best-effort text recovery when spider builds toast without makeText/setText hooks
     * (custom view, or field-backed text on newer Android).
     */
    private static CharSequence extractText(Toast toast) {
        if (toast == null) return null;
        try {
            View view = null;
            try {
                view = toast.getView();
            } catch (Throwable ignored) {
            }
            if (view != null) {
                CharSequence fromView = findText(view);
                if (!TextUtils.isEmpty(fromView)) return fromView;
            }
            // Fallback: private mNextView / mText fields (varies by API / OEM)
            for (String name : new String[]{"mNextView", "mView"}) {
                try {
                    Field f = Toast.class.getDeclaredField(name);
                    f.setAccessible(true);
                    Object v = f.get(toast);
                    if (v instanceof View) {
                        CharSequence t = findText((View) v);
                        if (!TextUtils.isEmpty(t)) return t;
                    }
                } catch (Throwable ignored) {
                }
            }
            try {
                Field f = Toast.class.getDeclaredField("mText");
                f.setAccessible(true);
                Object t = f.get(toast);
                if (t instanceof CharSequence) return (CharSequence) t;
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CharSequence findText(View view) {
        if (view instanceof TextView tv) {
            CharSequence t = tv.getText();
            if (!TextUtils.isEmpty(t)) return t;
        }
        if (view instanceof ViewGroup group) {
            int n = group.getChildCount();
            for (int i = 0; i < n; i++) {
                CharSequence t = findText(group.getChildAt(i));
                if (!TextUtils.isEmpty(t)) return t;
            }
        }
        return null;
    }
}
