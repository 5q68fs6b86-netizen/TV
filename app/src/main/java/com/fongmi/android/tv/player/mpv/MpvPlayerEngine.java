package com.fongmi.android.tv.player.mpv;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;

@UnstableApi
public class MpvPlayerEngine implements PlayerEngine {

    private final MpvErrorMsgProvider provider;
    private final Player.Listener listener;
    private MpvPlayer player;
    private int decode;

    public MpvPlayerEngine(int decode, Player.Listener listener) {
        this.provider = new MpvErrorMsgProvider();
        this.listener = listener;
        this.decode = decode;
        this.player = new MpvPlayer(App.get(), decode);
        this.player.addListener(listener);
    }

    public static boolean isAvailable() {
        try {
            Class.forName("is.xyz.mpv.MPVLib");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public Type getType() {
        return Type.MPV;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void release() {
        player.removeListener(listener);
        player.release();
    }

    @Override
    public Player rebuild() {
        release();
        player = new MpvPlayer(App.get(), decode);
        player.addListener(listener);
        return player;
    }

    @Override
    public boolean addSubtitle(Sub sub) {
        return player.addSubtitle(sub);
    }

    @Override
    public boolean setDecode(int decode) {
        this.decode = decode;
        player.setDecode(decode);
        return false;
    }

    @Override
    public void setSubtitleStyle() {
        player.setSubtitleStyle();
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        player.start(spec, startPositionMs, decode);
    }

    @Override
    public boolean isLive() {
        return player.isLive();
    }

    @Override
    public boolean isVod() {
        return player.isVod();
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return provider.get(e);
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return ErrorAction.FATAL;
    }
}
