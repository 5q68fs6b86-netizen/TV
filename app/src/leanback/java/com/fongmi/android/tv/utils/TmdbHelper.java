package com.fongmi.android.tv.utils; // Or your preferred package

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.github.catvod.net.OkHttp; // Use OkHttp

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

public class TmdbHelper {

    public interface LogoCallback {
        void onLogoFound(@NonNull String logoUrl);
        void onLogoNotFound();
        void onError();
    }

    public static void findLogoForVod(String title, @Nullable String year, @Nullable String typeName, @NonNull LogoCallback callback) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(Constant.TMDB_API_KEY)) {
            callback.onLogoNotFound(); // Cannot search without title or API key
            return;
        }

        // Basic type detection (improve this if possible based on your typeName values)
        boolean isTv = typeName != null && (typeName.contains("剧") || typeName.contains("动漫") || typeName.contains("综艺"));
        String searchPath = isTv ? "search/tv" : "search/movie";
        String detailsPath = isTv ? "tv/" : "movie/";

        searchTmdb(searchPath, title, year, (id) -> {
            if (id != null) {
                getTmdbDetails(detailsPath + id + "/images", callback);
            } else {
                // Optional: Try searching the other type if the first failed
                if (!isTv) { // If movie search failed, try TV
                    searchTmdb("search/tv", title, year, (tvId) -> {
                         if (tvId != null) {
                             getTmdbDetails("tv/" + tvId + "/images", callback);
                         } else {
                             callback.onLogoNotFound();
                         }
                    });
                } else { // If TV search failed initially
                     callback.onLogoNotFound();
                }
            }
        }, callback::onError);
    }

    private static void searchTmdb(String searchPath, String title, @Nullable String year, @NonNull IdCallback idCallback, @NonNull ErrorCallback errorCallback) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + searchPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            urlBuilder.addQueryParameter("query", title);
            urlBuilder.addQueryParameter("language", "zh-CN"); // Prioritize Chinese results
            urlBuilder.addQueryParameter("include_adult", "false");
            if (!TextUtils.isEmpty(year)) {
                 // TMDB uses first_air_date_year for TV, primary_release_year for movies
                 String yearParam = searchPath.contains("tv") ? "first_air_date_year" : "primary_release_year";
                 urlBuilder.addQueryParameter(yearParam, year);
            }

            Request request = new Request.Builder().url(urlBuilder.build()).build();

            OkHttp.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    App.post(errorCallback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        App.post(errorCallback::onError);
                        return;
                    }
                    try {
                        String json = response.body().string();
                        JSONObject result = new JSONObject(json);
                        JSONArray results = result.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            // Simple: Take the first result. Could add more matching logic here.
                            JSONObject firstMatch = results.getJSONObject(0);
                            Integer id = firstMatch.optInt("id");
                             App.post(() -> idCallback.onIdFound(id));
                        } else {
                            App.post(() -> idCallback.onIdFound(null)); // No results found
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                         App.post(errorCallback::onError);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            App.post(errorCallback::onError);
        }
    }

    private static void getTmdbDetails(String detailsPath, @NonNull LogoCallback callback) {
         try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + detailsPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            // Request specific languages for logos, e.g., English and Chinese
            urlBuilder.addQueryParameter("include_image_language", "en,zh,null"); // 'null' often includes originals

            Request request = new Request.Builder().url(urlBuilder.build()).build();

            OkHttp.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    App.post(callback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                     if (!response.isSuccessful() || response.body() == null) {
                        App.post(callback::onError);
                        return;
                    }
                    try {
                        String json = response.body().string();
                        JSONObject result = new JSONObject(json);
                        JSONArray logos = result.optJSONArray("logos");
                        String bestLogoPath = findBestLogoPath(logos);

                        if (bestLogoPath != null) {
                            String fullLogoUrl = Constant.TMDB_IMG_BASE_URL + Constant.TMDB_LOGO_SIZE + bestLogoPath;
                            App.post(() -> callback.onLogoFound(fullLogoUrl));
                        } else {
                            App.post(callback::onLogoNotFound);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        App.post(callback::onError);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            App.post(callback::onError);
        }
    }

    // Function to select the best logo (e.g., prefer Chinese, then English, then first)
    private static String findBestLogoPath(@Nullable JSONArray logos) {
        if (logos == null || logos.length() == 0) {
            return null;
        }

        String enLogo = null;
        String otherLogo = null;

        for (int i = 0; i < logos.length(); i++) {
            JSONObject logoInfo = logos.optJSONObject(i);
            if (logoInfo != null) {
                String filePath = logoInfo.optString("file_path", null);
                if (filePath == null) continue;

                // Basic aspect ratio check to prefer wider logos (optional)
                double aspectRatio = logoInfo.optDouble("aspect_ratio", 1.0);
                if (aspectRatio < 1.0) continue; // Skip portrait logos if desired

                String lang = logoInfo.optString("iso_639_1", "");
                if ("zh".equalsIgnoreCase(lang)) {
                    return filePath; // Found Chinese logo, use it immediately
                } else if ("en".equalsIgnoreCase(lang)) {
                    if (enLogo == null) enLogo = filePath; // Store first English logo
                } else {
                     if (otherLogo == null) otherLogo = filePath; // Store first other/null language logo
                }
            }
        }

        // Return in order of preference: English, Other/Null
        if (enLogo != null) return enLogo;
        return otherLogo; // Might be null if only portrait logos were found
    }


    // Internal interfaces for callbacks
    @FunctionalInterface
    private interface IdCallback {
        void onIdFound(@Nullable Integer id);
    }
     @FunctionalInterface
    private interface ErrorCallback {
        void onError();
    }
}