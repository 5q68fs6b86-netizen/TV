package com.fongmi.android.tv.utils;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fongmi.android.tv.setting.Setting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Process-wide Toast filter for spider promo text.
 * Keywords come from {@link Setting#getToastFilterKeywords()} (settings UI).
 * AlertDialog paths (e.g. config-center QR) are untouched.
 */
public final class ToastFilter {

    private static final String TAG = "ToastFilter";
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

    private ToastFilter() {
    }

    public static void install() {
        if (!INSTALLED.compareAndSet(false, true)) return;
        try {
            hookNotificationService();
            Log.i(TAG, "installed");
        } catch (Throwable t) {
            INSTALLED.set(false);
            Log.w(TAG, "install failed", t);
        }
    }

    public static boolean shouldBlock(CharSequence text) {
        if (!Setting.isToastFilter()) return false;
        if (TextUtils.isEmpty(text)) return false;
        List<String> keys = Setting.getToastFilterKeywords();
        if (keys.isEmpty()) return false;
        String lower = text.toString().toLowerCase(Locale.ROOT);
        for (String key : keys) {
            if (TextUtils.isEmpty(key)) continue;
            if (lower.contains(key.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private static void hookNotificationService() throws Exception {
        Method getService = Toast.class.getDeclaredMethod("getService");
        getService.setAccessible(true);
        Object service = getService.invoke(null);
        if (service == null) {
            // force Toast binder init on some API levels
            try {
                Toast.makeText(com.fongmi.android.tv.App.get(), "", Toast.LENGTH_SHORT);
            } catch (Throwable ignored) {
            }
            service = getService.invoke(null);
        }
        if (service == null) throw new IllegalStateException("Toast service null");

        Class<?> iNotificationManager = Class.forName("android.app.INotificationManager");
        Object original = service;
        Object proxy = Proxy.newProxyInstance(
                iNotificationManager.getClassLoader(),
                new Class<?>[]{iNotificationManager},
                (p, method, args) -> {
                    String name = method.getName();
                    if (name != null && name.startsWith("enqueueToast")) {
                        CharSequence text = extractText(args);
                        if (shouldBlock(text)) {
                            Log.i(TAG, "block: " + text);
                            return null;
                        }
                    }
                    try {
                        return method.invoke(original, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException re) throw re;
                        if (cause instanceof Error err) throw err;
                        throw e;
                    }
                }
        );

        Field sService = Toast.class.getDeclaredField("sService");
        sService.setAccessible(true);
        sService.set(null, proxy);
    }

    private static CharSequence extractText(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
            CharSequence from = readObjectText(arg);
            if (!TextUtils.isEmpty(from)) return from;
        }
        return null;
    }

    private static CharSequence readObjectText(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Toast toast) return readToastText(toast);
        CharSequence direct = readFieldText(obj, "mText", "text", "mNextView");
        if (!TextUtils.isEmpty(direct)) return direct;
        // Toast$TN / ITransientNotification impl
        try {
            for (Field f : obj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
                if (v instanceof View view) {
                    CharSequence t = findText(view);
                    if (!TextUtils.isEmpty(t)) return t;
                }
                if (v instanceof Toast toast) {
                    CharSequence t = readToastText(toast);
                    if (!TextUtils.isEmpty(t)) return t;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CharSequence readFieldText(Object obj, String... names) {
        for (String name : names) {
            try {
                Field f = obj.getClass().getDeclaredField(name);
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v instanceof CharSequence cs) return cs;
                if (v instanceof View view) return findText(view);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static CharSequence readToastText(Toast toast) {
        try {
            Method getText = Toast.class.getMethod("getText");
            Object v = getText.invoke(toast);
            if (v instanceof CharSequence cs) return cs;
        } catch (Throwable ignored) {
        }
        CharSequence fromField = readFieldText(toast, "mText", "mNextView");
        if (!TextUtils.isEmpty(fromField)) return fromField;
        try {
            Field tn = Toast.class.getDeclaredField("mTN");
            tn.setAccessible(true);
            return readObjectText(tn.get(toast));
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CharSequence findText(View view) {
        if (view instanceof TextView tv) return tv.getText();
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                CharSequence t = findText(group.getChildAt(i));
                if (!TextUtils.isEmpty(t)) return t;
            }
        }
        return null;
    }
}
