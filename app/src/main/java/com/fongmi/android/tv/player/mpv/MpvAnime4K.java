package com.fongmi.android.tv.player.mpv;

import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.MpvLogCollector;

import java.io.File;

import is.xyz.mpv.MPVLib;

final class MpvAnime4K {

    private static final String SHADER_DIR = "shaders";
    private static final String CLAMP = "Anime4K_Clamp_Highlights.glsl";
    private static final String AUTO_X2 = "Anime4K_AutoDownscalePre_x2.glsl";
    private static final String AUTO_X4 = "Anime4K_AutoDownscalePre_x4.glsl";
    private static final String UPSCALE_S = "Anime4K_Upscale_CNN_x2_S.glsl";
    private static final String UPSCALE_M = "Anime4K_Upscale_CNN_x2_M.glsl";
    private static final String UPSCALE_VL = "Anime4K_Upscale_CNN_x2_VL.glsl";

    private static final String[][] MODES = {
            {},
            {CLAMP, "Anime4K_Restore_CNN_M.glsl", UPSCALE_M, AUTO_X2, AUTO_X4, UPSCALE_S},
            {CLAMP, "Anime4K_Restore_CNN_Soft_M.glsl", UPSCALE_M, AUTO_X2, AUTO_X4, UPSCALE_S},
            {CLAMP, "Anime4K_Upscale_Denoise_CNN_x2_M.glsl", AUTO_X2, AUTO_X4, UPSCALE_S},
            {CLAMP, "Anime4K_Restore_CNN_VL.glsl", UPSCALE_VL, AUTO_X2, AUTO_X4, UPSCALE_M},
            {CLAMP, "Anime4K_Restore_CNN_Soft_VL.glsl", UPSCALE_VL, AUTO_X2, AUTO_X4, UPSCALE_M},
            {CLAMP, "Anime4K_Upscale_Denoise_CNN_x2_VL.glsl", AUTO_X2, AUTO_X4, UPSCALE_M},
    };

    private MpvAnime4K() {
    }

    static void apply(File configDir) {
        int mode = PlayerSetting.getMpvAnime4K();
        String[] shaders = mode >= 0 && mode < MODES.length ? MODES[mode] : MODES[PlayerSetting.MPV_ANIME4K_OFF];
        if (shaders.length == 0) return;
        File shaderDir = new File(configDir, SHADER_DIR);
        if (!command("change-list", "glsl-shaders", "clr", "")) return;
        boolean applied = true;
        for (String shader : shaders) applied &= append(shaderDir, shader);
        if (applied) MpvLogCollector.log("MpvAnime4K", "已启用 Anime4K 模式: " + mode);
        else MpvLogCollector.logError("MpvAnime4K", "Anime4K 模式未完整应用: " + mode);
    }

    private static boolean append(File shaderDir, String shader) {
        File file = new File(shaderDir, shader);
        if (!file.exists()) {
            MpvLogCollector.logError("MpvAnime4K", "缺少 shader: " + file.getAbsolutePath());
            return false;
        }
        return command("change-list", "glsl-shaders", "append", file.getAbsolutePath());
    }

    private static boolean command(String... args) {
        try {
            MPVLib.INSTANCE.command(args);
            return true;
        } catch (RuntimeException e) {
            MpvLogCollector.logError("MpvAnime4K", "Anime4K 配置失败: " + e.getMessage());
            return false;
        }
    }
}
