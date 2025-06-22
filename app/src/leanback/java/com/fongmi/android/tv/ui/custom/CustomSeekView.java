package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;

public class CustomSeekView extends View {
    
    private Paint progressPaint;
    private Paint bufferedPaint;
    private Paint backgroundPaint;
    private Paint thumbPaint;
    
    private float progress = 0f;
    private float bufferedProgress = 0f;
    
    private int progressHeight;
    private int thumbRadius;
    
    private Player player;
    private boolean isSeeking = false;
    private long seekPosition = 0;
    
    public CustomSeekView(Context context) {
        super(context);
        init();
    }
    
    public CustomSeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        progressHeight = dp2px(4);
        thumbRadius = dp2px(8);
        
        // 背景画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0x33FFFFFF);
        
        // 缓冲进度画笔
        bufferedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bufferedPaint.setColor(0x66FFFFFF);
        
        // 播放进度画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // 滑块画笔
        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(0xFFFFFFFF);
        
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;
        
        int startX = thumbRadius;
        int endX = width - thumbRadius;
        int barWidth = endX - startX;
        
        // 1. 绘制背景轨道
        RectF bgRect = new RectF(
            startX,
            centerY - progressHeight / 2f,
            endX,
            centerY + progressHeight / 2f
        );
        canvas.drawRoundRect(bgRect, progressHeight / 2f, progressHeight / 2f, backgroundPaint);
        
        // 2. 绘制缓冲进度
        if (bufferedProgress > 0) {
            float bufferedEnd = startX + barWidth * bufferedProgress;
            RectF bufferedRect = new RectF(
                startX,
                centerY - progressHeight / 2f,
                bufferedEnd,
                centerY + progressHeight / 2f
            );
            canvas.drawRoundRect(bufferedRect, progressHeight / 2f, progressHeight / 2f, bufferedPaint);
        }
        
        // 3. 绘制播放进度（渐变色）
        if (progress > 0) {
            float progressEnd = startX + barWidth * progress;
            
            // 设置渐变
            LinearGradient gradient = new LinearGradient(
                startX, 0, progressEnd, 0,
                new int[]{0xFF2196F3, 0xFF1976D2},
                null,
                Shader.TileMode.CLAMP
            );
            progressPaint.setShader(gradient);
            
            RectF progressRect = new RectF(
                startX,
                centerY - progressHeight / 2f,
                progressEnd,
                centerY + progressHeight / 2f
            );
            canvas.drawRoundRect(progressRect, progressHeight / 2f, progressHeight / 2f, progressPaint);
        }
        
        // 4. 绘制滑块
        float thumbX = startX + barWidth * progress;
        
        // 滑块阴影
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x40000000);
        canvas.drawCircle(thumbX, centerY + dp2px(1), thumbRadius + dp2px(2), shadowPaint);
        
        // 滑块主体
        canvas.drawCircle(thumbX, centerY, thumbRadius, thumbPaint);
        
        // 获得焦点时的外圈
        if (isFocused()) {
            Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            focusPaint.setColor(0x40FFFFFF);
            canvas.drawCircle(thumbX, centerY, thumbRadius + dp2px(4), focusPaint);
        }
    }
    
    // 设置播放器
    public void setListener(Player player) {
        this.player = player;
    }
    
    // 更新进度
    public void updateProgress() {
        if (player != null && !isSeeking) {
            long duration = player.getDuration();
            long position = player.getCurrentPosition();
            long buffered = player.getBufferedPosition();
            
            if (duration > 0) {
                progress = (float) position / duration;
                bufferedProgress = (float) buffered / duration;
                invalidate();
            }
        }
    }
    
    // 按键处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (player == null) return super.onKeyDown(keyCode, event);
        
        long duration = player.getDuration();
        if (duration <= 0) return super.onKeyDown(keyCode, event);
        
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                startSeek();
                seekPosition = Math.max(0, player.getCurrentPosition() - 10000);
                progress = (float) seekPosition / duration;
                invalidate();
                return true;
                
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                startSeek();
                seekPosition = Math.min(duration, player.getCurrentPosition() + 10000);
                progress = (float) seekPosition / duration;
                invalidate();
                return true;
                
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isSeeking) {
                    endSeek();
                }
                return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void startSeek() {
        isSeeking = true;
        if (player != null) {
            seekPosition = player.getCurrentPosition();
        }
    }
    
    private void endSeek() {
        if (isSeeking && player != null) {
            player.seekTo(seekPosition);
            isSeeking = false;
        }
    }
    
    private int dp2px(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultHeight = dp2px(48);
        int height = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY
            ? MeasureSpec.getSize(heightMeasureSpec)
            : defaultHeight;
        
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}