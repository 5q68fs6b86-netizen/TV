package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.player.Players;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.BaseOnChangeListener;
import com.google.android.material.slider.BaseOnSliderTouchListener;

public class CustomSeekView extends FrameLayout {

    private static final int MAX_UPDATE_INTERVAL_MS = 1000;
    private static final int MIN_UPDATE_INTERVAL_MS = 200;

    private TextView positionView;
    private TextView durationView;
    private Slider timeBar;

    private Runnable refresh;
    private Players player;

    private long currentDuration;
    private long currentPosition;
    private boolean scrubbing;

    public CustomSeekView(Context context) {
        this(context, null);
    }

    public CustomSeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_control_seek, this);
        init();
        start();
    }

    private void init() {
        positionView = findViewById(R.id.position);
        durationView = findViewById(R.id.duration);
        timeBar = findViewById(R.id.timeBar);
        refresh = this::refresh;

        timeBar.addOnChangeListener(new BaseOnChangeListener<Slider>() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    positionView.setText(player.stringToTime((long) value));
                }
            }
        });

        timeBar.addOnSliderTouchListener(new BaseOnSliderTouchListener<Slider>() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                scrubbing = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                scrubbing = false;
                seekToTimeBarPosition((long) slider.getValue());
            }
        });
    }

    public void setListener(Players player) {
        this.player = player;
    }

    private void start() {
        removeCallbacks(refresh);
        post(refresh);
    }

    private void refresh() {
        if (player == null || player.isRelease()) return;
        long duration = player.getDuration();
        long position = player.getPosition();
        boolean positionChanged = position != currentPosition;
        boolean durationChanged = duration != currentDuration;
        currentDuration = duration;
        currentPosition = position;
        if (durationChanged) {
            timeBar.setValueTo(duration);
            durationView.setText(player.stringToTime(duration < 0 ? 0 : duration));
        }
        if (positionChanged && !scrubbing) {
            timeBar.setValue(position);
            positionView.setText(player.stringToTime(position < 0 ? 0 : position));
        }
        if (player.isEmpty()) {
            positionView.setText("00:00");
            durationView.setText("00:00");
            timeBar.setValue(0);
            timeBar.setValueTo(0);
        }
        removeCallbacks(refresh);
        if (player.isPlaying()) {
            postDelayed(refresh, 1000 - position % 1000);
        } else {
            postDelayed(refresh, MAX_UPDATE_INTERVAL_MS);
        }
    }

    private void seekToTimeBarPosition(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
            refresh();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(refresh);
    }
}
