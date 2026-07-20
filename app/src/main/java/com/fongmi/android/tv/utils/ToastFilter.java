package com.fongmi.android.tv.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.setting.Setting;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Settings-driven Toast keyword filter.
 * <p>
 * Hooks {@code Toast.sService} so spider / third-party {@link Toast#show()} is filtered.
 * Keywords only come from settings — nothing hard-coded in the app.
 */
public final class ToastFilter {

    private static final String TAG = "ToastFilter";
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static final AtomicBoolean HOOKED = new AtomicBoolean(false);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static volatile Object originalService;
    private static volatile Object proxyService;

    private ToastFilter() {
    }

    public static void install() {
        if (!INSTALLED.compareAndSet(false, true)) {
            MAIN.post(ToastFilter::hookService);
            return;
        }
        exemptHiddenApi();
        MAIN.post(() -> {
            if (!hookService()) retry(0);
            MAIN.postDelayed(ToastFilter::keepAlive, 1500);
        });
        Log.i(TAG, "install scheduled");
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

    private static void keepAlive() {
        try {
            Field sService = accessible(Toast.class.getDeclaredField("sService"));
            Object cur = sService.get(null);
            if (proxyService == null || cur != proxyService) {
                HOOKED.set(false);
                hookService();
            }
        } catch (Throwable t) {
            Log.w(TAG, "keepAlive: " + t.getMessage());
        }
        MAIN.postDelayed(ToastFilter::keepAlive, 3000);
    }

    private static void retry(int n) {
        if (HOOKED.get()) return;
        if (hookService()) return;
        if (n >= 15) {
            Log.e(TAG, "toast hook failed permanently");
            return;
        }
        MAIN.postDelayed(() -> retry(n + 1), 250L * (n + 1));
    }

    private static void exemptHiddenApi() {
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Class<?> vmRuntime = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntime, "getRuntime", null);
            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntime, "setHiddenApiExemptions", new Class[]{String[].class});
            setHiddenApiExemptions.invoke(getRuntime.invoke(null), (Object) new String[]{"L"});
            Log.i(TAG, "hidden api exempt ok");
        } catch (Throwable t) {
            Log.w(TAG, "hiddenapi: " + t.getMessage());
        }
    }

    private static synchronized boolean hookService() {
        try {
            Method getService = accessible(Toast.class.getDeclaredMethod("getService"));
            Object service = getService.invoke(null);
            if (service == null) {
                try {
                    if (App.get() != null) {
                        Toast toast = Toast.makeText(App.get(), "", Toast.LENGTH_SHORT);
                        // do not show; just force binder init
                        Field tn = accessible(Toast.class.getDeclaredField("mTN"));
                        // touch service again
                    }
                } catch (Throwable ignored) {
                }
                service = getService.invoke(null);
            }
            if (service == null) {
                Log.w(TAG, "sService null");
                return false;
            }
            if (proxyService != null && service == proxyService) {
                HOOKED.set(true);
                return true;
            }
            // if already a proxy from us lost reference, still wrap again using current as original
            if (Proxy.isProxyClass(service.getClass()) && service == proxyService) {
                HOOKED.set(true);
                return true;
            }

            originalService = service;
            Class<?> iNotificationManager = Class.forName("android.app.INotificationManager");
            ClassLoader cl = iNotificationManager.getClassLoader();
            Class<?>[] ifaces = mergeInterfaces(service.getClass(), iNotificationManager);
            proxyService = Proxy.newProxyInstance(cl, ifaces, (p, method, args) -> {
                String name = method.getName();
                if (name != null && (name.startsWith("enqueueToast") || name.equals("enqueueTextToast"))) {
                    CharSequence text = extractText(args);
                    if (TextUtils.isEmpty(text)) {
                        text = extractTextDeep(args);
                    }
                    if (Setting.isToastFilter()) {
                        Log.d(TAG, name + " text=[" + text + "] args=" + describeArgs(args));
                    }
                    if (shouldBlock(text)) {
                        Log.i(TAG, "block: " + text);
                        return null;
                    }
                }
                try {
                    return method.invoke(originalService, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    Throwable c = e.getCause();
                    if (c instanceof RuntimeException re) throw re;
                    if (c instanceof Error err) throw err;
                    throw e;
                }
            });

            Field sService = accessible(Toast.class.getDeclaredField("sService"));
            unfinal(sService);
            boolean written = false;
            try {
                sService.set(null, proxyService);
                written = sService.get(null) == proxyService;
            } catch (Throwable t) {
                Log.w(TAG, "field set failed: " + t.getMessage());
            }
            if (!written) written = unsafeSet(sService, proxyService);
            if (!written) {
                Log.e(TAG, "cannot write Toast.sService");
                return false;
            }
            HOOKED.set(true);
            Log.i(TAG, "toast sService hooked");
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "hookService", t);
            return false;
        }
    }

    private static String describeArgs(Object[] args) {
        if (args == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(',');
            Object a = args[i];
            sb.append(a == null ? "null" : a.getClass().getName());
        }
        return sb.append(']').toString();
    }

    private static Class<?>[] mergeInterfaces(Class<?> concrete, Class<?> required) {
        List<Class<?>> list = new ArrayList<>();
        list.add(required);
        Class<?> c = concrete;
        while (c != null && c != Object.class) {
            for (Class<?> i : c.getInterfaces()) {
                if (!list.contains(i)) list.add(i);
            }
            c = c.getSuperclass();
        }
        return list.toArray(new Class<?>[0]);
    }

    private static <T extends java.lang.reflect.AccessibleObject> T accessible(T o) {
        o.setAccessible(true);
        return o;
    }

    private static void unfinal(Field f) {
        for (String name : new String[]{"modifiers", "accessFlags"}) {
            try {
                Field m = Field.class.getDeclaredField(name);
                m.setAccessible(true);
                m.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            } catch (Throwable ignored) {
            }
        }
    }

    private static boolean unsafeSet(Field field, Object value) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = accessible(unsafeClass.getDeclaredField("theUnsafe"));
            Object unsafe = theUnsafe.get(null);
            Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
            Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
            Method putObject = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
            Object base = staticFieldBase.invoke(unsafe, field);
            long offset = (Long) staticFieldOffset.invoke(unsafe, field);
            putObject.invoke(unsafe, base, offset, value);
            return field.get(null) == value;
        } catch (Throwable t) {
            // try jdk.internal.misc.Unsafe
            try {
                Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                Method getUnsafe = accessible(unsafeClass.getDeclaredMethod("getUnsafe"));
                Object unsafe = getUnsafe.invoke(null);
                Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
                Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
                Method putObject = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
                Object base = staticFieldBase.invoke(unsafe, field);
                long offset = (Long) staticFieldOffset.invoke(unsafe, field);
                putObject.invoke(unsafe, base, offset, value);
                return field.get(null) == value;
            } catch (Throwable t2) {
                Log.w(TAG, "unsafeSet: " + t.getMessage());
                return false;
            }
        }
    }

    private static CharSequence extractText(Object[] args) {
        if (args == null) return null;
        for (Object a : args) {
            if (a instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
            if (a instanceof Toast toast) {
                CharSequence t = readToastText(toast);
                if (!TextUtils.isEmpty(t)) return t;
            }
            if (a instanceof View view) {
                CharSequence t = findText(view);
                if (!TextUtils.isEmpty(t)) return t;
            }
        }
        // TN / ITransientNotification is usually args[1]
        for (Object a : args) {
            CharSequence t = readAnyText(a, 0);
            if (!TextUtils.isEmpty(t)) return t;
        }
        return null;
    }

    private static CharSequence extractTextDeep(Object[] args) {
        if (args == null) return null;
        ArrayDeque<Object> q = new ArrayDeque<>();
        Set<Integer> seen = new HashSet<>();
        for (Object a : args) if (a != null) q.add(a);
        int steps = 0;
        while (!q.isEmpty() && steps++ < 80) {
            Object o = q.removeFirst();
            if (!seen.add(System.identityHashCode(o))) continue;
            if (o instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
            if (o instanceof Toast toast) {
                CharSequence t = readToastText(toast);
                if (!TextUtils.isEmpty(t)) return t;
            }
            if (o instanceof View view) {
                CharSequence t = findText(view);
                if (!TextUtils.isEmpty(t)) return t;
            }
            if (o instanceof Reference<?> ref) {
                Object v = ref.get();
                if (v != null) q.add(v);
                continue;
            }
            if (o instanceof Object[] arr) {
                q.addAll(Arrays.asList(arr));
                continue;
            }
            String cn = o.getClass().getName();
            if (cn.startsWith("java.") && !cn.equals("java.lang.ref.WeakReference") && !cn.equals("java.lang.ref.SoftReference")) {
                continue;
            }
            Class<?> c = o.getClass();
            int depth = 0;
            while (c != null && c != Object.class && depth++ < 5) {
                Field[] fields;
                try {
                    fields = c.getDeclaredFields();
                } catch (Throwable e) {
                    break;
                }
                for (Field f : fields) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    try {
                        f.setAccessible(true);
                        Object v = f.get(o);
                        if (v == null) continue;
                        if (v instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
                        if (v instanceof Toast || v instanceof View || v instanceof Reference) {
                            q.add(v);
                            continue;
                        }
                        String fn = f.getName();
                        if ("this$0".equals(fn) || fn.contains("ext") || fn.contains("Text") || fn.contains("iew")
                                || fn.contains("oast") || "mTN".equals(fn) || "mNextView".equals(fn)
                                || "mText".equals(fn) || "mView".equals(fn) || "mPresenter".equals(fn)) {
                            q.add(v);
                        }
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static CharSequence readAnyText(Object o, int depth) {
        if (o == null || depth > 4) return null;
        if (o instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
        if (o instanceof Toast toast) return readToastText(toast);
        if (o instanceof View view) return findText(view);
        if (o instanceof Reference<?> ref) return readAnyText(ref.get(), depth + 1);
        try {
            for (String name : new String[]{"mNextView", "mView", "mText", "text", "mToast", "this$0"}) {
                try {
                    Field f = o.getClass().getDeclaredField(name);
                    f.setAccessible(true);
                    Object v = f.get(o);
                    CharSequence t = readAnyText(v, depth + 1);
                    if (!TextUtils.isEmpty(t)) return t;
                } catch (NoSuchFieldException ignored) {
                }
            }
            // superclass TN fields
            Class<?> c = o.getClass().getSuperclass();
            while (c != null && c != Object.class) {
                for (String name : new String[]{"mNextView", "mView", "mText"}) {
                    try {
                        Field f = c.getDeclaredField(name);
                        f.setAccessible(true);
                        Object v = f.get(o);
                        CharSequence t = readAnyText(v, depth + 1);
                        if (!TextUtils.isEmpty(t)) return t;
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CharSequence readToastText(Toast toast) {
        if (toast == null) return null;
        try {
            Method getText = Toast.class.getMethod("getText");
            Object v = getText.invoke(toast);
            if (v instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
        } catch (Throwable ignored) {
        }
        for (String name : new String[]{"mText", "mNextView", "mView"}) {
            try {
                Field f = accessible(Toast.class.getDeclaredField(name));
                Object v = f.get(toast);
                if (v instanceof Reference<?> ref) v = ref.get();
                if (v instanceof CharSequence cs && !TextUtils.isEmpty(cs)) return cs;
                if (v instanceof View view) {
                    CharSequence t = findText(view);
                    if (!TextUtils.isEmpty(t)) return t;
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            Field tn = accessible(Toast.class.getDeclaredField("mTN"));
            return readAnyText(tn.get(toast), 0);
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CharSequence findText(View view) {
        if (view == null) return null;
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
