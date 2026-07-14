package com.fongmi.android.tv.ai.subtitle;

import android.app.ActivityManager;
import android.content.Context;
import android.os.StatFs;

import androidx.annotation.Nullable;

import com.github.catvod.net.OkHttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class AsrModelManager {
    private static final ConcurrentHashMap<String, String> VERIFIED = new ConcurrentHashMap<>();
    public interface Listener {
        void onProgress(int percent, String fileName);

        void onComplete();

        void onError(String message);
    }

    private final Context context;
    private final File root;
    private final ExecutorService downloads;
    private final AtomicBoolean downloading = new AtomicBoolean();

    public AsrModelManager(Context context) {
        this.context = context.getApplicationContext();
        this.root = new File(this.context.getFilesDir(), "asr-models");
        this.downloads = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ai-model-download");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        root.mkdirs();
    }

    public File modelDir(AsrModel model) {
        return new File(root, model.folder);
    }

    public File vadFile() {
        return new File(root, AsrModel.SILERO_VAD.relativePath);
    }

    public boolean isInstalled(AiLanguage language) {
        AsrModel model = language.model();
        return isInstalled(model) && (!model.needsVad || valid(vadFile(), AsrModel.SILERO_VAD));
    }

    public boolean isInstalled(AsrModel model) {
        File dir = modelDir(model);
        for (ModelFile file : model.files) if (!valid(new File(dir, file.relativePath), file)) return false;
        return true;
    }

    public long getTotalRamMb() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        return info.totalMem / (1024L * 1024L);
    }

    public boolean canRun(AiLanguage language) {
        return getTotalRamMb() >= language.model().minRamMb;
    }

    public long requiredBytes(AiLanguage language) {
        AsrModel model = language.model();
        File dir = modelDir(model);
        long bytes = 0L;
        for (ModelFile file : model.files) {
            if (!valid(new File(dir, file.relativePath), file)) bytes += file.size;
        }
        if (model.needsVad && !valid(vadFile(), AsrModel.SILERO_VAD)) bytes += AsrModel.SILERO_VAD.size;
        return bytes;
    }

    public boolean hasEnoughStorage(AiLanguage language) {
        StatFs stat = new StatFs(root.getAbsolutePath());
        return stat.getAvailableBytes() > requiredBytes(language) + 128L * 1024L * 1024L;
    }

    public void download(AiLanguage language, Listener listener) {
        if (!downloading.compareAndSet(false, true)) {
            listener.onError("已有语言包正在下载");
            return;
        }
        downloads.execute(() -> {
            try {
                if (!canRun(language)) throw new IllegalStateException("设备内存不足，无法安全加载该语种语言包");
                if (!hasEnoughStorage(language)) throw new IllegalStateException("存储空间不足");
                AsrModel model = language.model();
                long total = requiredBytes(language);
                long[] completed = {0L};
                if (model.needsVad && !valid(vadFile(), AsrModel.SILERO_VAD)) {
                    downloadFile(AsrModel.SILERO_VAD, vadFile(), completed, total, listener);
                }
                File dir = modelDir(model);
                for (ModelFile file : model.files) {
                    File target = new File(dir, file.relativePath);
                    if (valid(target, file)) continue;
                    downloadFile(file, target, completed, total, listener);
                }
                listener.onComplete();
            } catch (Throwable e) {
                listener.onError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            } finally {
                downloading.set(false);
            }
        });
    }

    public void delete(AiLanguage language) {
        deleteTree(modelDir(language.model()));
    }

    public boolean isDownloading() {
        return downloading.get();
    }

    private void downloadFile(ModelFile model, File target, long[] completed, long total, Listener listener) throws Exception {
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        File part = new File(target.getAbsolutePath() + ".part");
        if (part.exists()) part.delete();
        Request request = new Request.Builder().url(model.url).build();
        try (Response response = OkHttp.client(120_000L).newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IllegalStateException("下载失败 HTTP " + response.code());
            ResponseBody body = response.body();
            if (body == null) throw new IllegalStateException("下载响应为空");
            try (InputStream input = body.byteStream(); FileOutputStream output = new FileOutputStream(part)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                long current = 0;
                int lastPercent = -1;
                while ((read = input.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException("下载已取消");
                    output.write(buffer, 0, read);
                    current += read;
                    int percent = (int) Math.min(99, ((completed[0] + current) * 100L) / Math.max(1L, total));
                    if (percent != lastPercent) {
                        lastPercent = percent;
                        listener.onProgress(percent, model.relativePath);
                    }
                }
                output.getFD().sync();
            }
            if (!valid(part, model)) throw new IllegalStateException("语言包校验失败: " + model.relativePath);
            if (target.exists() && !target.delete()) throw new IllegalStateException("无法替换旧语言包");
            if (!part.renameTo(target)) throw new IllegalStateException("无法提交语言包文件");
            File partMarker = marker(part);
            File targetMarker = marker(target);
            if (targetMarker.exists()) targetMarker.delete();
            if (partMarker.exists()) partMarker.renameTo(targetMarker);
            completed[0] += model.size;
        } finally {
            if (part.exists()) part.delete();
            File marker = marker(part);
            if (marker.exists()) marker.delete();
        }
    }

    private static boolean valid(File file, ModelFile model) {
        if (!file.isFile() || file.length() != model.size) return false;
        if (model.sha256.isEmpty()) return true;
        String signature = file.length() + ":" + file.lastModified() + ":" + model.sha256;
        if (signature.equals(VERIFIED.get(file.getAbsolutePath()))) return true;
        File marker = marker(file);
        try {
            if (marker.isFile() && signature.equals(readMarker(marker))) {
                VERIFIED.put(file.getAbsolutePath(), signature);
                return true;
            }
        } catch (Exception ignored) {
        }
        boolean valid = model.sha256.equalsIgnoreCase(sha256(file));
        if (valid) {
            VERIFIED.put(file.getAbsolutePath(), signature);
            try {
                try (FileOutputStream output = new FileOutputStream(marker)) {
                    output.write(signature.getBytes(StandardCharsets.UTF_8));
                    output.getFD().sync();
                }
            } catch (Exception ignored) {
            }
        } else if (marker.exists()) {
            marker.delete();
        }
        return valid;
    }

    private static File marker(File file) {
        return new File(file.getAbsolutePath() + ".verified");
    }

    private static String readMarker(File marker) throws Exception {
        if (marker.length() <= 0 || marker.length() > 512) return "";
        byte[] data = new byte[(int) marker.length()];
        try (FileInputStream input = new FileInputStream(marker)) {
            int offset = 0;
            while (offset < data.length) {
                int read = input.read(data, offset, data.length - offset);
                if (read < 0) break;
                offset += read;
            }
            if (offset != data.length) return "";
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    @Nullable
    private static String sha256(File file) {
        try (FileInputStream input = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[128 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            StringBuilder value = new StringBuilder();
            for (byte b : digest.digest()) value.append(String.format(Locale.ROOT, "%02x", b));
            return value.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void deleteTree(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteTree(child);
        VERIFIED.remove(file.getAbsolutePath());
        file.delete();
    }
}
