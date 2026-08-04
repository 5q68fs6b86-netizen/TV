package com.fongmi.quickjs.crawler;

import com.whl.quickjs.android.QuickJSLoader;

import dalvik.system.DexClassLoader;

public class Loader {

    private final String tmdbApiKey;

    public Loader() {
        this("");
    }

    public Loader(String tmdbApiKey) {
        this.tmdbApiKey = tmdbApiKey == null ? "" : tmdbApiKey;
        QuickJSLoader.init();
    }

    public Spider spider(String api, DexClassLoader dex) {
        return new Spider(api, dex, tmdbApiKey);
    }
}
