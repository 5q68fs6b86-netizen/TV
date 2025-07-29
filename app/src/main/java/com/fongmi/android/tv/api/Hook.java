package com.fongmi.android.tv.api;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import com.fongmi.android.tv.App;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Hook {

    private static final String PROXY = "com.github.catvod.app.Proxy";
    private static final String TAG = Hook.class.getSimpleName();

    private static Class<?> getProxy() throws Exception {
        return Class.forName(PROXY);
    }

    public static void set(App app) {
        try {
            getProxy().getMethod("init", App.class).invoke(null, app);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        try {
            getProxy().getMethod("init").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return new ContextWrapper(App.get()) {
            @Override
            public Context getApplicationContext() {
                return this;
            }

            @Override
            public Object getSystemService(String name) {
                if (Context.ACTIVITY_SERVICE.equals(name)) return App.get().getSystemService(name);
                return super.getSystemService(name);
            }

            @Override
            public PackageManager getPackageManager() {
                return (PackageManager) Proxy.newProxyInstance(App.get().getClassLoader(), new Class[]{PackageManager.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("getPackageInfo")) {
                            String name = (String) args[0];
                            int flags = (int) args[1];
                            if (flags == PackageManager.GET_SIGNATURES) {
                                PackageInfo info = App.get().getPackageManager().getPackageInfo(name, flags);
                                info.signatures = new Signature[]{new Signature("7223DE1AAE9E09110A3007C980AF081B1609A3FE")};
                                return info;
                            }
                        }
                        return method.invoke(App.get().getPackageManager(), args);
                    }
                });
            }
        };
    }

    public static void check(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageManager hookedPM = context.getPackageManager();
            PackageInfo hookedPI = hookedPM.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            String hookedSignature = hookedPI.signatures[0].toCharsString();
            Log.d(TAG, "Signature as seen by JAR: " + hookedSignature);
            PackageManager realPM = App.get().getPackageManager();
            PackageInfo realPI = realPM.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            String realSignature = realPI.signatures[0].toCharsString();
            Log.d(TAG, "Real App Signature: " + realSignature);
            if (hookedSignature.equals(realSignature)) {
                Log.d(TAG, "Signature check PASSED (Hook is NOT working or signature is genuine)");
            } else {
                Log.d(TAG, "Signature check FAILED (Hook is working, signature is forged)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during signature check", e);
        }
    }
}