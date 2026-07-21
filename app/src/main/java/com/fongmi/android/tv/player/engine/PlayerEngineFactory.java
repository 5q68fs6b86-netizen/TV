package com.fongmi.android.tv.player.engine;

import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.EXO;
import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.MPV;

import androidx.media3.common.Player;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.player.exo.ExoPlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.player.mpv.MpvPlayerEngine;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.MpvLogCollector;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.UrlUtil;

public final class PlayerEngineFactory {

    public static PlayerEngine create(int decode, Player.Listener listener) {
        return create(decode, resolve(), listener);
    }

    public static PlayerEngine create(int decode, PlaySpec spec, Player.Listener listener) {
        return create(decode, resolve(spec), listener);
    }

    private static PlayerEngine create(int decode, PlayerEngine.Type type, Player.Listener listener) {
        return switch (type) {
            case EXO -> new ExoPlayerEngine(decode, listener);
            case MPV -> createMpv(decode, listener);
        };
    }

    private static PlayerEngine createMpv(int decode, Player.Listener listener) {
        try {
            return new MpvPlayerEngine(decode, listener);
        } catch (Throwable e) {
            // Never silent: UnsatisfiedLinkError / native init failures used to fall back to Exo
            // with no signal, so settings still said MPV while Exo was actually playing.
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            MpvLogCollector.logError("PlayerEngineFactory", "MPV 初始化失败, 回退 Exo — " + msg);
            App.post(() -> Notify.show("MPV 不可用, 已回退 Exo\n" + msg));
            return new ExoPlayerEngine(decode, listener);
        }
    }

    public static boolean matches(PlayerEngine engine, PlaySpec spec) {
        return engine != null && engine.getType() == resolve(spec);
    }

    private static PlayerEngine.Type resolve(PlaySpec spec) {
        if (!isMpvReady()) return EXO;
        if (requiresExo(spec)) return EXO;
        return MPV;
    }

    private static PlayerEngine.Type resolve() {
        return isMpvReady() ? MPV : EXO;
    }

    private static boolean requiresExo(PlaySpec spec) {
        String scheme = UrlUtil.scheme(spec.getUrl());
        return spec.getDrm() != null || "smb".equals(scheme) || "content".equals(scheme);
    }

    private static boolean isMpvReady() {
        return PlayerSetting.isMpv() && MpvPlayerEngine.isAvailable();
    }
}
