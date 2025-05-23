package com.fongmi.android.tv.utils; // 或者替换为你项目实际的包名

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// 假设你的项目中有 App 类用于主线程操作，Constant 类存储常量
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
// 假设你使用了 CatVod 的 OkHttp 包装类，如果直接用 OkHttp3，需要修改这里的导入和调用
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
// import java.net.URLEncoder; // 在这个版本中未使用

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.HttpUrl;

/**
 * 使用 The Movie Database (TMDb) API 查找影视作品 Logo 的帮助类。
 *
 * 主要功能:
 * 1. 根据标题、年份、类型搜索 TMDb 获取影视 ID。
 * 2. 获取指定 ID 的图片信息 (主要是 Logo)。
 * 3. 根据优先级选择最佳 Logo (中文 > 英文 > 其他，同语言选最宽)。
 */
public class TmdbHelper {

    private static final String TAG = "TmdbHelper"; // 日志标签

    /**
     * Logo 查找结果的回调接口。
     */
    public interface LogoCallback {
        /**
         * 当成功找到 Logo 时调用。
         * @param logoUrl 完整的 Logo 图片 URL。
         */
        void onLogoFound(@NonNull String logoUrl);

        /**
         * 当搜索完成但未找到合适的 Logo 时调用。
         */
        void onLogoNotFound();

        /**
         * 当在查找过程中发生错误时调用。
         */
        void onError();
    }

    /**
     * 公开的入口方法，用于查找指定影视作品的 Logo。
     *
     * @param title    影视作品标题 (必需)。
     * @param year     年份 (可选)。
     * @param typeName 类型名称 (可选, 用于辅助判断是电影还是电视剧)。
     * @param callback 结果回调接口 (必需)。
     */
    public static void findLogoForVod(String title, @Nullable String year, @Nullable String typeName, @NonNull LogoCallback callback) {
        // 基础验证
        if (TextUtils.isEmpty(title)) {
             Log.e(TAG, "无法搜索: 标题为空。");
             callback.onLogoNotFound(); // 或者 onError()，取决于你的错误处理策略
             return;
        }
         // 检查 API Key 是否配置 (假设 Constant.TMDB_API_KEY 存在)
        if (TextUtils.isEmpty(Constant.TMDB_API_KEY) || "YOUR_TMDB_API_KEY_HERE".equals(Constant.TMDB_API_KEY)) {
            Log.e(TAG, "无法搜索: TMDB API Key 未设置或无效。请在 Constant.java 中配置。");
            // 通常不直接暴露 Key 未设置给用户，可能只记录日志或调用 onError
            callback.onError(); // 或者 onLogoNotFound()
            return;
        }

        // 猜测是电视剧还是电影
        // 这个判断逻辑可能需要根据实际情况调整
        boolean isTv = typeName != null && (
                typeName.contains("剧") ||
                typeName.contains("电视") ||
                typeName.contains("动漫") ||
                typeName.contains("动画") ||
                typeName.contains("综艺") ||
                typeName.contains("纪录") // 可能也属于 TV
        );

        String searchPath = isTv ? "search/tv" : "search/movie";
        String detailsPathPrefix = isTv ? "tv/" : "movie/";
        String alternativeSearchPath = isTv ? "search/movie" : "search/tv";
        String alternativeDetailsPathPrefix = isTv ? "movie/" : "tv/";

        Log.d(TAG, "开始为 '" + title + "' (猜测类型: " + (isTv ? "电视剧" : "电影") + ") 查找 Logo...");

        // 第一次尝试搜索
        searchTmdb(searchPath, title, year, (id) -> {
            // 主线程回调
            if (id != null) {
                Log.d(TAG, "首次搜索成功，找到 ID: " + id + "，获取图片详情...");
                getTmdbDetails(detailsPathPrefix + id + "/images", callback);
            } else {
                // 第一次搜索失败，尝试备选类型
                Log.d(TAG, "首次搜索 '" + title + "' 作为 " + (isTv ? "电视剧" : "电影") + " 未找到 ID. 尝试备选类型: " + (isTv ? "电影" : "电视剧"));
                searchTmdb(alternativeSearchPath, title, year, (altId) -> {
                    // 主线程回调
                    if (altId != null) {
                        Log.d(TAG, "备选搜索成功，找到 ID: " + altId + "，获取图片详情...");
                        getTmdbDetails(alternativeDetailsPathPrefix + altId + "/images", callback);
                    } else {
                        // 两次搜索都失败
                        Log.w(TAG, "备选类型搜索 '" + title + "' 也未找到 ID.");
                        callback.onLogoNotFound();
                    }
                }, () -> {
                    // 备选搜索出错的回调
                    Log.e(TAG, "备选 TMDB 搜索出错: " + title);
                    callback.onError();
                });
            }
        }, () -> {
            // 初始搜索出错的回调
             Log.e(TAG, "初始 TMDB 搜索出错: " + title);
            callback.onError();
        });
    }

