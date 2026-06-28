package androidx.media3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;

public class PlayerSeekView extends FrameLayout {

    private static final long MIN_UPDATE_INTERVAL_MS = 200;
    private static final long MAX_UPDATE_INTERVAL_MS = 1000;

    private final DefaultTimeBar timeBar;
    private final Runnable progressUpdater;
    private final Player.Listener playerListener;
    private final TimeBar.OnScrubListener scrubListener;

    private @Nullable Player player;
    private boolean scrubbing;

    public PlayerSeekView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerSeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSeekView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        progressUpdater = this::updateProgress;
        playerListener = new Player.Listener() {
            @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
                updateProgress();
            }
        };
        scrubListener = new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                scrubbing = true;
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                timeBar.setPosition(position);
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                Player current = player;
                scrubbing = false;
                if (!canceled && current != null && current.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
                    current.seekTo(position);
                }
                updateProgress();
            }
        };
        timeBar = new DefaultTimeBar(context, attrs);
        timeBar.setId(R.id.exo_progress);
        timeBar.addListener(scrubListener);
        addView(timeBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player == player) return;
        if (this.player != null) this.player.removeListener(playerListener);
        this.player = player;
        if (player != null) player.addListener(playerListener);
        updateProgress();
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateProgress();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(progressUpdater);
        super.onDetachedFromWindow();
    }

    private void updateProgress() {
        removeCallbacks(progressUpdater);
        Player current = player;
        if (current == null) {
            timeBar.setDuration(0);
            timeBar.setBufferedPosition(0);
            timeBar.setPosition(0);
            return;
        }
        timeBar.setDuration(normalizeTime(current.getDuration()));
        timeBar.setBufferedPosition(normalizeTime(current.getBufferedPosition()));
        if (!scrubbing) timeBar.setPosition(normalizeTime(current.getCurrentPosition()));
        if (isAttachedToWindow() && current.isPlaying()) postDelayed(progressUpdater, getUpdateDelayMs(current));
    }

    private long getUpdateDelayMs(Player player) {
        long delayMs = timeBar.getPreferredUpdateDelay();
        if (delayMs == Long.MAX_VALUE || delayMs <= 0) delayMs = MAX_UPDATE_INTERVAL_MS;
        float speed = player.getPlaybackParameters().speed;
        if (speed > 0) delayMs = (long) (delayMs / speed);
        return Math.min(Math.max(delayMs, MIN_UPDATE_INTERVAL_MS), MAX_UPDATE_INTERVAL_MS);
    }

    private static long normalizeTime(long timeMs) {
        return timeMs == C.TIME_UNSET ? 0 : Math.max(0, timeMs);
    }
}
