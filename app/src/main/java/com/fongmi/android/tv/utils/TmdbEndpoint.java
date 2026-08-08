package com.fongmi.android.tv.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.setting.Setting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class TmdbEndpoint {

    public static final String DEFAULT_ROOT = "https://tapi.coolmarket.eu.org/";
    public static final String DEFAULT_API_BASE = DEFAULT_ROOT + "3/";
    public static final String DEFAULT_IMAGE_BASE = DEFAULT_ROOT + "t/p/";

    private TmdbEndpoint() {
    }

    @NonNull
    public static String getApiBase() {
        return getEffectiveRoot() + "3/";
    }

    @NonNull
    public static String getImageBase() {
        return getEffectiveRoot() + "t/p/";
    }

    @NonNull
    public static String getEffectiveRoot() {
        String root = getCustomRoot();
        return root.isEmpty() ? DEFAULT_ROOT : root;
    }

    @NonNull
    public static String getCustomRoot() {
        return normalizeRoot(Setting.getTmdbProxyUrl());
    }

    public static boolean isCustom() {
        return !getCustomRoot().isEmpty();
    }

    @NonNull
    public static String normalizeRoot(@Nullable String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) return "";
        String candidate = raw.contains("://") ? raw : "https://" + raw;
        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String authority = uri.getRawAuthority() == null ? "" : uri.getRawAuthority();
            if ((!"http".equals(scheme) && !"https".equals(scheme)) || authority.isEmpty()) return "";
            return scheme + "://" + authority + normalizePath(uri.getRawPath());
        } catch (URISyntaxException e) {
            return "";
        }
    }

    @NonNull
    private static String normalizePath(@Nullable String path) {
        String value = path == null ? "" : path.trim();
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        if (value.endsWith("/3")) value = value.substring(0, value.length() - 2);
        else if (value.endsWith("/t/p")) value = value.substring(0, value.length() - 4);
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        if (value.isEmpty()) return "/";
        return (value.startsWith("/") ? value : "/" + value) + "/";
    }
}