    /**
     * 执行 TMDb 搜索请求。
     *
     * @param searchPath   API 路径 ("search/tv" 或 "search/movie")。
     * @param title        搜索标题。
     * @param year         年份 (可选)。
     * @param idCallback   找到 ID 时的回调。
     * @param errorCallback 发生错误时的回调。
     */
    private static void searchTmdb(String searchPath, String title, @Nullable String year, @NonNull IdCallback idCallback, @NonNull ErrorCallback errorCallback) {
       try {
            // 构建 URL
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + searchPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            urlBuilder.addQueryParameter("query", title);
            urlBuilder.addQueryParameter("language", "zh-CN"); // 优先使用中文搜索结果匹配标题
            urlBuilder.addQueryParameter("include_adult", "false");

            // 添加年份参数 (根据是 tv 还是 movie 使用不同参数名)
            if (!TextUtils.isEmpty(year)) {
                 String yearParam = searchPath.contains("tv") ? "first_air_date_year" : "primary_release_year";
                 urlBuilder.addQueryParameter(yearParam, year);
                 Log.d(TAG, "搜索时添加年份参数: " + yearParam + "=" + year);
            }

            String url = urlBuilder.build().toString();
            Log.d(TAG, "搜索 TMDB URL: " + url);

            // 发起异步请求 (使用 CatVod 的 OkHttp 包装类)
            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "TMDB 搜索网络请求失败: " + e.getMessage(), e);
                    // 确保回调在主线程执行
                    App.post(errorCallback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // 确保 response body 被关闭
                    try (Response resp = response) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Log.e(TAG, "TMDB 搜索响应错误: Code=" + resp.code() + ", Message=" + resp.message());
                            App.post(errorCallback::onError);
                            return;
                        }

                        String json = resp.body().string();
                        // Log.v(TAG, "TMDB 搜索响应 JSON: " + json); // Verbose log for debugging

                        try {
                            JSONObject result = new JSONObject(json);
                            JSONArray results = result.optJSONArray("results");
                            Integer foundId = null;

                            if (results != null && results.length() > 0) {
                                // 只取第一个结果作为最佳匹配
                                JSONObject firstMatch = results.getJSONObject(0);
                                foundId = firstMatch.optInt("id", -1);
                                if (foundId == -1) foundId = null; // 如果 id 是 -1 或不存在，设为 null

                                // 记录找到的标题，方便核对是否匹配
                                String foundTitle = firstMatch.optString(searchPath.contains("tv") ? "name" : "title");
                                Log.d(TAG, "TMDB 搜索找到最佳匹配 ID: " + foundId + " (标题: '" + foundTitle + "')");
                            } else {
                                Log.d(TAG, "TMDB 搜索: API 返回结果为空或 results 数组为空。");
                            }

                            // 将结果通过回调发送回主线程
                            final Integer finalFoundId = foundId;
                            App.post(() -> idCallback.onIdFound(finalFoundId));

                        } catch (Exception e) { // 更广泛地捕获 JSON 解析等异常
                            Log.e(TAG, "TMDB 搜索 JSON 解析或处理错误: ", e);
                            App.post(errorCallback::onError);
                        }
                    } catch (IOException e) {
                        // 处理 body().string() 可能抛出的 IO 异常
                        Log.e(TAG, "读取 TMDB 搜索响应体时出错: ", e);
                        App.post(errorCallback::onError);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "构建 TMDB 搜索 URL 时出错 (可能是 Base URL 配置问题): ", e);
            App.post(errorCallback::onError);
        } catch (Exception e) {
            // 捕获其他潜在的同步异常
             Log.e(TAG, "TMDB 搜索请求准备阶段发生异常: ", e);
             App.post(errorCallback::onError);
        }
    }

    /**
     * 获取指定 TMDb ID 的图片详情 (主要关注 Logo)。
     *
     * @param detailsPath API 路径，包含 ID 和 "/images" (例如 "tv/123/images")。
     * @param callback    结果回调接口。
     */
     private static void getTmdbDetails(String detailsPath, @NonNull LogoCallback callback) {
         try {
            // 构建 URL
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.TMDB_API_BASE_URL + detailsPath).newBuilder();
            urlBuilder.addQueryParameter("api_key", Constant.TMDB_API_KEY);
            // 请求包含中文、英文和无语言标签的图片，确保优先级所需语言都被请求
            urlBuilder.addQueryParameter("include_image_language", "zh,en,null");
            // 可以考虑添加其他语言，如果需要的话，例如 "ja" (日语)
            // urlBuilder.addQueryParameter("append_to_response", "images"); // 另一种获取图片的方式，但直接 /images 通常足够

            String url = urlBuilder.build().toString();
            Log.d(TAG, "获取 TMDB 图片详情 URL: " + url);

            // 发起异步请求
            OkHttp.newCall(url).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     Log.e(TAG, "TMDB 图片详情网络请求失败: " + e.getMessage(), e);
                    App.post(callback::onError);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                     try (Response resp = response) {
                         if (!resp.isSuccessful() || resp.body() == null) {
                            Log.e(TAG, "TMDB 图片详情响应错误: Code=" + resp.code() + ", Message=" + resp.message());
                            App.post(callback::onError);
                             return;
                        }

                        String json = resp.body().string();
                        // Log.v(TAG, "TMDB 图片详情响应 JSON: " + json); // Verbose log for debugging

                        try {
                            JSONObject result = new JSONObject(json);
                            JSONArray logos = result.optJSONArray("logos"); // 获取 logo 数组

                            // --- 调用 Logo 选择逻辑 ---
                            String bestLogoPath = findBestLogoByPriority(logos);
                            // --- 结束调用 ---

                            if (bestLogoPath != null) {
                                // 获取配置的 Logo 尺寸 (假设 Constant.TMDB_LOGO_SIZE 定义了，例如 "original", "w500")
                                String logoSize = Constant.TMDB_LOGO_SIZE;
                                // 拼接完整 URL (假设 Constant.TMDB_IMG_BASE_URL 定义了，例如 "https://image.tmdb.org/t/p/")
                                String fullLogoUrl = Constant.TMDB_IMG_BASE_URL + logoSize + bestLogoPath;

                                Log.i(TAG, "选中的最佳 Logo URL (尺寸: " + logoSize + "): " + fullLogoUrl);
                                App.post(() -> callback.onLogoFound(fullLogoUrl));
                            } else {
                                // 虽然 API 调用成功，但根据优先级规则没有找到合适的 Logo
                                Log.w(TAG, "获取到图片列表，但在检查优先级后未找到合适的 Logo。");
                                App.post(callback::onLogoNotFound);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "TMDB 图片详情 JSON 解析或处理错误: ", e);
                            App.post(callback::onError);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "读取 TMDB 图片详情响应体时出错: ", e);
                        App.post(callback::onError);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
             Log.e(TAG, "构建 TMDB 图片详情 URL 时出错: ", e);
             App.post(callback::onError);
        } catch (Exception e) {
             Log.e(TAG, "TMDB 图片详情请求准备阶段发生异常: ", e);
             App.post(callback::onError);
        }
    }


    /**
     * 根据三级优先级查找最佳 Logo:
     * 1. 优先选择最宽的中文 (zh) Logo。
     * 2. 如果没有中文 Logo，则选择最宽的英文 (en) Logo。
     * 3. 如果没有中文或英文 Logo，则选择最宽的其他语言 Logo (包括 'null' 语言标签)。
     * 如果 logos 数组为空或没有找到任何有效的 Logo，则返回 null。
     *
     * @param logos 从 TMDb API 获取的 logo JSON 数组。
     * @return 最佳 Logo 的文件路径 (例如 "/abc.png")，如果找不到则返回 null。
     */
    private static String findBestLogoByPriority(@Nullable JSONArray logos) {
        if (logos == null || logos.length() == 0) {
            Log.d(TAG, "findBestLogo: 输入的 logos 数组为空或 null。");
            return null;
        }

        // 用于跟踪每个优先级下最佳候选者的变量
        String bestChineseLogoPath = null;
        int maxChineseWidth = -1;

        String bestEnglishLogoPath = null;
        int maxEnglishWidth = -1;

        String bestOtherLogoPath = null; // 其他语言（包括 null）
        int maxOtherWidth = -1;

        Log.d(TAG, "findBestLogo: 开始检查 " + logos.length() + " 个 logo (优先级: 中文 > 英文 > 其他)...");

        for (int i = 0; i < logos.length(); i++) {
            JSONObject logoInfo = logos.optJSONObject(i);
            if (logoInfo == null) {
                Log.v(TAG, "findBestLogo: 跳过索引 " + i + "，因为 logoInfo 为 null。");
                continue;
            }

            String filePath = logoInfo.optString("file_path", null);
            // 必须有文件路径
            if (filePath == null || filePath.isEmpty() || "null".equals(filePath)) {
                 Log.v(TAG, "findBestLogo: 跳过索引 " + i + "，因为 file_path 无效: " + filePath);
                 continue;
            }

            // 语言代码，TMDb 可能返回 "null" 字符串表示无语言标签
            String lang = logoInfo.optString("iso_639_1", "null");
            int width = logoInfo.optInt("width", -1);
            // double aspectRatio = logoInfo.optDouble("aspect_ratio", 0.0); // 当前逻辑不直接使用宽高比

            // Logo 必须有有效的正宽度
            if (width <= 0) {
                 Log.v(TAG, "findBestLogo: 跳过 logo (宽度无效 <= 0): " + filePath + " (width=" + width + ")");
                 continue;
            }

            // 详细日志记录当前检查的 Logo 信息
            Log.v(TAG, "findBestLogo: 检查 logo: path=" + filePath + ", lang=" + lang + ", width=" + width);

            // 判断语言类别 (忽略大小写比较)
            boolean isChinese = "zh".equalsIgnoreCase(lang);
            boolean isEnglish = "en".equalsIgnoreCase(lang);

            // --- 检查是否为更宽的中文 Logo (优先级 1) ---
            if (isChinese) {
                if (width > maxChineseWidth) {
                    // 使用 Debug 级别记录找到新候选者
                    Log.d(TAG, "findBestLogo: 找到新的最宽中文 Logo (P1): " + filePath + " (宽度: " + width + ")");
                    maxChineseWidth = width;
                    bestChineseLogoPath = filePath;
                }
            }
            // --- 检查是否为更宽的英文 Logo (优先级 2) ---
            else if (isEnglish) { // 只有不是中文时才检查是否为英文
                if (width > maxEnglishWidth) {
                    Log.d(TAG, "findBestLogo: 找到新的最宽英文 Logo (P2): " + filePath + " (宽度: " + width + ")");
                    maxEnglishWidth = width;
                    bestEnglishLogoPath = filePath;
                }
            }
            // --- 检查是否为更宽的其他语言 Logo (优先级 3) ---
            else { // 既不是中文也不是英文 (包括 lang 为 "null" 或其他语言代码)
                if (width > maxOtherWidth) {
                    Log.d(TAG, "findBestLogo: 找到新的最宽其他语言 Logo (P3): " + filePath + " (宽度: " + width + ", 语言: " + lang + ")");
                    maxOtherWidth = width;
                    bestOtherLogoPath = filePath;
                }
            }
        }

        // --- 根据优先级返回结果 ---
        if (bestChineseLogoPath != null) {
            // 使用 Info 级别记录最终选择
            Log.i(TAG, "findBestLogo: 最终选择: 最宽的中文 Logo (P1): " + bestChineseLogoPath);
            return bestChineseLogoPath;
        } else if (bestEnglishLogoPath != null) {
            Log.i(TAG, "findBestLogo: 最终选择: 未找到中文 Logo, 选择最宽的英文 Logo (P2): " + bestEnglishLogoPath);
            return bestEnglishLogoPath;
        } else if (bestOtherLogoPath != null) {
            Log.i(TAG, "findBestLogo: 最终选择: 未找到中/英文 Logo, 选择最宽的其他语言 Logo (P3): " + bestOtherLogoPath);
            return bestOtherLogoPath;
        } else {
            // 使用 Warning 级别记录未找到任何 Logo
            Log.w(TAG, "findBestLogo: 检查了 " + logos.length() + " 个 logo，但未找到任何符合条件的 Logo。");
            return null; // 所有优先级都未找到
        }
    }


    // --- 内部使用的回调接口 ---

    /**
     * 用于在异步搜索后传递找到的 TMDb ID 的回调接口。
     */
    @FunctionalInterface
    private interface IdCallback {
        /**
         * 当找到 ID 时调用。
         * @param id 找到的 TMDb ID，如果未找到则为 null。
         */
        void onIdFound(@Nullable Integer id);
    }

    /**
     * 用于在异步操作中发生错误时进行通知的回调接口。
     */
     @FunctionalInterface
    private interface ErrorCallback {
        /**
         * 当发生错误时调用。
         */
        void onError();
    }
}