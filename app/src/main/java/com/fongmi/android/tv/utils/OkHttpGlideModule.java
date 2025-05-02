package com.fongmi.android.tv.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder; // <--- Import GlideBuilder
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat; // <--- Import DecodeFormat
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions; // <--- Import RequestOptions
import com.github.catvod.net.OkHttp;

import java.io.InputStream;

@GlideModule
public class OkHttpGlideModule extends AppGlideModule {

    // --- ADD THIS METHOD ---
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Set default request options for high quality format
        builder.setDefaultRequestOptions(
                new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
        );
        // You could add other default options here if needed, like disk cache size etc.
        // For example:
        // int diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MB
        // builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
    // --- END ADD METHOD ---


    @Override
    public void registerComponents(@NonNull Context context, @Nullable Glide glide, Registry registry) {
        if (glide != null && registry != null) { // Add null checks for safety
            registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(OkHttp.client()));
        }
    }

    // Optional but recommended: Disable manifest parsing
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}