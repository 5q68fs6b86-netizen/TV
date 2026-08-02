package com.fongmi.android.tv.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.media3.ui.PlayerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.MpvLogCollector;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import is.xyz.mpv.MPVLib;

@RunWith(AndroidJUnit4.class)
public class MpvPlaybackTest {

    private static final String TAG = "MpvPlaybackTest";
    private static final String ARG_URL = "mpv.url";
    private static final String ARG_URL_ALT = "mpv_url";
    private static final String ARG_EXPECTED_SIZE = "mpv.expectedSize";
    private static final String ARG_EXPECTED_SIZE_ALT = "mpv_expected_size";
    private static final String DEFAULT_URL = "https://pub-ed8547997d934fd8af55b0393a9ff88f.r2.dev/%E7%AC%AC9%E9%9B%86(4).mp4";
    private static final long TIMEOUT_MS = 45_000;
    private static final long POLL_MS = 500;

    @Test
    public void playsMpvUrl() {
        Bundle arguments = InstrumentationRegistry.getArguments();
        String url = getArgument(arguments, ARG_URL, ARG_URL_ALT, DEFAULT_URL);
        String expectedSize = getArgument(arguments, ARG_EXPECTED_SIZE, ARG_EXPECTED_SIZE_ALT, "");
        prepareMpvSettings();
        for (int attempt = 1; attempt <= 2; attempt++) verifyPlaybackAttempt(url, expectedSize, attempt);
    }

    private void verifyPlaybackAttempt(String url, String expectedSize, int attempt) {
        MpvLogCollector.clear();
        Activity activity = startPlayback(url);
        try {
            PlaybackResult result = waitForPlayback(expectedSize);
            Log.i(TAG, "attempt=" + attempt + ", url=" + url + ", lastVideoSize=" + result.lastVideoSize + ", lastLog=" + result.lastLog);
            if (!result.passed) fail("MPV playback attempt " + attempt + " did not reach ready video state. lastVideoSize=" + result.lastVideoSize + ", lastLog=" + result.lastLog);
            if ("leanback".equals(BuildConfig.FLAVOR_mode)) verifyFullscreenSurface(activity);
            if (attempt == 1) verifyHttpHeaderFieldsContract();
        } finally {
            finishActivity(activity);
        }
    }

    /**
     * Gold-compatible header contract: use change-list + string property,
     * not getPropertyNode (absent from gold libplayer JNI).
     */
    private void verifyHttpHeaderFieldsContract() {
        String header = "Accept: video/mp4,video/*";
        MPVLib.INSTANCE.command("change-list", "http-header-fields", "clr", "");
        try {
            MPVLib.INSTANCE.command("change-list", "http-header-fields", "append", header);
            String fields = MPVLib.INSTANCE.getPropertyString("http-header-fields");
            assertNotNull(fields);
            assertTrue("expected header in http-header-fields, got: " + fields, fields.contains("Accept"));
        } finally {
            MPVLib.INSTANCE.command("change-list", "http-header-fields", "clr", "");
        }
        String cleared = MPVLib.INSTANCE.getPropertyString("http-header-fields");
        assertTrue(cleared == null || cleared.isEmpty() || "[]".equals(cleared.trim()));
    }

    private String getArgument(Bundle arguments, String key, String altKey, String defaultValue) {
        String value = arguments.getString(key);
        if (value == null || value.isEmpty()) value = arguments.getString(altKey);
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    private void prepareMpvSettings() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        preferences.edit()
                .putInt("player_engine", 1)
                .putInt("render", 0)
                .putInt("mpv_anime4k", 0)
                .putBoolean("mpv_gpu_next", false)
                .putBoolean("mpv_vulkan", false)
                .apply();
    }

