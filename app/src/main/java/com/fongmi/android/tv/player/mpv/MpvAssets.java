package com.fongmi.android.tv.player.mpv;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class MpvAssets {

    private static final String TAG = MpvAssets.class.getSimpleName();

    private MpvAssets() {
    }

    static void ensure(Context context, File configDir) {
        File filesDir = context.getFilesDir();
        copyAsset(context.getAssets(), "cacert.pem", new File(filesDir, "cacert.pem"));
        copyAsset(context.getAssets(), "subfont.ttf", new File(filesDir, "subfont.ttf"));
        copyAsset(context.getAssets(), "cacert.pem", new File(configDir, "cacert.pem"));
        copyAsset(context.getAssets(), "subfont.ttf", new File(configDir, "subfont.ttf"));
        copyAssetDirectory(context.getAssets(), "mpv/shaders", new File(configDir, "shaders"));
        writeFontsConf(context, new File(filesDir, "fonts.conf"), true);
        writeFontsConf(context, new File(configDir, "fonts.conf"), false);
    }

    private static void copyAssetDirectory(AssetManager assets, String path, File outDir) {
        try {
            String[] names = assets.list(path);
            if (names == null || names.length == 0) return;
            if (!outDir.exists() && !outDir.mkdirs()) return;
            for (String name : names) copyAsset(assets, path + "/" + name, new File(outDir, name));
        } catch (IOException e) {
            Log.w(TAG, "Unable to copy mpv asset directory: " + path, e);
        }
    }

    private static void copyAsset(AssetManager assets, String name, File outFile) {
        try (InputStream in = assets.open(name, AssetManager.ACCESS_STREAMING)) {
            if (outFile.exists() && outFile.length() == in.available()) return;
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[16384];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to copy mpv asset: " + name, e);
        }
    }

    private static void writeFontsConf(Context context, File file, boolean overwrite) {
        if (!overwrite && file.exists()) return;
        String filesDir = escapeXml(context.getFilesDir().getAbsolutePath());
        String cacheDir = escapeXml(context.getCacheDir().getAbsolutePath());
        String xml = String.join("\n",
                "<fontconfig>",
                "<dir>" + filesDir + "</dir>",
                "<dir>/system/fonts/</dir>",
                "<dir>/product/fonts/</dir>",
                "<cachedir>" + cacheDir + "</cachedir>",
                "<alias><family>serif</family><prefer><family>Noto Serif</family></prefer></alias>",
                "<alias><family>sans-serif</family><prefer><family>Roboto</family><family>Noto Sans</family></prefer></alias>",
                "<alias><family>monospace</family><prefer><family>Droid Sans Mono</family></prefer></alias>",
                "</fontconfig>");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(xml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "Unable to write mpv fonts.conf", e);
        }
    }

    private static String escapeXml(String value) {
        if (TextUtils.isEmpty(value)) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
