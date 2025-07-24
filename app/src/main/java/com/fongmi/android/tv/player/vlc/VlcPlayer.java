package com.fongmi.android.tv.player.vlc;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class VlcPlayer {

    private MediaPlayer.EventListener mEventListener;
    private VLCVideoLayout mVideoLayout;
    private MediaPlayer mMediaPlayer;
    private LibVLC mLibVLC;

    public VlcPlayer(Context context) {
        mVideoLayout = new VLCVideoLayout(context);
    }

    public void init(Context context) {
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-sub-autodetect-file");
        options.add("--avcodec-codec=h264");
        options.add("--avcodec-threads=4");
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        mLibVLC = new LibVLC(context, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setEventListener(mEventListener);
    }

    public View getPlayerView() {
        return mVideoLayout;
    }

    public void setPlayerListener(MediaPlayer.EventListener listener) {
        mEventListener = listener;
    }

    public void start(String url) {
        if (mMediaPlayer == null) return;
        final Media media = new Media(mLibVLC, Uri.parse(url));
        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.attachViews(mVideoLayout, null, false, false);
        mMediaPlayer.play();
    }

    public void pause() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void play() {
        if (mMediaPlayer == null) return;
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.play();
        }
    }

    public void stop() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.stop();
        mMediaPlayer.detachViews();
    }

    public void release() {
        if (mMediaPlayer != null) mMediaPlayer.release();
        if (mLibVLC != null) mLibVLC.release();
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) return false;
        return mMediaPlayer.isPlaying();
    }

    public long getPosition() {
        if (mMediaPlayer == null) return 0;
        return mMediaPlayer.getTime();
    }

    public long getDuration() {
        if (mMediaPlayer == null) return 0;
        return mMediaPlayer.getLength();
    }

    public void seekTo(long time) {
        if (mMediaPlayer == null) return;
        mMediaPlayer.setTime(time);
    }
} 