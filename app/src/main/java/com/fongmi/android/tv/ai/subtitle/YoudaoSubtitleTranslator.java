package com.fongmi.android.tv.ai.subtitle;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** The reference app's no-account Youdao web translator, isolated from the player thread. */
final class YoudaoSubtitleTranslator {
    private static final String TAG = "AiSubtitle";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private final OkHttpClient client;
    private volatile Keys keys;
    private volatile Call activeCall;

    private record Keys(String secret, String aesKey, String aesIv, long createdAt) {
    }

    YoudaoSubtitleTranslator(OkHttpClient client) {
        this.client = client;
    }

    void cancel() {
        Call call = activeCall;
        activeCall = null;
        if (call != null) call.cancel();
    }

    void translate(AiLanguage language, String source, OpenAiSubtitleTranslator.ResultCallback callback) {
        Keys cached = keys;
        if (cached != null && System.currentTimeMillis() - cached.createdAt < 600_000L) {
            requestTranslation(cached, source, callback);
        } else {
            requestKeys(source, callback);
        }
    }

    private void requestKeys(String source, OpenAiSubtitleTranslator.ResultCallback callback) {
        String time = String.valueOf(System.currentTimeMillis());
        String sign = md5Hex("client=fanyideskweb&mysticTime=" + time + "&product=webfanyi&key=asdjnjfenknafdfsdfsd");
        String url = "https://dict.youdao.com/webtranslate/key?client=fanyideskweb&product=webfanyi" +
                "&appVersion=1.0.0&vendor=web&pointParam=client%2CmysticTime%2Cproduct&keyfrom=fanyi.web" +
                "&keyid=webfanyi-key-getter&sign=" + sign + "&mysticTime=" + time;
        Request request = base(new Request.Builder().url(url)).get().build();
        Call call = client.newCall(request);
        activeCall = call;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException error) {
                if (call.isCanceled()) return;
                fail(source, callback, "默认翻译取密钥失败", error);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        fail(source, callback, "默认翻译取密钥 HTTP " + response.code(), null);
                        return;
                    }
                    JSONObject root = new JSONObject(response.body().string());
                    JSONObject data = root.optJSONObject("data");
                    if (root.optInt("code", -1) != 0 || data == null) throw new IllegalStateException("invalid key response");
                    Keys value = new Keys(data.getString("secretKey"), data.getString("aesKey"), data.getString("aesIv"), System.currentTimeMillis());
                    keys = value;
                    requestTranslation(value, source, callback);
                } catch (Exception error) {
                    fail(source, callback, "默认翻译密钥响应错误", error);
                }
            }
        });
    }

    private void requestTranslation(Keys key, String source, OpenAiSubtitleTranslator.ResultCallback callback) {
        String time = String.valueOf(System.currentTimeMillis());
        String sign = md5Hex("client=fanyideskweb&mysticTime=" + time + "&product=webfanyi&key=" + key.secret);
        FormBody body = new FormBody.Builder()
                .add("client", "fanyideskweb").add("product", "webfanyi").add("appVersion", "1.0.0")
                .add("vendor", "web").add("pointParam", "client,mysticTime,product").add("keyfrom", "fanyi.web")
                .add("keyid", "webfanyi").add("sign", sign).add("mysticTime", time).add("i", source)
                .add("from", "auto").add("to", "zh-CHS").add("dictResult", "false").build();
        Request request = base(new Request.Builder().url("https://dict.youdao.com/webtranslate")).post(body).build();
        Call call = client.newCall(request);
        activeCall = call;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException error) {
                if (call.isCanceled()) return;
                fail(source, callback, "默认翻译网络错误", error);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        fail(source, callback, "默认翻译 HTTP " + response.code(), null);
                        return;
                    }
                    String plain = decrypt(response.body().string(), key.aesKey, key.aesIv);
                    JSONArray paragraphs = new JSONObject(plain).optJSONArray("translateResult");
                    if (paragraphs == null) throw new IllegalStateException("missing translateResult");
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < paragraphs.length(); i++) {
                        JSONArray parts = paragraphs.getJSONArray(i);
                        if (i > 0) result.append('\n');
                        for (int j = 0; j < parts.length(); j++) result.append(parts.getJSONObject(j).optString("tgt", ""));
                    }
                    if (result.toString().isBlank()) throw new IllegalStateException("empty translation");
                    callback.onSuccess(source, result.toString());
                } catch (Exception error) {
                    fail(source, callback, "默认翻译响应错误", error);
                }
            }
        });
    }

    private static Request.Builder base(Request.Builder builder) {
        return builder.header("User-Agent", USER_AGENT)
                .header("Referer", "https://fanyi.youdao.com")
                .header("Cookie", "OUTFOX_SEARCH_USER_ID=1796239350@10.110.96.157;");
    }

    private static String decrypt(String encoded, String key, String iv) throws Exception {
        byte[] data = Base64.decode(encoded.replace('-', '+').replace('_', '/'), Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(md5(key), "AES"), new IvParameterSpec(md5(iv)));
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    private static String md5Hex(String value) {
        try {
            byte[] digest = md5(value);
            StringBuilder result = new StringBuilder(32);
            for (byte b : digest) result.append(String.format("%02x", b & 0xff));
            return result.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static byte[] md5(String value) throws Exception {
        return MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
    }

    private static void fail(String source, OpenAiSubtitleTranslator.ResultCallback callback, String message, Throwable error) {
        Log.w(TAG, message + (error == null ? "" : ": " + error.getClass().getSimpleName()));
        callback.onFailure(source, message);
    }
}