    private Activity startPlayback(String url) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getTargetContext();
        String className = context.getPackageName() + ".ui.activity.VideoActivity";
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(className, null, false);
        Activity activity;
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), className));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("key", "push_agent");
            intent.putExtra("id", url);
            intent.putExtra("name", "MPV Test");
            context.startActivity(intent);
            activity = instrumentation.waitForMonitorWithTimeout(monitor, TIMEOUT_MS);
        } finally {
            instrumentation.removeMonitor(monitor);
        }
        assertNotNull("VideoActivity did not start", activity);
        return activity;
    }

    private void verifyFullscreenSurface(Activity activity) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        View initialSurface = getVideoSurfaceView(activity);
        int detailId = activity.getResources().getIdentifier("detail", "id", activity.getPackageName());
        assertTrue(initialSurface instanceof TextureView);
        assertTrue(detailId != 0);
        assertEquals(View.VISIBLE, getVisibility(activity, detailId));
        MpvLogCollector.clear();
        instrumentation.runOnMainSync(() -> activity.findViewById(R.id.video).performClick());
        instrumentation.waitForIdleSync();
        assertEquals(View.GONE, getVisibility(activity, detailId));
        assertSame(initialSurface, getVideoSurfaceView(activity));
        instrumentation.runOnMainSync(() -> ((FragmentActivity) activity).getOnBackPressedDispatcher().onBackPressed());
        instrumentation.waitForIdleSync();
        assertFalse(activity.isFinishing());
        assertEquals(View.VISIBLE, getVisibility(activity, detailId));
        assertSame(initialSurface, getVideoSurfaceView(activity));
        for (String log : MpvLogCollector.getLogs()) assertFalse("Fullscreen switched MPV Surface: " + log, log.contains("=== 分离 Surface ==="));
    }

    private View getVideoSurfaceView(Activity activity) {
        AtomicReference<View> surface = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            PlayerView playerView = activity.findViewById(R.id.player);
            surface.set(playerView.getVideoSurfaceView());
        });
        return surface.get();
    }

    private int getVisibility(Activity activity, int id) {
        AtomicReference<Integer> visibility = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> visibility.set(activity.findViewById(id).getVisibility()));
        return visibility.get();
    }

    private void finishActivity(Activity activity) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(activity::finish);
        instrumentation.waitForIdleSync();
    }

    private PlaybackResult waitForPlayback(String expectedSize) {
        long deadline = SystemClock.elapsedRealtime() + TIMEOUT_MS;
        String lastLog = "";
        String lastVideoSize = "";
        boolean fileLoaded = false;
        boolean firstFrame = false;
        boolean videoSize = false;
        while (SystemClock.elapsedRealtime() < deadline) {
            List<String> logs = MpvLogCollector.getLogs();
            for (String log : logs) {
                lastLog = log;
                if (log.contains("文件加载成功")) fileLoaded = true;
                if (log.contains("通知首帧已渲染")) firstFrame = true;
                if (log.contains("视频尺寸: ")) {
                    lastVideoSize = extractVideoSize(log);
                    videoSize = expectedSize.isEmpty() || expectedSize.equals(lastVideoSize);
                }
                if (log.contains("ERROR-")) return new PlaybackResult(false, lastLog, lastVideoSize);
            }
            if (fileLoaded && firstFrame && videoSize) return new PlaybackResult(true, lastLog, lastVideoSize);
            SystemClock.sleep(POLL_MS);
        }
        Log.e(TAG, "Timed out. fileLoaded=" + fileLoaded + ", firstFrame=" + firstFrame + ", videoSize=" + videoSize + ", lastVideoSize=" + lastVideoSize + ", lastLog=" + lastLog);
        return new PlaybackResult(false, lastLog, lastVideoSize);
    }

    private String extractVideoSize(String log) {
        int start = log.indexOf("视频尺寸: ");
        if (start == -1) return "";
        start += "视频尺寸: ".length();
        int end = log.indexOf(",", start);
        return end == -1 ? log.substring(start).trim() : log.substring(start, end).trim();
    }

    private static final class PlaybackResult {
        private final boolean passed;
        private final String lastLog;
        private final String lastVideoSize;

        private PlaybackResult(boolean passed, String lastLog, String lastVideoSize) {
            this.passed = passed;
            this.lastLog = lastLog;
            this.lastVideoSize = lastVideoSize;
        }
    }
}
