package com.fongmi.android.tv.player.mpv;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;

public class MpvPlayerEngine implements PlayerEngine {

    public MpvPlayerEngine(int decode, Player.Listener listener) {
        throw unavailable();
    }

    public static boolean isAvailable() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.MPV;
    }

    @Override
    public Player getPlayer() {
        throw unavailable();
    }

    @Override
    public void release() {
    }

    @Override
    public Player rebuild() {
        throw unavailable();
    }

    @Override
    public boolean addSubtitle(Sub sub) {
        return false;
    }

    @Override
    public boolean setDecode(int decode) {
        return false;
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        throw unavailable();
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isVod() {
        return false;
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return new MpvErrorMsgProvider().get(e);
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return ErrorAction.FATAL;
    }

    private static UnsupportedOperationException unavailable() {
        return new UnsupportedOperationException("MPV Media3 player is not bundled.");
    }
}
