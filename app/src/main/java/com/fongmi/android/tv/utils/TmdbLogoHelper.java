package com.fongmi.android.tv.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;

public final class TmdbLogoHelper {

    private static final String API_BASE = "https://tapi.coolmarket.eu.org/3/";
    private static final String IMAGE_BASE = "https://tapi.coolmarket.eu.org/t/p/";
    private static final String DEFAULT_LOGO_SIZE = "w500";
    private static final String INCLUDE_IMAGE_LANGUAGE = "zh,en,null";
    private static final Pattern YEAR = Pattern.compile("(?:19|20)\\d{2}");

    private TmdbLogoHelper() {
    }

    public static void findLogo(@Nullable String apiKey, @Nullable String title, @Nullable String year, @Nullable String typeName, @NonNull LogoCallback callback) {
        findLogo(apiKey, title, year, typeName, DEFAULT_LOGO_SIZE, callback);
    }

    public static void findLogo(@Nullable String apiKey, @Nullable String title, @Nullable String year, @Nullable String typeName, @Nullable String imageSize, @NonNull LogoCallback callback) {
        String safeApiKey = normalize(apiKey);
        String safeTitle = normalize(title);
        String safeYear = normalizeYear(year);
        String safeImageSize = normalizeImageSize(imageSize);
        if (TextUtils.isEmpty(safeTitle)) {
            post(callback::onNotFound);
            return;
        }
        MediaType firstType = guessMediaType(typeName);
        MediaType secondType = firstType == MediaType.TV ? MediaType.MOVIE : MediaType.TV;
        search(safeApiKey, firstType, safeTitle, safeYear, new SearchCallback() {
            @Override
            public void onFound(@Nullable Integer id) {
                if (id != null) {
                    fetchLogo(safeApiKey, firstType, id, safeImageSize, callback);
                } else {
                    searchAlternative(safeApiKey, secondType, safeTitle, safeYear, safeImageSize, callback);
                }
            }

            @Override
            public void onError(@NonNull Exception error) {
                post(() -> callback.onError(error));
            }
        });
    }

    private static void searchAlternative(String apiKey, MediaType type, String title, String year, String imageSize, LogoCallback callback) {
        search(apiKey, type, title, year, new SearchCallback() {
            @Override
            public void onFound(@Nullable Integer id) {
                if (id != null) fetchLogo(apiKey, type, id, imageSize, callback);
                else post(callback::onNotFound);
            }

            @Override
            public void onError(@NonNull Exception error) {
                post(() -> callback.onError(error));
            }
        });
    }

