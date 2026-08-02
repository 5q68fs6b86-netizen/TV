package com.fongmi.android.tv.bean;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;

public final class DiscoverMediaKey {

    public static final String MOVIE = "movie";
    public static final String TV = "tv";

    private final String mediaType;
    private final long id;

    private DiscoverMediaKey(String mediaType, long id) {
        this.mediaType = mediaType;
        this.id = id;
    }

    public static DiscoverMediaKey of(String mediaType, long id) {
        String normalized = normalize(mediaType);
        if (normalized.isEmpty() || id <= 0) throw new IllegalArgumentException("Invalid TMDB media key");
        return new DiscoverMediaKey(normalized, id);
    }

    @Nullable
    public static DiscoverMediaKey parse(@Nullable String value) {
        if (value == null) return null;
        String[] parts = value.trim().split(":", -1);
        if (parts.length != 3 || !"tmdb".equals(parts[0])) return null;
        try {
            return of(parts[1], Long.parseLong(parts[2]));
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean isValid(@Nullable String value) {
        return parse(value) != null;
    }

    public String getMediaType() {
        return mediaType;
    }

    public long getId() {
        return id;
    }

    public boolean isMovie() {
        return MOVIE.equals(mediaType);
    }

    private static String normalize(String mediaType) {
        if (mediaType == null) return "";
        String value = mediaType.trim().toLowerCase(Locale.US);
        return MOVIE.equals(value) || TV.equals(value) ? value : "";
    }

    @Override
    public String toString() {
        return "tmdb:" + mediaType + ":" + id;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof DiscoverMediaKey other)) return false;
        return id == other.id && mediaType.equals(other.mediaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaType, id);
    }
}
