package com.fongmi.android.tv.player.exo;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.setting.PreloadSetting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class PreCache {

    private DefaultPreloadManager manager;
    private ExecutorService executor;
    private MediaItem mediaItem;

    public void start(ExoPlayer player, MediaItem mediaItem) {
        this.mediaItem = mediaItem;
        restart();
    }

    public void stop() {
        stopManager();
        mediaItem = null;
    }

    public void release() {
        stop();
    }

    private void restart() {
        stopManager();
        if (mediaItem == null) return;
        if (!PreloadSetting.isPreload()) return;
        if (!canPreload(mediaItem)) return;
        executor = Executors.newFixedThreadPool(PreloadSetting.getPreloadThreads());
        manager = createManager(mediaItem);
        manager.setCurrentPlayingIndex(0);
        manager.add(mediaItem, 0);
        manager.invalidate();
    }

    private void stopManager() {
        if (manager != null) manager.release();
        manager = null;
        if (executor != null) executor.shutdownNow();
        executor = null;
    }

    private DefaultPreloadManager createManager(MediaItem mediaItem) {
        return new DefaultPreloadManager.Builder(App.get(), rankingData -> DefaultPreloadManager.PreloadStatus.specifiedRangeCached(PreloadSetting.getPreloadDurationMs()))
                .setCache(MediaSourceFactory.getCache())
                .setDataSourceFactory(MediaSourceFactory.createUpstreamDataSourceFactory(ExoUtil.extractHeaders(mediaItem)))
                .setRenderersFactory(ExoUtil.buildRenderersFactory())
                .setCachingExecutor(executor)
                .build();
    }

    private boolean canPreload(MediaItem mediaItem) {
        if (mediaItem.localConfiguration == null) return false;
        String scheme = mediaItem.localConfiguration.uri.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }
}