    private static void search(String apiKey, MediaType type, String title, String year, SearchCallback callback) {
        HttpUrl url = buildSearchUrl(apiKey, type, title, year);
        OkHttp.newCall(url.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) throw new IOException("TMDB search failed: HTTP " + resp.code());
                    callback.onFound(parseFirstId(resp.body().string()));
                } catch (Exception e) {
                    post(() -> callback.onError(e));
                }
            }
        });
    }

    private static void fetchLogo(String apiKey, MediaType type, int id, String imageSize, LogoCallback callback) {
        HttpUrl url = buildImagesUrl(apiKey, type, id);
        OkHttp.newCall(url.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) throw new IOException("TMDB images failed: HTTP " + resp.code());
                    String filePath = selectBestLogo(new JSONObject(resp.body().string()).optJSONArray("logos"));
                    if (TextUtils.isEmpty(filePath)) post(callback::onNotFound);
                    else post(() -> callback.onFound(buildImageUrl(imageSize, filePath)));
                } catch (Exception e) {
                    post(() -> callback.onError(e));
                }
            }
        });
    }

    private static HttpUrl buildSearchUrl(String apiKey, MediaType type, String title, String year) {
        HttpUrl url = HttpUrl.parse(API_BASE + type.searchPath);
        if (url == null) throw new IllegalArgumentException("Invalid TMDB search URL");
        HttpUrl.Builder builder = url.newBuilder()
                .addQueryParameter("query", title)
                .addQueryParameter("language", "zh-CN")
                .addQueryParameter("include_adult", "false");
        if (!TextUtils.isEmpty(apiKey)) builder.addQueryParameter("api_key", apiKey);
        if (!TextUtils.isEmpty(year)) builder.addQueryParameter(type.yearParam, year);
        return builder.build();
    }

    private static HttpUrl buildImagesUrl(String apiKey, MediaType type, int id) {
        HttpUrl url = HttpUrl.parse(API_BASE + type.detailPath + "/" + id + "/images");
        if (url == null) throw new IllegalArgumentException("Invalid TMDB images URL");
        HttpUrl.Builder builder = url.newBuilder()
                .addQueryParameter("include_image_language", INCLUDE_IMAGE_LANGUAGE);
        if (!TextUtils.isEmpty(apiKey)) builder.addQueryParameter("api_key", apiKey);
        return builder.build();
    }

    @Nullable
    private static Integer parseFirstId(String body) throws Exception {
        JSONArray results = new JSONObject(body).optJSONArray("results");
        if (results == null) return null;
        for (int i = 0; i < results.length(); i++) {
            int id = results.optJSONObject(i) == null ? 0 : results.optJSONObject(i).optInt("id");
            if (id > 0) return id;
        }
        return null;
    }

    @Nullable
    private static String selectBestLogo(@Nullable JSONArray logos) {
        if (logos == null || logos.length() == 0) return null;
        String[] paths = new String[3];
        int[] widths = new int[]{0, 0, 0};
        for (int i = 0; i < logos.length(); i++) {
            JSONObject logo = logos.optJSONObject(i);
            if (logo == null) continue;
            String path = logo.optString("file_path");
            int width = logo.optInt("width");
            if (TextUtils.isEmpty(path) || "null".equalsIgnoreCase(path) || width <= 0) continue;
            int rank = getLanguageRank(logo.optString("iso_639_1"));
            if (width > widths[rank]) {
                paths[rank] = path;
                widths[rank] = width;
            }
        }
        for (String path : paths) if (!TextUtils.isEmpty(path)) return path;
        return null;
    }

    private static int getLanguageRank(String language) {
        if ("zh".equalsIgnoreCase(language)) return 0;
        if ("en".equalsIgnoreCase(language)) return 1;
        return 2;
    }

    private static String buildImageUrl(String imageSize, String filePath) {
        return IMAGE_BASE + imageSize + (filePath.startsWith("/") ? filePath : "/" + filePath);
    }

    private static MediaType guessMediaType(@Nullable String typeName) {
        String lower = normalize(typeName).toLowerCase(Locale.ROOT);
        if (lower.contains("剧") || lower.contains("电视") || lower.contains("动漫") || lower.contains("动画") || lower.contains("综艺") || lower.contains("纪录") || lower.contains("tv")) {
            return MediaType.TV;
        }
        return MediaType.MOVIE;
    }

    private static String normalize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeYear(@Nullable String year) {
        Matcher matcher = YEAR.matcher(normalize(year));
        return matcher.find() ? matcher.group() : "";
    }

    private static String normalizeImageSize(@Nullable String imageSize) {
        String value = normalize(imageSize);
        if (TextUtils.isEmpty(value)) return DEFAULT_LOGO_SIZE;
        while (value.startsWith("/")) value = value.substring(1);
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return TextUtils.isEmpty(value) ? DEFAULT_LOGO_SIZE : value;
    }

    private static void post(Runnable runnable) {
        if (App.get() == null) runnable.run();
        else App.post(runnable);
    }

    public interface LogoCallback {

        void onFound(@NonNull String logoUrl);

        void onNotFound();

        void onError(@NonNull Exception error);
    }

    private interface SearchCallback {

        void onFound(@Nullable Integer id);

        void onError(@NonNull Exception error);
    }

    private enum MediaType {
        MOVIE("search/movie", "movie", "primary_release_year"),
        TV("search/tv", "tv", "first_air_date_year");

        private final String searchPath;
        private final String detailPath;
        private final String yearParam;

        MediaType(String searchPath, String detailPath, String yearParam) {
            this.searchPath = searchPath;
            this.detailPath = detailPath;
            this.yearParam = yearParam;
        }
    }
}
