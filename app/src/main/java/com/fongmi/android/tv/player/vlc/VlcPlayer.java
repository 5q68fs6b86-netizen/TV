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
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-sub-autodetect-file");
        options.add("--avcodec-codec=h264");
        options.add("--avcodec-threads=4");
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        mLibVLC = new LibVLC(context, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mVideoLayout = new VLCVideoLayout(context);
    }

    public View getPlayerView() {
        return mVideoLayout;
    }

    public void setPlayerListener(MediaPlayer.EventListener listener) {
        mEventListener = listener;
    }

    public void start(String url) {
        final Media media = new Media(mLibVLC, Uri.parse(url));
        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.attachViews(mVideoLayout, null, false, false);
        mMediaPlayer.setEventListener(mEventListener);
        mMediaPlayer.play();
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void play() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.play();
        }
    }

    public void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.detachViews();
    }

    public void release() {
        mMediaPlayer.release();
        mLibVLC.release();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public long getPosition() {
        return mMediaPlayer.getTime();
    }

    public long getDuration() {
        return mMediaPlayer.getLength();
    }

    public void seekTo(long time) {
        mMediaPlayer.setTime(time);
    }
} 