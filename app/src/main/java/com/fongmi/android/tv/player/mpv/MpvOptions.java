package com.fongmi.android.tv.player.mpv;

import android.content.Context;
import android.text.TextUtils;

import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.util.PlayerHelper;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.PreloadSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.utils.MpvLogCollector;
import com.github.catvod.utils.Path;

import java.io.File;

import is.xyz.mpv.MPVLib;

/**
 * Central MPV option funnel (self-hosted), behaviour aligned with FongMi {@code MpvUtil}:
 * <ul>
 *   <li>Vulkan → only pre-init {@code gpu-api=vulkan} + {@code gpu-context=androidvk}</li>
 *   <li>{@code gpu-next} only selects {@code vo}, never forced by Vulkan</li>
 *   <li>config-dir / font cache / TLS CA / demuxer defaults</li>
 * </ul>
 * Does <b>not</b> import {@code androidx.media3.mpvplayer}.
 */
final class MpvOptions {

    static final String HWDEC_HARD = "mediacodec,mediacodec-copy";
    private static final String HWDEC_CODECS_BASE = "h264,hevc,mpeg4,mpeg2video,vp8,vp9,av1";
    private static final String HWDEC_CODECS_DOLBY = HWDEC_CODECS_BASE + ",dvhe,dvh1";
    private static final long DEFAULT_DEMUXER_BYTES = 64L * 1024L * 1024L;

    private MpvOptions() {
    }

    /** FongMi: gpu-next is opt-in only; Vulkan does not force it. */
    static String videoOutputDriver() {
        return PlayerSetting.isMpvGpuNext() ? "gpu-next" : "gpu";
    }

    static String decodeMode(int decode) {
        return decode == PlayerEngine.HARD ? HWDEC_HARD : "no";
    }

    /**
     * Options that must be set after {@code create} and before {@code init}.
     */
    static void applyPreInit(Context context, int decode) {
        File configDir = Path.mpv();
        File cacheDir = Path.mpvCache();
        MpvAssets.ensure(context, configDir);

        set("config", "yes");
        set("config-dir", configDir.getAbsolutePath());
        set("msg-level", "all=warn");
        set("gpu-shader-cache-dir", cacheDir.getAbsolutePath());
        set("icc-cache-dir", cacheDir.getAbsolutePath());
        set("profile", "fast");
        set("vo", videoOutputDriver());
        applyVideoOutputOptions();
        applyHdrHint();
        applyDecode(decode);
        applyHwdecCodecs();
        set("ao", "audiotrack,opensles");
        set("audio-set-media-role", "yes");
        set("tls-verify", "yes");
        set("tls-ca-file", new File(context.getFilesDir(), "cacert.pem").getAbsolutePath());
        set("input-default-bindings", "yes");
        // FongMi: HLS HTTP persistent off
        set("demuxer-lavf-o", "http_persistent=0");
        applyDemuxerBudget();
        applyDefaultUserAgent();
        MpvLogCollector.log("MpvOptions", "pre-init vo=" + videoOutputDriver()
                + " vulkan=" + PlayerSetting.isMpvVulkan()
                + " gpu-next=" + PlayerSetting.isMpvGpuNext()
                + " decode=" + (decode == PlayerEngine.HARD ? "hard" : "soft"));
    }

    /**
     * Options / side effects after {@code init}.
     */
    static void applyPostInit(File configDir) {
        MpvAnime4K.apply(configDir);
        set("save-position-on-quit", "no");
        set("force-window", "no");
        set("idle", "once");
    }

    static void applyDecode(int decode) {
        String mode = decodeMode(decode);
        set("hwdec", mode);
        try {
            MPVLib.INSTANCE.setPropertyString("hwdec", mode);
        } catch (Throwable ignored) {
            // Property writes can fail before init/runtime is ready.
        }
    }

    static void applyHwdecCodecs() {
        String codecs = PlayerSetting.isMpvDolbyHwdecEnabled() ? HWDEC_CODECS_DOLBY : HWDEC_CODECS_BASE;
        set("hwdec-codecs", codecs);
        try {
            MPVLib.INSTANCE.setPropertyString("hwdec-codecs", codecs);
        } catch (Throwable ignored) {
        }
        MpvLogCollector.log("MpvOptions", "hwdec-codecs=" + codecs);
    }

    /**
     * Vulkan: only gpu-api + androidvk (FongMi MpvUtil.addVideoOutputOptions).
     * GL path keeps opengl-es + android context.
     */
    private static void applyVideoOutputOptions() {
        if (PlayerSetting.isMpvVulkan()) {
            set("gpu-api", "vulkan");
            set("gpu-context", "androidvk");
        } else {
            set("opengl-es", "yes");
            set("gpu-context", "android");
        }
    }

    /**
     * Optional kernel hint only — not a product "HDR mode" guarantee.
     * Unknown options are ignored by mpv.
     */
    private static void applyHdrHint() {
        int mode = PlayerSetting.getMpvHdr();
        String hint = switch (mode) {
            case PlayerSetting.MPV_HDR_ON -> "yes";
            case PlayerSetting.MPV_HDR_OFF -> "no";
            default -> "auto";
        };
        try {
            set("target-colorspace-hint", hint);
            MpvLogCollector.log("MpvOptions", "target-colorspace-hint=" + hint + " (kernel optional)");
        } catch (Throwable e) {
            MpvLogCollector.log("MpvOptions", "HDR hint not applied: " + e.getMessage());
        }
    }

    private static void applyDemuxerBudget() {
        long bytes = DEFAULT_DEMUXER_BYTES;
        if (PreloadSetting.isPreload()) {
            long preload = PreloadSetting.getPreloadSizeBytes();
            if (preload > 0) bytes = Math.max(bytes, preload);
        }
        String value = Long.toString(bytes);
        set("demuxer-max-bytes", value);
        set("demuxer-max-back-bytes", value);
    }

    private static void applyDefaultUserAgent() {
        String ua = Setting.getUa();
        if (TextUtils.isEmpty(ua)) ua = PlayerHelper.getDefaultUa();
        if (!TextUtils.isEmpty(ua)) set("user-agent", ua);
    }

    private static void set(String name, String value) {
        MPVLib.INSTANCE.setOptionString(name, value);
    }
}
