package com.fongmi.android.tv.player.media;

import java.util.Locale;

public final class MediaUrlGuard {

    private static final String[] IMAGE_EXTENSIONS = {
            ".jpg",
            ".jpeg",
            ".png",
            ".webp",
            ".gif",
            ".bmp",
            ".avif",
            ".heic",
            ".image"
    };

    private MediaUrlGuard() {
    }

    public static boolean shouldReject(String url, String format) {
        return isImageFormat(format) || isImageUrl(url);
    }

    private static boolean isImageFormat(String format) {
        if (format == null) return false;
        String value = format.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("image/");
    }

    private static boolean isImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String value = url.toLowerCase(Locale.ROOT);
        String path = stripQueryAndFragment(value);
        if (path.contains("~tplv-") && path.contains("-image.image")) return true;
        for (String extension : IMAGE_EXTENSIONS) if (path.endsWith(extension)) return true;
        return false;
    }

    private static String stripQueryAndFragment(String url) {
        int query = url.indexOf('?');
        int fragment = url.indexOf('#');
        int end = url.length();
        if (query >= 0) end = Math.min(end, query);
        if (fragment >= 0) end = Math.min(end, fragment);
        return url.substring(0, end);
    }
}
