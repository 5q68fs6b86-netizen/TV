package com.fongmi.android.tv.ui.dialog;

import android.util.TypedValue; // Import TypedValue
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.SubtitleView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.DialogSubtitleBinding;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.bassaer.library.MDColor;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

@UnstableApi
public final class SubtitleDialog extends BaseDialog {

    // --- Constants for Text Size in SP ---
    private static final float DEFAULT_TEXT_SIZE_SP = 16f; // 默认字体大小 (SP)
    private static final float TEXT_SIZE_STEP_SP = 1f;   // 调整字体大小的步长 (SP)
    private static final float MIN_TEXT_SIZE_SP = 10f;  // 最小字体大小 (SP)
    private static final float MAX_TEXT_SIZE_SP = 30f;  // 最大字体大小 (SP)
    // --- Constants for Padding Fraction ---
    private static final float PADDING_STEP = 0.01f; // 调整底部边距的步长（占视图高度的比例）
    private static final float MIN_PADDING_FRACTION = 0.0f;  // 最小边距比例
    private static final float MAX_PADDING_FRACTION = 0.5f;  // 最大边距比例

    private static final String TAG = "SubtitleDialog";

    private DialogSubtitleBinding binding;
    private SubtitleView subtitleView;
    private boolean full;

    // 当前设置的值
    private float currentTextSizeSp; // 当前字体大小 (SP)
    private float currentPaddingFraction; // 当前底部边距比例

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
        if (activity.getSupportFragmentManager().findFragmentByTag(TAG) != null) {
            return;
        }
        if (this.subtitleView == null) {
            // Log.e(TAG, "SubtitleView is null, cannot show dialog.");
            return;
        }
        show(activity.getSupportFragmentManager(), TAG);
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
        applySettings(); // 应用保存的设置

        if (full) {
            int white = MDColor.WHITE;
            setTint(binding.up, white);
            setTint(binding.down, white);
            setTint(binding.large, white);
            setTint(binding.small, white);
            setTint(binding.reset, white);
        }
    }

    private void setTint(ImageView imageView, int color) {
        if (imageView != null && imageView.getDrawable() != null) {
            imageView.getDrawable().mutate().setTint(color);
        }
    }

    // 应用保存的设置到 SubtitleView
    private void applySettings() {
        if (subtitleView == null) return;

        // 获取并应用字体大小设置 (现在是 SP 值)
        // *** 重要: 假设 Setting.getSubtitleTextSize() 现在返回 SP 值 ***
        // *** 如果它返回的是旧的比例值，需要在这里做转换或修改 Setting 类 ***
        currentTextSizeSp = Setting.getSubtitleTextSize();
        if (currentTextSizeSp <= 0) { // 0 或无效值表示使用默认
            currentTextSizeSp = DEFAULT_TEXT_SIZE_SP;
            subtitleView.setUserDefaultTextSize(); // 让 View 使用其内部默认值
        } else {
            // 确保加载的值在合理范围内
            currentTextSizeSp = Math.max(MIN_TEXT_SIZE_SP, Math.min(MAX_TEXT_SIZE_SP, currentTextSizeSp));
            // 使用 setTextSize 设置 SP 值
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSizeSp);
        }

        // 获取并应用底部边距设置 (保持比例方式)
        currentPaddingFraction = Setting.getSubtitleBottomPadding();
        if (currentPaddingFraction < MIN_PADDING_FRACTION || currentPaddingFraction > MAX_PADDING_FRACTION ) { // 处理无效值或表示默认的值(如0或负数)
             currentPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;
        }
        // 确保加载的值在合理范围内
        currentPaddingFraction = Math.max(MIN_PADDING_FRACTION, Math.min(MAX_PADDING_FRACTION, currentPaddingFraction));
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);
    }

    @Override
    protected void initEvent() {
        binding.up.setOnClickListener(this::onUp);
        binding.down.setOnClickListener(this::onDown);
        binding.large.setOnClickListener(this::onLarge);
        binding.small.setOnClickListener(this::onSmall);
        binding.reset.setOnClickListener(this::onReset);
    }

    // 向上移动字幕 (减少底部边距)
    private void onUp(View view) {
        if (subtitleView == null) return;
        currentPaddingFraction = Math.max(MIN_PADDING_FRACTION, currentPaddingFraction - PADDING_STEP);
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);
        Setting.putSubtitleBottomPadding(currentPaddingFraction);
    }

    // 向下移动字幕 (增加底部边距)
    private void onDown(View view) {
        if (subtitleView == null) return;
        currentPaddingFraction = Math.min(MAX_PADDING_FRACTION, currentPaddingFraction + PADDING_STEP);
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);
        Setting.putSubtitleBottomPadding(currentPaddingFraction);
    }

    // 增大字体 (使用 SP)
    private void onLarge(View view) {
        if (subtitleView == null) return;
        // 基于当前 SP 值计算新值，并限制范围
        currentTextSizeSp = Math.min(MAX_TEXT_SIZE_SP, currentTextSizeSp + TEXT_SIZE_STEP_SP);
        // 应用新 SP 值
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSizeSp);
        // 保存新的 SP 值到 Setting
        // *** 重要: 确保 Setting.putSubtitleTextSize 可以接收 SP 值 ***
        Setting.putSubtitleTextSize(currentTextSizeSp);
    }

    // 减小字体 (使用 SP)
    private void onSmall(View view) {
        if (subtitleView == null) return;
        // 基于当前 SP 值计算新值，并限制范围
        currentTextSizeSp = Math.max(MIN_TEXT_SIZE_SP, currentTextSizeSp - TEXT_SIZE_STEP_SP);
        // 应用新 SP 值
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSizeSp);
        // 保存新的 SP 值到 Setting
        // *** 重要: 确保 Setting.putSubtitleTextSize 可以接收 SP 值 ***
        Setting.putSubtitleTextSize(currentTextSizeSp);
    }

    // 重置设置
    private void onReset(View view) {
        if (subtitleView == null) return;
        // 将 Setting 设为 0 表示默认
        Setting.putSubtitleTextSize(0);
        Setting.putSubtitleBottomPadding(0); // 或者保存 SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION

        // 更新内部状态为默认值
        currentTextSizeSp = DEFAULT_TEXT_SIZE_SP;
        currentPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;

        // 应用到 View
        subtitleView.setUserDefaultTextSize(); // 重置大小
        subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION); // 重置边距
    }

    @Override
    public void onResume() {
        super.onResume();
        if (full) setDimAmount(0.5f);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ResUtil.dp2px(248), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subtitleView = null;
        binding = null;
    }
}