package com.fongmi.android.tv.utils; // Or your preferred package

import android.text.TextUtils;
import android.util.Log; // Import Log if you add logging
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant; // Make sure API Key is defined here
import com.github.catvod.net.OkHttp; // Use the project's OkHttp

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
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
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e("TmdbHelper", "Cannot search: Title is empty or TMDB API Key is not set.");
            callback.onLogoNotFound(); // Cannot search without title or valid API key
            return;
        }

        // Basic type detection (improve this if possible based on your typeName values)
        boolean isTv = typeName != null && (typeName.contains("剧") || typeName.contains("电视") || typeName.contains("动漫") || typeName.contains("动画") || typeName.contains("综艺"));
        String searchPath = isTv ? "search/tv" : "search/movie";
        String detailsPath = isTv ? "tv/" : "movie/";
        String alternativeSearchPath = isTv ? "search/movie" : "search/tv"; // Path for fallback search
        String alternativeDetailsPath = isTv ? "movie/" : "tv/"; // Details path for fallback

        // First attempt (movie or TV based on detection)
        searchTmdb(searchPath, title, year, (id) -> {
            if (id != null) {
                // Found ID, get images
                getTmdbDetails(detailsPath + id + "/images", callback);
            } else {
                // First attempt failed, try the alternative type
                Log.d("TmdbHelper", "First search failed for '" + title + "' as " + (isTv ? "TV" : "Movie") + ". Trying alternative.");
                searchTmdb(alternativeSearchPath, title, year, (altId) -> {
                    if (altId != null) {
                        // Found ID on alternative search, get images
                        getTmdbDetails(alternativeDetailsPath + altId + "/images", callback);
                    } else {
                        // Both attempts failed
                         Log.d("TmdbHelper", "Alternative search also failed for '" + title + "'.");
                        callback.onLogoNotFound();
                    }
                }, () -> {
                    // Error during alternative search
                    Log.e("TmdbHelper", "Error during alternative TMDB search for: " + title);
                    callback.onError(); // Report error from alternative search
                });
            }
        }, () -> {
            // Error during initial search - We might still try the alternative? Or just report error?
            // Let's report the error for simplicity here. Could be changed to try alternative anyway.
             Log.e("TmdbHelper", "Error during initial TMDB search for: " + title);
            callback.onError(); // Report error from initial search
        });
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

            // Pass URL String to OkHttp.newCall
            String url = urlBuilder.build().toString();
            Log.d("TmdbHelper", "Searching TMDB: " + url); // Log search URL

            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("TmdbHelper", "TMDB Search onFailure: " + e.getMessage());
                    App.post(errorCallback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e("TmdbHelper", "TMDB Search onResponse Error: Code=" + response.code());
                        App.post(errorCallback::onError);
                        response.close(); // Close response body on error
                        return;
                    }
                    try {
                        String json = response.body().string();
                        Log.d("TmdbHelper", "TMDB Search Response: " + json.substring(0, Math.min(json.length(), 500)) + "..."); // Log part of response
                        JSONObject result = new JSONObject(json);
                        JSONArray results = result.optJSONArray("results");
                        Integer foundId = null;
                        if (results != null && results.length() > 0) {
                            // Simple: Take the first result. Could add more matching logic here (e.g., check title similarity).
                            JSONObject firstMatch = results.getJSONObject(0);
                            foundId = firstMatch.optInt("id", -1); // Use optInt with default
                             if (foundId == -1) foundId = null; // Treat -1 as not found
                            Log.d("TmdbHelper", "TMDB Search Found ID: " + foundId + " for title: " + firstMatch.optString(searchPath.contains("tv") ? "name" : "title"));
                        } else {
                            Log.d("TmdbHelper", "TMDB Search: No results found.");
                        }
                        final Integer finalFoundId = foundId; // Final variable for lambda
                        App.post(() -> idCallback.onIdFound(finalFoundId));
                    } catch (Exception e) {
                        Log.e("TmdbHelper", "TMDB Search JSON Parsing Error: ", e);
                         App.post(errorCallback::onError);
                    } finally {
                         response.close(); // Ensure response body is closed
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TmdbHelper", "TMDB Search Exception: ", e);
            App.post(errorCallback::onError);
        }
    }

    private static void getTmdbDetails(String detailsPath, @NonNull LogoCallback callback) {
         try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + detailsPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            // Request specific languages for logos, e.g., English and Chinese
            urlBuilder.addQueryParameter("include_image_language", "en,zh,null"); // 'null' often includes originals

            // Pass URL String to OkHttp.newCall
            String url = urlBuilder.build().toString();
            Log.d("TmdbHelper", "Getting TMDB Details/Images: " + url); // Log details URL

            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     Log.e("TmdbHelper", "TMDB Details onFailure: " + e.getMessage());
                    App.post(callback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                     if (!response.isSuccessful() || response.body() == null) {
                        Log.e("TmdbHelper", "TMDB Details onResponse Error: Code=" + response.code());
                        App.post(callback::onError);
                         response.close(); // Close response body on error
                        return;
                    }
                    try {
                        String json = response.body().string();
                        Log.d("TmdbHelper", "TMDB Details Response: " + json.substring(0, Math.min(json.length(), 500)) + "..."); // Log part of response
                        JSONObject result = new JSONObject(json);
                        JSONArray logos = result.optJSONArray("logos");
                        String bestLogoPath = findBestLogoPath(logos); // Call the findBestLogoPath method

                        if (bestLogoPath != null) {
                            String fullLogoUrl = Constant.TMDB_IMG_BASE_URL + Constant.TMDB_LOGO_SIZE + bestLogoPath;
                            Log.d("TmdbHelper", "Found Logo URL: " + fullLogoUrl);
                            App.post(() -> callback.onLogoFound(fullLogoUrl));
                        } else {
                             Log.d("TmdbHelper", "No suitable logo found in details response.");
                            App.post(callback::onLogoNotFound);
                        }
                    } catch (Exception e) {
                        Log.e("TmdbHelper", "TMDB Details JSON Parsing Error: ", e);
                        App.post(callback::onError);
                    } finally {
                         response.close(); // Ensure response body is closed
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TmdbHelper", "TMDB Details Exception: ", e);
            App.post(callback::onError);
        }
    }

    // Function to select the best logo (e.g., prefer Chinese, then English, then first)
    private static String findBestLogoPath(@Nullable JSONArray logos) {
        if (logos == null || logos.length() == 0) {
            Log.d("TmdbHelper", "findBestLogoPath: logos array is null or empty.");
            return null;
        }

        String zhLogo = null; // Specifically look for Chinese
        String enLogo = null;
        String otherLogo = null;

        Log.d("TmdbHelper", "findBestLogoPath: Checking " + logos.length() + " logos.");
        for (int i = 0; i < logos.length(); i++) {
            JSONObject logoInfo = logos.optJSONObject(i);
            if (logoInfo != null) {
                String filePath = logoInfo.optString("file_path", null);
                if (filePath == null) continue;

                // Basic aspect ratio check to prefer wider logos (optional)
                double aspectRatio = logoInfo.optDouble("aspect_ratio", 1.0);
                // Adjust this threshold as needed. Logos are often wider than 1.0.
                if (aspectRatio < 1.0) { // Example: Skip logos significantly taller than wide
                     Log.d("TmdbHelper", "findBestLogoPath: Skipping logo due to aspect ratio < 1.0: " + filePath);
                     continue;
                }

                String lang = logoInfo.optString("iso_639_1", "null"); // Default to "null" if key not present

                 Log.d("TmdbHelper", "findBestLogoPath: Checking logo: path=" + filePath + ", lang=" + lang + ", aspect=" + aspectRatio);

                // Prioritize specific languages
                if ("zh".equalsIgnoreCase(lang)) {
                     Log.d("TmdbHelper", "findBestLogoPath: Found 'zh' logo: " + filePath);
                     if (zhLogo == null) zhLogo = filePath; // Store first Chinese logo found
                    // Optionally, return immediately if Chinese is the absolute priority: return zhLogo;
                } else if ("en".equalsIgnoreCase(lang)) {
                     if (enLogo == null) {
                         Log.d("TmdbHelper", "findBestLogoPath: Found 'en' logo: " + filePath);
                         enLogo = filePath; // Store first English logo
                     }
                } else {
                     // Store first logo with other language or null language (often original)
                     if (otherLogo == null) {
                          Log.d("TmdbHelper", "findBestLogoPath: Found 'other/null' logo: " + filePath);
                          otherLogo = filePath;
                     }
                }
            }
        }

        // Return in order of preference: Chinese, English, Other/Null
        if (zhLogo != null) {
             Log.d("TmdbHelper", "findBestLogoPath: Returning 'zh' logo: " + zhLogo);
             return zhLogo;
        }
        if (enLogo != null) {
            Log.d("TmdbHelper", "findBestLogoPath: Returning 'en' logo: " + enLogo);
            return enLogo;
        }
        Log.d("TmdbHelper", "findBestLogoPath: Returning 'other/null' logo: " + otherLogo);
        return otherLogo; // This might be null if only filtered-out logos were found
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
