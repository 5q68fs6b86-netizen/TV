package com.fongmi.quickjs.utils;

import android.text.TextUtils;
import android.util.LruCache;

import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Asset;
import com.orhanobut.logger.Logger;

public class Module {

    private static final String TAG = Module.class.getSimpleName();
    private static final int MAX_SIZE = 50;
    private final LruCache<String, String> cache;

    public Module() {
        cache = new LruCache<>(MAX_SIZE);
    }

    public static Module get() {
        return Loader.INSTANCE;
    }

    public String fetch(String name) {
        String content = cache.get(name);
        if (!TextUtils.isEmpty(content)) {
            Logger.t(TAG).d("fetch cache name=%s length=%s", name, content.length());
            return content;
        }
        long start = System.currentTimeMillis();
        Logger.t(TAG).d("fetch start name=%s", name);
        if (name.startsWith("http")) cache.put(name, content = OkHttp.string(name));
        else if (name.startsWith("assets")) cache.put(name, content = Asset.read(name));
        else if (name.startsWith("lib/")) cache.put(name, content = Asset.read("js/" + name));
        if (TextUtils.isEmpty(content)) {
            Logger.t(TAG).e("fetch empty name=" + name + " elapsed=" + (System.currentTimeMillis() - start) + "ms");
            throw new IllegalStateException("Module fetch empty: " + name);
        }
        Logger.t(TAG).d("fetch success name=%s length=%s elapsed=%sms", name, content.length(), System.currentTimeMillis() - start);
        return content;
    }

    public void clear() {
        cache.evictAll();
    }

    private static class Loader {
        static volatile Module INSTANCE = new Module();
    }
}
