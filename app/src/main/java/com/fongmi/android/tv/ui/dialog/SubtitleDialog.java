package com.fongmi.android.tv.ui.dialog;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.ui.SubtitleView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.DialogSubtitleBinding;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.bassaer.library.MDColor;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class SubtitleDialog extends BaseDialog {

    private DialogSubtitleBinding binding;
    private SubtitleView subtitleView;
    private boolean full;

    private float bottomPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;
    private float textSize = 1.0f; // 初始大小，可以根据需求调整
    private static final float PADDING_INCREMENT = 0.005f;
    private static final float TEXT_SIZE_INCREMENT = 0.002f;

    public static SubtitleDialog create() {
        return new SubtitleDialog();
    }

    public SubtitleDialog view(SubtitleView subtitleView) {
        this.subtitleView = subtitleView;
        return this;
    }

    public SubtitleDialog full(boolean full) {
        this.full = full;
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments())
            if (f instanceof BottomSheetDialogFragment) return;
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected boolean transparent() {
        return full;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogSubtitleBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        int count = binding.getRoot().getChildCount();
        if (full)
            for (int i = 0; i < count; i++)
                ((ImageView) binding.getRoot().getChildAt(i)).getDrawable().setTint(MDColor.WHITE);

        // 获取并设置字幕的底部间距
        float savedPadding = Setting.getSubtitleBottomPadding() / 1000.0f;
        bottomPaddingFraction = savedPadding;
        subtitleView.setBottomPaddingFraction(bottomPaddingFraction);

        // 获取并设置字幕的文字大小
        float savedTextSize = Setting.getSubtitleTextSize() / 1000.0f;
        textSize = savedTextSize;
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    @Override
    protected void initEvent() {
        binding.up.setOnClickListener(this::onUp);
        binding.down.setOnClickListener(this::onDown);
        binding.large.setOnClickListener(this::onLarge);
        binding.small.setOnClickListener(this::onSmall);
        binding.reset.setOnClickListener(this::onReset);
    }

    private void onUp(View view) {
        bottomPaddingFraction += PADDING_INCREMENT;
        subtitleView.setBottomPaddingFraction(bottomPaddingFraction);
        Setting.putSubtitleBottomPadding((int) (bottomPaddingFraction * 1000));
    }

    private void onDown(View view) {
        bottomPaddingFraction -= PADDING_INCREMENT;
        if (bottomPaddingFraction < 0) bottomPaddingFraction = 0;
        subtitleView.setBottomPaddingFraction(bottomPaddingFraction);
        Setting.putSubtitleBottomPadding((int) (bottomPaddingFraction * 1000));
    }

    private void onLarge(View view) {
        textSize += TEXT_SIZE_INCREMENT;
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        Setting.putSubtitleTextSize((int) (textSize * 1000));
    }

    private void onSmall(View view) {
        textSize -= TEXT_SIZE_INCREMENT;
        textSize = Math.max(textSize, 0.5f); // 确保最小值为 0.5
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        Setting.putSubtitleTextSize((int) (textSize * 1000));
    }

    private void onReset(View view) {
        Setting.putSubtitleTextSize(0);
        Setting.putSubtitleBottomPadding(0);
        subtitleView.setUserDefaultTextSize();
        subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION);
        bottomPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;
        textSize = 1.0f;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (full) setDimAmount(0.5f);
        getDialog().getWindow().setLayout(ResUtil.dp2px(248), -1);
    }
}