package com.fongmi.android.tv.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fongmi.android.tv.setting.Setting;

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
 * Settings-driven Toast keyword filter.
 * Uses Pine to hook {@link Toast#makeText} / {@link Toast#show} so spider-created
 * system toasts can be blocked when any configured keyword matches.
 */
public final class ToastFilter {

    private static final String TAG = "ToastFilter";
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static final Map<Toast, CharSequence> TEXTS = new WeakHashMap<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ToastFilter() {
    }

    public static void install() {
        if (!INSTALLED.compareAndSet(false, true)) return;
        MAIN.post(() -> {
            try {
                installPineHooks();
                Log.i(TAG, "pine toast hooks installed");
            } catch (Throwable t) {
                INSTALLED.set(false);
                Log.e(TAG, "install failed", t);
            }
        });
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
        PineConfig.debug = false;
        PineConfig.debuggable = false;
        PineConfig.disableHiddenApiPolicy = true;
        PineConfig.disableHiddenApiPolicyForPlatformDomain = true;
        Pine.ensureInitialized();

        // makeText(Context, CharSequence, int)
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
                        // do not recreate Toast here (would re-enter hook); show() will block
                        if (shouldBlock(text)) {
                            Log.i(TAG, "makeText marked: " + text);
                        }
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "makeText hook: " + t.getMessage());
                }
            }
        });

        // makeText(Context, int, int) — res id path (unlikely for spider promo)
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

        // show()
        Method show = Toast.class.getDeclaredMethod("show");
        Pine.hook(show, new MethodHook() {
            @Override
            public void beforeCall(Pine.CallFrame frame) {
                try {
                    Toast toast = (Toast) frame.thisObject;
                    CharSequence text = recall(toast);
                    if (TextUtils.isEmpty(text)) text = readToastText(toast);
                    if (shouldBlock(text)) {
                        Log.i(TAG, "show blocked: " + text);
                        frame.setResult(null); // skip original show
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

    private static CharSequence readToastText(Toast toast) {
        try {
            Method getText = Toast.class.getMethod("getText");
            Object v = getText.invoke(toast);
            if (v instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field f = Toast.class.getDeclaredField("mNextView");
            f.setAccessible(true);
            Object v = f.get(toast);
            if (v instanceof View view) return findText(view);
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field f = Toast.class.getDeclaredField("mText");
            f.setAccessible(true);
            Object v = f.get(toast);
            if (v instanceof CharSequence cs) return cs;
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
            for (int i = 0; i < group.getChildCount(); i++) {
                CharSequence t = findText(group.getChildAt(i));
                if (!TextUtils.isEmpty(t)) return t;
            }
        }
        return null;
    }
}
