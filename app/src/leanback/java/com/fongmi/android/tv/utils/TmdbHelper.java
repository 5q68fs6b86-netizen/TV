package com.fongmi.android.tv.utils; // Or your preferred package

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.HttpUrl;

public class TmdbHelper {

    // LogoCallback interface remains the same...
    public interface LogoCallback {
        void onLogoFound(@NonNull String logoUrl);
        void onLogoNotFound();
        void onError();
    }

    // findLogoForVod method remains the same...
    public static void findLogoForVod(String title, @Nullable String year, @Nullable String typeName, @NonNull LogoCallback callback) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e("TmdbHelper", "Cannot search: Title is empty or TMDB API Key is not set.");
            callback.onLogoNotFound();
            return;
        }
        boolean isTv = typeName != null && (typeName.contains("剧") || typeName.contains("电视") || typeName.contains("动漫") || typeName.contains("动画") || typeName.contains("综艺"));
        String searchPath = isTv ? "search/tv" : "search/movie";
        String detailsPath = isTv ? "tv/" : "movie/";
        String alternativeSearchPath = isTv ? "search/movie" : "search/tv";
        String alternativeDetailsPath = isTv ? "movie/" : "tv/";

        searchTmdb(searchPath, title, year, (id) -> {
            if (id != null) {
                getTmdbDetails(detailsPath + id + "/images", callback);
            } else {
                Log.d("TmdbHelper", "First search failed for '" + title + "' as " + (isTv ? "TV" : "Movie") + ". Trying alternative.");
                searchTmdb(alternativeSearchPath, title, year, (altId) -> {
                    if (altId != null) {
                        getTmdbDetails(alternativeDetailsPath + altId + "/images", callback);
                    } else {
                         Log.d("TmdbHelper", "Alternative search also failed for '" + title + "'.");
                        callback.onLogoNotFound();
                    }
                }, () -> {
                    Log.e("TmdbHelper", "Error during alternative TMDB search for: " + title);
                    callback.onError();
                });
            }
        }, () -> {
             Log.e("TmdbHelper", "Error during initial TMDB search for: " + title);
            callback.onError();
        });
    }

    // searchTmdb method remains the same...
    private static void searchTmdb(String searchPath, String title, @Nullable String year, @NonNull IdCallback idCallback, @NonNull ErrorCallback errorCallback) {
       try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + searchPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            urlBuilder.addQueryParameter("query", title);
            urlBuilder.addQueryParameter("language", "zh-CN");
            urlBuilder.addQueryParameter("include_adult", "false");
            if (!TextUtils.isEmpty(year)) {
                 String yearParam = searchPath.contains("tv") ? "first_air_date_year" : "primary_release_year";
                 urlBuilder.addQueryParameter(yearParam, year);
            }
            String url = urlBuilder.build().toString();
            Log.d("TmdbHelper", "Searching TMDB: " + url);

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
                        response.close();
                        return;
                    }
                    try {
                        String json = response.body().string();
                        JSONObject result = new JSONObject(json);
                        JSONArray results = result.optJSONArray("results");
                        Integer foundId = null;
                        if (results != null && results.length() > 0) {
                            JSONObject firstMatch = results.getJSONObject(0);
                            foundId = firstMatch.optInt("id", -1);
                             if (foundId == -1) foundId = null;
                            Log.d("TmdbHelper", "TMDB Search Found ID: " + foundId + " for title: " + firstMatch.optString(searchPath.contains("tv") ? "name" : "title"));
                        } else {
                            Log.d("TmdbHelper", "TMDB Search: No results found.");
                        }
                        final Integer finalFoundId = foundId;
                        App.post(() -> idCallback.onIdFound(finalFoundId));
                    } catch (Exception e) {
                        Log.e("TmdbHelper", "TMDB Search JSON Parsing Error: ", e);
                         App.post(errorCallback::onError);
                    } finally {
                         response.close();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TmdbHelper", "TMDB Search Exception: ", e);
            App.post(errorCallback::onError);
        }
    }

    // getTmdbDetails method now calls findBestLogoByPriority
     private static void getTmdbDetails(String detailsPath, @NonNull LogoCallback callback) {
         try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + detailsPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            urlBuilder.addQueryParameter("include_image_language", "en,zh,null"); // Request all relevant languages
            String url = urlBuilder.build().toString();
            Log.d("TmdbHelper", "Getting TMDB Details/Images: " + url);

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
                         response.close();
                        return;
                    }
                    try {
                        String json = response.body().string();
                        JSONObject result = new JSONObject(json);
                        JSONArray logos = result.optJSONArray("logos");
                        // --- MODIFIED CALL ---
                        String bestLogoPath = findBestLogoByPriority(logos); // Use the new priority method
                        // --- END MODIFIED CALL ---

                        if (bestLogoPath != null) {
                            String logoSize = Constant.TMDB_LOGO_SIZE; // e.g., "original" or "w780"
                            String fullLogoUrl = Constant.TMDB_IMG_BASE_URL + logoSize + bestLogoPath;
                            Log.i("TmdbHelper", "Selected Logo URL (" + logoSize + "): " + fullLogoUrl); // Use Info level for final selection
                            App.post(() -> callback.onLogoFound(fullLogoUrl));
                        } else {
                             Log.w("TmdbHelper", "No suitable logo found after checking priorities."); // Use Warning level
                            App.post(callback::onLogoNotFound);
                        }
                    } catch (Exception e) {
                        Log.e("TmdbHelper", "TMDB Details JSON Parsing Error: ", e);
                        App.post(callback::onError);
                    } finally {
                         response.close();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TmdbHelper", "TMDB Details Exception: ", e);
            App.post(callback::onError);
        }
    }


    // --- NEW METHOD: findBestLogoByPriority (4-level priority) ---
    /**
     * Finds the best logo according to the specified priority:
     * 1. Widest Chinese (zh) Horizontal Logo.
     * 2. Widest Chinese (zh) Vertical Logo.
     * 3. Most Horizontal (highest aspect ratio) Logo of any language.
     * 4. Widest Logo of any language (regardless of orientation).
     * Returns null if no logos are found.
     */
    private static String findBestLogoByPriority(@Nullable JSONArray logos) {
        if (logos == null || logos.length() == 0) {
            Log.d("TmdbHelper", "findBestLogoByPriority: logos array is null or empty.");
            return null;
        }

        // Variables to track the best candidates for each priority level
        String p1_bestZhHorizontalWidestPath = null;
        int p1_maxZhHorizontalWidth = -1;

        String p2_bestZhVerticalWidestPath = null;
        int p2_maxZhVerticalWidth = -1;

        String p3_bestAnyHorizontalPath = null;
        double p3_maxAnyHorizontalAspectRatio = -1.0;

        String p4_bestAnyWidestPath = null;
        int p4_maxAnyWidth = -1;

        // Define the threshold for considering a logo "horizontal"
        // Using >= 1.0 means square or wider is horizontal, < 1.0 is vertical
        final double HORIZONTAL_THRESHOLD = 1.0;

        Log.d("TmdbHelper", "findBestLogoByPriority: Checking " + logos.length() + " logos. Horizontal threshold >= " + HORIZONTAL_THRESHOLD);

        for (int i = 0; i < logos.length(); i++) {
            JSONObject logoInfo = logos.optJSONObject(i);
            if (logoInfo == null) continue;

            String filePath = logoInfo.optString("file_path", null);
            if (filePath == null) continue;

            String lang = logoInfo.optString("iso_639_1", "null");
            double aspectRatio = logoInfo.optDouble("aspect_ratio", 0.0);
            int width = logoInfo.optInt("width", -1);

            // Skip if essential info is missing
            if (width <= 0 || aspectRatio <= 0) { // Also check aspect ratio validity
                 Log.d("TmdbHelper", "findBestLogoByPriority: Skipping logo (invalid width/aspect): " + filePath);
                 continue;
            }

             Log.d("TmdbHelper", "findBestLogoByPriority: Checking logo: path=" + filePath + ", lang=" + lang + ", width=" + width + ", aspect=" + aspectRatio);

            boolean isHorizontal = aspectRatio >= HORIZONTAL_THRESHOLD;
            boolean isChinese = "zh".equalsIgnoreCase(lang);

            // --- Evaluate for Priority 1: Widest Chinese Horizontal ---
            if (isChinese && isHorizontal) {
                if (width > p1_maxZhHorizontalWidth) {
                     Log.v("TmdbHelper", "findBestLogoByPriority: Found new P1 candidate: " + filePath + " (width: " + width + ")"); // Verbose log
                    p1_maxZhHorizontalWidth = width;
                    p1_bestZhHorizontalWidestPath = filePath;
                }
            }

            // --- Evaluate for Priority 2: Widest Chinese Vertical ---
            if (isChinese && !isHorizontal) { // Note: !isHorizontal means vertical
                if (width > p2_maxZhVerticalWidth) {
                     Log.v("TmdbHelper", "findBestLogoByPriority: Found new P2 candidate: " + filePath + " (width: " + width + ")");
                    p2_maxZhVerticalWidth = width;
                    p2_bestZhVerticalWidestPath = filePath;
                }
            }

            // --- Evaluate for Priority 3: Most Horizontal (Any Language) ---
            if (isHorizontal) {
                if (aspectRatio > p3_maxAnyHorizontalAspectRatio) {
                     Log.v("TmdbHelper", "findBestLogoByPriority: Found new P3 candidate: " + filePath + " (aspect: " + aspectRatio + ")");
                    p3_maxAnyHorizontalAspectRatio = aspectRatio;
                    p3_bestAnyHorizontalPath = filePath;
                }
                // Tie-breaker: same aspect ratio, prefer wider (also helps P4)
                else if (aspectRatio == p3_maxAnyHorizontalAspectRatio && width > p4_maxAnyWidth) {
                    Log.v("TmdbHelper", "findBestLogoByPriority: Found P3 aspect tie, choosing wider: " + filePath + " (width: " + width + ")");
                    p3_bestAnyHorizontalPath = filePath; // Update P3 candidate
                    // This wider one is also the best P4 candidate found so far
                    p4_maxAnyWidth = width;
                    p4_bestAnyWidestPath = filePath;
                }
            }

            // --- Evaluate for Priority 4: Widest (Any Language, Any Orientation) ---
            if (width > p4_maxAnyWidth) {
                 Log.v("TmdbHelper", "findBestLogoByPriority: Found new P4 candidate: " + filePath + " (width: " + width + ")");
                p4_maxAnyWidth = width;
                p4_bestAnyWidestPath = filePath;
                 // If this widest one is also the *most* horizontal found so far, update P3 as well.
                 if (isHorizontal && aspectRatio > p3_maxAnyHorizontalAspectRatio) {
                     p3_maxAnyHorizontalAspectRatio = aspectRatio;
                     p3_bestAnyHorizontalPath = filePath;
                 }
            }
        }

        // --- Return based on priority ---
        if (p1_bestZhHorizontalWidestPath != null) {
            Log.i("TmdbHelper", "findBestLogoByPriority: Returning P1 (Widest Zh Horizontal): " + p1_bestZhHorizontalWidestPath);
            return p1_bestZhHorizontalWidestPath;
        } else if (p2_bestZhVerticalWidestPath != null) {
            Log.i("TmdbHelper", "findBestLogoByPriority: Returning P2 (Widest Zh Vertical): " + p2_bestZhVerticalWidestPath);
            return p2_bestZhVerticalWidestPath;
        } else if (p3_bestAnyHorizontalPath != null) {
            Log.i("TmdbHelper", "findBestLogoByPriority: Returning P3 (Most Horizontal): " + p3_bestAnyHorizontalPath);
            return p3_bestAnyHorizontalPath;
        } else if (p4_bestAnyWidestPath != null) {
            Log.i("TmdbHelper", "findBestLogoByPriority: Returning P4 (Widest Overall): " + p4_bestAnyWidestPath);
            return p4_bestAnyWidestPath;
        } else {
            Log.w("TmdbHelper", "findBestLogoByPriority: No suitable logo found based on priorities.");
            return null; // No logo met any criteria
        }
    }
    // --- END NEW METHOD ---


    // --- REMOVE OR COMMENT OUT PREVIOUS findBest... METHODS ---
    /*
    private static String findBestHorizontalLogo(@Nullable JSONArray logos) { ... }
    private static String findBestChineseLogoByWidth(@Nullable JSONArray logos) { ... }
    */
    // --- END REMOVAL ---


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