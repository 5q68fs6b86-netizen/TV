package com.fongmi.android.tv.ui.dialog;

import android.util.TypedValue;
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

@UnstableApi // 使用了 media3 不稳定 API，标记一下
public final class SubtitleDialog extends BaseDialog {

    // 定义调整步长常量
    private static final float TEXT_SIZE_STEP = 0.01f; // 调整字体大小的步长（占视图高度的比例）
    private static final float PADDING_STEP = 0.01f; // 调整底部边距的步长（占视图高度的比例）
    // 定义字体大小和边距的限制
    private static final float MIN_TEXT_SIZE_FRACTION = 0.02f; // 最小字体大小比例
    private static final float MAX_TEXT_SIZE_FRACTION = 0.15f; // 最大字体大小比例
    private static final float MIN_PADDING_FRACTION = 0.0f;  // 最小边距比例
    private static final float MAX_PADDING_FRACTION = 0.5f;  // 最大边距比例
    // 定义一个用户默认的字体大小比例，当 Setting 中为 0 时使用
    // SubtitleView.DEFAULT_TEXT_SIZE_FRACTION 不可见，我们自己定义一个接近的值
    private static final float USER_DEFAULT_TEXT_SIZE_FRACTION = 0.0533f;
    private static final String TAG = "SubtitleDialog"; // 用于防止重复显示的 Tag

    private DialogSubtitleBinding binding;
    private SubtitleView subtitleView;
    private boolean full;

    // 当前设置的比例值，用于增量计算
    private float currentTextSizeFraction;
    private float currentPaddingFraction;

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
        // 使用 TAG 防止重复显示同一个 Dialog
        if (activity.getSupportFragmentManager().findFragmentByTag(TAG) != null) {
            return;
        }
        // 检查 subtitleView 是否已设置
        if (this.subtitleView == null) {
            // 可以选择抛出异常或记录错误日志，这里简单返回避免崩溃
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
        // 应用当前保存的设置
        applySettings();

        // 设置按钮颜色（如果需要全屏样式）
        // **重要**: 确保 XML 布局中 up, down, large, small, reset 都是 ImageView
        if (full) {
            int white = MDColor.WHITE;
            // 使用 Binding 直接访问，更安全
            setTint(binding.up, white);
            setTint(binding.down, white);
            setTint(binding.large, white);
            setTint(binding.small, white);
            setTint(binding.reset, white);
        }
    }

    // 辅助方法，安全地设置 Tint
    private void setTint(ImageView imageView, int color) {
        if (imageView != null && imageView.getDrawable() != null) {
            imageView.getDrawable().mutate().setTint(color); // 使用 mutate() 避免影响其他地方的 Drawable
        }
    }

    // 应用保存的设置到 SubtitleView
    private void applySettings() {
        if (subtitleView == null) return;

        // 获取并应用字体大小设置
        currentTextSizeFraction = Setting.getSubtitleTextSize();
        if (currentTextSizeFraction <= 0) { // 0 表示使用默认值
            subtitleView.setUserDefaultTextSize();
            // 将我们的内部状态也设置为默认值，以便增量计算
            currentTextSizeFraction = USER_DEFAULT_TEXT_SIZE_FRACTION;
        } else {
            // 确保加载的值在合理范围内
            currentTextSizeFraction = Math.max(MIN_TEXT_SIZE_FRACTION, Math.min(MAX_TEXT_SIZE_FRACTION, currentTextSizeFraction));
            subtitleView.setUserTextSizeFraction(currentTextSizeFraction);
        }

        // 获取并应用底部边距设置
        currentPaddingFraction = Setting.getSubtitleBottomPadding();
        if (currentPaddingFraction < 0) { // 使用负数或其他特殊值表示默认？这里假设 0 或负数是默认
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
        // 基于当前值计算新值，并限制范围
        currentPaddingFraction = Math.max(MIN_PADDING_FRACTION, currentPaddingFraction - PADDING_STEP);
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);
        Setting.putSubtitleBottomPadding(currentPaddingFraction);
    }

    // 向下移动字幕 (增加底部边距)
    private void onDown(View view) {
        if (subtitleView == null) return;
        // 基于当前值计算新值，并限制范围
        currentPaddingFraction = Math.min(MAX_PADDING_FRACTION, currentPaddingFraction + PADDING_STEP);
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);
        Setting.putSubtitleBottomPadding(currentPaddingFraction);
    }

    // 增大字体
    private void onLarge(View view) {
        if (subtitleView == null) return;
        // 基于当前值计算新值，并限制范围
        currentTextSizeFraction = Math.min(MAX_TEXT_SIZE_FRACTION, currentTextSizeFraction + TEXT_SIZE_STEP);
        subtitleView.setUserTextSizeFraction(currentTextSizeFraction);
        Setting.putSubtitleTextSize(currentTextSizeFraction);
    }

    // 减小字体
    private void onSmall(View view) {
        if (subtitleView == null) return;
        // 基于当前值计算新值，并限制范围
        currentTextSizeFraction = Math.max(MIN_TEXT_SIZE_FRACTION, currentTextSizeFraction - TEXT_SIZE_STEP);
        subtitleView.setUserTextSizeFraction(currentTextSizeFraction);
        Setting.putSubtitleTextSize(currentTextSizeFraction);
    }

    // 重置设置
    private void onReset(View view) {
        if (subtitleView == null) return;
        // 将设置保存为 0 或特定值表示默认
        Setting.putSubtitleTextSize(0);
        Setting.putSubtitleBottomPadding(0); // 或者保存 SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION? 取决于 Setting 如何处理 0
        // 更新内部状态
        currentTextSizeFraction = USER_DEFAULT_TEXT_SIZE_FRACTION;
        currentPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;
        // 应用到 View
        subtitleView.setUserDefaultTextSize();
        subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (full) setDimAmount(0.5f);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ResUtil.dp2px(248), ViewGroup.LayoutParams.WRAP_CONTENT); // 使用 WRAP_CONTENT 更合适
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subtitleView = null; // 解除引用
        binding = null; // ViewBinding 置空
    }
}