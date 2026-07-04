package com.fongmi.android.tv.utils;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.bean.Vod;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Response;

public class FeaturedPosterCache {

    private static final String DIR = "featured_poster_cache";
    private static final String META = "meta.json";
    private static final String TAG = "featured_poster_cache";
    private static final String FILE_SCHEME = "file://";

    private final Map<String, Entry> entries;
    private boolean loaded;
    private String signature;

    public FeaturedPosterCache() {
        this.entries = new HashMap<>();
        this.signature = "";
    }

    public synchronized boolean prepare(List<Vod> items) {
        ensureLoaded();
        String nextSignature = signatureOf(items);
        if (TextUtils.equals(signature, nextSignature)) return false;
        Path.clear(dir());
        entries.clear();
        signature = nextSignature;
        save();
        return true;
    }

    public synchronized String getSignature() {
        ensureLoaded();
        return signature;
    }

    public synchronized boolean isCurrent(String expectedSignature) {
        ensureLoaded();
        return TextUtils.equals(signature, expectedSignature);
    }

    @Nullable
    public synchronized String get(Vod item) {
        ensureLoaded();
        Entry entry = entries.get(keyOf(item));
        if (entry == null) return null;
        File file = file(entry.fileName);
        if (Path.exists(file)) return toLocalUrl(file);
        entries.remove(entry.key);
        save();
        return null;
    }

    public void put(String expectedSignature, Vod item, String imageUrl, Callback callback) {
        String key = keyOf(item);
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(imageUrl)) return;
        File target;
        synchronized (this) {
            ensureLoaded();
            if (!TextUtils.equals(signature, expectedSignature)) return;
            Entry entry = entries.get(key);
            if (entry != null && TextUtils.equals(entry.imageUrl, imageUrl) && Path.exists(file(entry.fileName))) {
                callback.success(toLocalUrl(file(entry.fileName)));
                return;
            }
            target = file(createFileName(key, imageUrl));
        }
        Task.execute(() -> download(expectedSignature, key, imageUrl, target, callback));
    }

    public static String keyOf(Vod item) {
        if (item == null) return "";
        return item.getName() + "\n" + item.getYear() + "\n" + item.getTypeName();
    }

    private void download(String expectedSignature, String key, String imageUrl, File target, Callback callback) {
        try (Response response = OkHttp.newCall(imageUrl, TAG + "_" + Util.md5(key)).execute()) {
            if (!response.isSuccessful() || response.body() == null) throw new IOException("TMDB poster download failed: HTTP " + response.code());
            Path.copy(response.body().byteStream(), target);
            if (!Path.exists(target)) throw new IOException("TMDB poster download failed: empty file");
            if (saveEntry(expectedSignature, key, imageUrl, target)) {
                App.post(() -> callback.success(toLocalUrl(target)));
            } else {
                Path.clear(target);
            }
        } catch (Exception e) {
            Path.clear(target);
            App.post(() -> callback.error(e));
        }
    }

    private synchronized boolean saveEntry(String expectedSignature, String key, String imageUrl, File target) {
        ensureLoaded();
        if (!TextUtils.equals(signature, expectedSignature)) return false;
        entries.put(key, new Entry(key, imageUrl, target.getName()));
        save();
        return true;
    }

    private synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Metadata metadata = read();
        signature = metadata.signature == null ? "" : metadata.signature;
        entries.clear();
        if (metadata.entries == null) return;
        for (Entry entry : metadata.entries) {
            if (entry == null || TextUtils.isEmpty(entry.key) || TextUtils.isEmpty(entry.fileName)) continue;
            entries.put(entry.key, entry);
        }
    }

    private Metadata read() {
        String text = Path.read(meta());
        if (TextUtils.isEmpty(text)) return new Metadata();
        try {
            Metadata metadata = App.gson().fromJson(text, Metadata.class);
            return metadata == null ? new Metadata() : metadata;
        } catch (Exception e) {
            return new Metadata();
        }
    }

    private void save() {
        Metadata metadata = new Metadata();
        metadata.signature = signature;
        metadata.entries = new ArrayList<>(entries.values());
        Path.write(meta(), App.gson().toJson(metadata).getBytes(StandardCharsets.UTF_8));
    }

    private File dir() {
        return Path.cache(DIR);
    }

    private File meta() {
        return new File(dir(), META);
    }

    private File file(String name) {
        return new File(dir(), name);
    }

    private String toLocalUrl(File file) {
        return FILE_SCHEME + file.getAbsolutePath();
    }

    private String createFileName(String key, String imageUrl) {
        return Util.md5(key + "\n" + imageUrl) + extensionOf(imageUrl);
    }

    private String extensionOf(String imageUrl) {
        String name = Uri.parse(imageUrl).getLastPathSegment();
        if (TextUtils.isEmpty(name)) return ".jpg";
        int index = name.lastIndexOf('.');
        if (index == -1) return ".jpg";
        String extension = name.substring(index).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp", ".avif" -> extension;
            default -> ".jpg";
        };
    }

    private String signatureOf(List<Vod> items) {
        List<String> keys = new ArrayList<>();
        if (items != null) for (Vod item : items) {
            String key = keyOf(item);
            if (!TextUtils.isEmpty(key)) keys.add(key);
        }
        Collections.sort(keys);
        return TextUtils.join("\n---\n", keys);
    }

    public interface Callback {

        void success(@NonNull String imageUrl);

        void error(@NonNull Exception error);
    }

    private static class Metadata {

        private String signature = "";
        private List<Entry> entries = new ArrayList<>();
    }

    private static class Entry {

        private String key;
        private String imageUrl;
        private String fileName;

        private Entry() {
        }

        private Entry(String key, String imageUrl, String fileName) {
            this.key = key;
            this.imageUrl = imageUrl;
            this.fileName = fileName;
        }
    }
}
