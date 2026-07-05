package com.fongmi.android.tv.api;

import androidx.annotation.Nullable;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.github.catvod.net.OkHttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LogvrApi {

    private static final String TAG = LogvrApi.class.getSimpleName();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern SEASON_PATTERN = Pattern.compile("(?i)\\bS(\\d{1,2})\\b|(?:第)?([0-9一二三四五六七八九十两]+)季");
    private static final Pattern EPISODE_CHINESE_PATTERN = Pattern.compile("第\\s*0*(\\d{1,4})\\s*[集话話]");
    private static final Pattern EPISODE_EP_PATTERN = Pattern.compile("(?i)\\bE(?:P)?0*(\\d{1,4})\\b");
    private static final Pattern EPISODE_NUMBER_PATTERN = Pattern.compile("^\\D*0*(\\d{1,4})\\D*$");

    public interface Listener {

        void onSuccess(List<Danmaku> items);

        void onError(Exception e);
    }

    public static boolean canSearch() {
        return !normalizeBase(DanmakuSetting.getEffectiveLogvrUrl()).isEmpty();
    }

    public static void searchAuto(String name, String episode, @Nullable Result result, Listener listener) {
        String base = normalizeBase(DanmakuSetting.getEffectiveLogvrUrl());
        if (base.isEmpty()) {
            postSuccess(listener, Collections.emptyList());
            return;
        }
        String videoUrl = preferredVideoUrl(result);
        if (isHttpUrl(videoUrl)) {
            searchByVideoUrl(base, videoUrl, new Listener() {
                @Override
                public void onSuccess(List<Danmaku> items) {
                    if (items.isEmpty()) searchByMatch(base, name, episode, listener);
                    else listener.onSuccess(items);
                }

                @Override
                public void onError(Exception e) {
                    searchByMatch(base, name, episode, listener);
                }
            });
        } else {
            searchByMatch(base, name, episode, listener);
        }
    }

    public static void searchEpisodes(String name, String episode, Listener listener) {
        String base = normalizeBase(DanmakuSetting.getEffectiveLogvrUrl());
        if (base.isEmpty()) {
            postSuccess(listener, Collections.emptyList());
            return;
        }
        String url = buildSearchEpisodesUrl(base, name, episode);
        if (url.isEmpty()) {
            postSuccess(listener, Collections.emptyList());
            return;
        }
        OkHttp.newCall(url, TAG).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    String text = body == null ? "" : body.string();
                    postSuccess(listener, response.isSuccessful() ? parseEpisodes(base, text) : Collections.emptyList());
                } catch (Exception e) {
                    postError(listener, e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                postError(listener, e);
            }
        });
    }

    private static void searchByVideoUrl(String base, String videoUrl, Listener listener) {
        String url = buildCommentByUrlUrl(base, videoUrl);
        if (url.isEmpty()) {
            postSuccess(listener, Collections.emptyList());
            return;
        }
        OkHttp.newCall(url, TAG).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    String text = body == null ? "" : body.string();
                    if (response.isSuccessful() && hasDanmakuXml(text)) {
                        postSuccess(listener, Collections.singletonList(createDanmaku("Logvr URL", url)));
                    } else {
                        postSuccess(listener, Collections.emptyList());
                    }
                } catch (Exception e) {
                    postError(listener, e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                postError(listener, e);
            }
        });
    }

    private static void searchByMatch(String base, String name, String episode, Listener listener) {
        String url = buildMatchUrl(base);
        if (url.isEmpty()) {
            postSuccess(listener, Collections.emptyList());
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("fileName", buildMatchFileName(name, episode));
        RequestBody body = RequestBody.create(json.toString(), JSON);
        OkHttp.newCall(url, body, TAG).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    String text = body == null ? "" : body.string();
                    postSuccess(listener, response.isSuccessful() ? parseMatch(base, text) : Collections.emptyList());
                } catch (Exception e) {
                    postError(listener, e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                postError(listener, e);
            }
        });
    }

    public static String normalizeBase(String url) {
        if (url == null) return "";
        String base = url.trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (base.isEmpty()) return "";
        return base.endsWith("/api/v2") ? base : base + "/api/v2";
    }

    public static String buildCommentByUrlUrl(String base, String videoUrl) {
        HttpUrl parsed = HttpUrl.parse(normalizeBase(base) + "/comment");
        if (parsed == null || !isHttpUrl(videoUrl)) return "";
        return parsed.newBuilder().addQueryParameter("url", videoUrl).addQueryParameter("format", "xml").build().toString();
    }

    public static String buildCommentUrl(String base, String episodeId) {
        if (episodeId == null || episodeId.trim().isEmpty()) return "";
        HttpUrl parsed = HttpUrl.parse(normalizeBase(base) + "/comment/" + episodeId.trim());
        if (parsed == null) return "";
        return parsed.newBuilder().addQueryParameter("format", "xml").build().toString();
    }

    public static String buildSearchEpisodesUrl(String base, String name, String episode) {
        HttpUrl parsed = HttpUrl.parse(normalizeBase(base) + "/search/episodes");
        if (parsed == null) return "";
        return parsed.newBuilder().addQueryParameter("anime", safe(name)).addQueryParameter("episode", buildSearchEpisodeQuery(episode)).build().toString();
    }

    public static String buildMatchUrl(String base) {
        return normalizeBase(base).isEmpty() ? "" : normalizeBase(base) + "/match";
    }

    public static String buildMatchFileName(String name, String episode) {
        String title = safe(name);
        String epName = safe(episode);
        int season = parseSeason(title + " " + epName);
        int ep = parseEpisode(epName);
        if (ep > 0) return String.format(Locale.ROOT, "%s S%02dE%02d", title, Math.max(1, season), ep).trim();
        return (title + " " + epName).trim();
    }

    public static String buildSearchEpisodeQuery(String episode) {
        String value = safe(episode);
        int ep = parseEpisode(value);
        return ep > 0 ? String.valueOf(ep) : value;
    }

    public static String preferredVideoUrl(@Nullable Result result) {
        if (result == null) return "";
        String raw = result.getUrl().v();
        if (isHttpUrl(raw)) return raw;
        String real = result.getRealUrl();
        return isHttpUrl(real) ? real : "";
    }

    public static boolean hasDanmakuXml(String text) {
        return text != null && (text.contains("<d ") || text.contains("<d\t") || text.contains("<d\n"));
    }

    public static List<Danmaku> parseMatch(String base, String json) {
        List<Danmaku> items = new ArrayList<>();
        JsonArray matches = getArray(parseObject(json), "matches");
        if (matches == null) return items;
        for (JsonElement element : matches) {
            JsonObject item = object(element);
            String episodeId = string(item, "episodeId");
            String title = joinName(string(item, "animeTitle"), string(item, "episodeTitle"));
            String url = buildCommentUrl(base, episodeId);
            if (!url.isEmpty()) {
                items.add(createDanmaku(title.isEmpty() ? "Logvr" : title, url));
                break;
            }
        }
        return items;
    }

    public static List<Danmaku> parseEpisodes(String base, String json) {
        List<Danmaku> items = new ArrayList<>();
        JsonArray animes = getArray(parseObject(json), "animes");
        if (animes == null) return items;
        for (JsonElement animeElement : animes) {
            JsonObject anime = object(animeElement);
            String animeTitle = string(anime, "animeTitle");
            JsonArray episodes = getArray(anime, "episodes");
            if (episodes == null) continue;
            for (JsonElement episodeElement : episodes) {
                JsonObject episode = object(episodeElement);
                String episodeId = string(episode, "episodeId");
                String name = joinName(animeTitle, string(episode, "episodeTitle"));
                String url = buildCommentUrl(base, episodeId);
                if (!url.isEmpty()) items.add(createDanmaku(name.isEmpty() ? "Logvr" : name, url));
            }
        }
        return items;
    }

    public static boolean isHttpUrl(String url) {
        if (url == null) return false;
        String value = url.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private static int parseSeason(String text) {
        Matcher matcher = SEASON_PATTERN.matcher(safe(text));
        if (!matcher.find()) return 1;
        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        return parsePositiveNumber(value, 1);
    }

    private static int parseEpisode(String text) {
        String value = safe(text);
        int ep = findNumber(EPISODE_CHINESE_PATTERN, value);
        if (ep > 0) return ep;
        ep = findNumber(EPISODE_EP_PATTERN, value);
        if (ep > 0) return ep;
        return findNumber(EPISODE_NUMBER_PATTERN, value);
    }

    private static int findNumber(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? parsePositiveNumber(matcher.group(1), 0) : 0;
    }

    private static int parsePositiveNumber(String text, int fallback) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : fallback;
        } catch (Exception ignored) {
            int value = parseChineseNumber(text);
            return value > 0 ? value : fallback;
        }
    }

    private static int parseChineseNumber(String text) {
        String value = safe(text);
        if (value.isEmpty()) return 0;
        int ten = value.indexOf('十');
        if (ten >= 0) {
            int high = ten == 0 ? 1 : chineseDigit(value.charAt(ten - 1));
            int low = ten == value.length() - 1 ? 0 : chineseDigit(value.charAt(ten + 1));
            return high * 10 + low;
        }
        return chineseDigit(value.charAt(value.length() - 1));
    }

    private static int chineseDigit(char ch) {
        return switch (ch) {
            case '一', '壹' -> 1;
            case '二', '贰', '兩', '两' -> 2;
            case '三', '叁' -> 3;
            case '四', '肆' -> 4;
            case '五', '伍' -> 5;
            case '六', '陆' -> 6;
            case '七', '柒' -> 7;
            case '八', '捌' -> 8;
            case '九', '玖' -> 9;
            default -> 0;
        };
    }

    private static JsonObject parseObject(String json) {
        try {
            return object(JsonParser.parseString(json));
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static JsonObject object(JsonElement element) {
        try {
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static JsonArray getArray(JsonObject object, String key) {
        try {
            JsonElement element = object.get(key);
            return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String string(JsonObject object, String key) {
        try {
            JsonElement element = object.get(key);
            return element == null || element.isJsonNull() ? "" : element.getAsString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static Danmaku createDanmaku(String name, String url) {
        Danmaku danmaku = new Danmaku();
        danmaku.setName(name);
        danmaku.setUrl(url);
        return danmaku;
    }

    private static String joinName(String title, String episode) {
        String name = safe(title);
        String ep = safe(episode);
        if (name.isEmpty()) return ep;
        if (ep.isEmpty()) return name;
        return name + " " + ep;
    }

    private static String safe(String text) {
        return text == null ? "" : text.trim();
    }

    private static void postSuccess(Listener listener, List<Danmaku> items) {
        App.post(() -> listener.onSuccess(items));
    }

    private static void postError(Listener listener, Exception e) {
        App.post(() -> listener.onError(e));
    }

    public static void cancel() {
        OkHttp.cancel(TAG);
    }
}
