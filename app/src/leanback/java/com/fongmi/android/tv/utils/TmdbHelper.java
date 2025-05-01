package com.fongmi.android.tv.utils; // Or your preferred package

// --- Add these imports ---
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
// --- End Add imports ---

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.github.catvod.net.OkHttp; // Use the project's OkHttp

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
// import okhttp3.Request; // We won't use Request object directly with newCall
import okhttp3.Response;
import okhttp3.HttpUrl;

public class TmdbHelper {

    public interface LogoCallback {
        void onLogoFound(@NonNull String logoUrl);
        void onLogoNotFound();
        void onError();
    }

    public static void findLogoForVod(String title, @Nullable String year, @Nullable String typeName, @NonNull LogoCallback callback) {
        // Check for TextUtils uses the added import
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(Constant.TMDB_API_KEY)) {
            callback.onLogoNotFound(); // Cannot search without title or API key
            return;
        }

        boolean isTv = typeName != null && (typeName.contains("剧") || typeName.contains("动漫") || typeName.contains("综艺"));
        String searchPath = isTv ? "search/tv" : "search/movie";
        String detailsPath = isTv ? "tv/" : "movie/";

        searchTmdb(searchPath, title, year, (id) -> {
            if (id != null) {
                getTmdbDetails(detailsPath + id + "/images", callback);
            } else {
                if (!isTv) {
                    // --- Fix: Add errorCallback to the nested call ---
                    searchTmdb("search/tv", title, year, (tvId) -> {
                         if (tvId != null) {
                             getTmdbDetails("tv/" + tvId + "/images", callback);
                         } else {
                             callback.onLogoNotFound();
                         }
                    }, callback::onError); // Pass the error callback here
                    // --- End Fix ---
                } else {
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
            urlBuilder.addQueryParameter("language", "zh-CN");
            urlBuilder.addQueryParameter("include_adult", "false");
            // Check for TextUtils uses the added import
            if (!TextUtils.isEmpty(year)) {
                 String yearParam = searchPath.contains("tv") ? "first_air_date_year" : "primary_release_year";
                 urlBuilder.addQueryParameter(yearParam, year);
            }

            // --- Fix: Pass URL String to OkHttp.newCall ---
            String url = urlBuilder.build().toString();
            OkHttp.newCall(url).enqueue(new Callback() {
            // --- End Fix ---
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    App.post(errorCallback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // ... (rest of onResponse remains the same) ...
                     if (!response.isSuccessful() || response.body() == null) {
                        App.post(errorCallback::onError);
                        return;
                    }
                    try {
                        String json = response.body().string();
                        JSONObject result = new JSONObject(json);
                        JSONArray results = result.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            JSONObject firstMatch = results.getJSONObject(0);
                            Integer id = firstMatch.optInt("id");
                             App.post(() -> idCallback.onIdFound(id));
                        } else {
                            App.post(() -> idCallback.onIdFound(null));
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
            urlBuilder.addQueryParameter("include_image_language", "en,zh,null");

            // --- Fix: Pass URL String to OkHttp.newCall ---
            String url = urlBuilder.build().toString();
            OkHttp.newCall(url).enqueue(new Callback() {
            // --- End Fix ---
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    App.post(callback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                     // ... (rest of onResponse remains the same) ...
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

    // findBestLogoPath remains the same

    // Internal interfaces remain the same
    @FunctionalInterface
    private interface IdCallback {
        void onIdFound(@Nullable Integer id);
    }
     @FunctionalInterface
    private interface ErrorCallback {
        void onError();
    }
}
