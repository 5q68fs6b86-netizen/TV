package com.fongmi.android.tv.ui.dialog;

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

    // --- Constants for Padding Fraction ---
    private static final float PADDING_STEP = 0.01f; // 调整底部边距的步长（占视图高度的比例）
    private static final float MIN_PADDING_FRACTION = 0.0f;  // 最小边距比例
    private static final float MAX_PADDING_FRACTION = 0.5f;  // 最大边距比例

    private static final String TAG = "SubtitleDialog";

    private DialogSubtitleBinding binding;
    private SubtitleView subtitleView;
    private boolean full;

    // 当前设置的值
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
        applySettings(); // 应用保存的边距设置

        // --- 移除或禁用字体大小调整按钮 ---
        binding.large.setVisibility(View.GONE); // 隐藏增大按钮
        binding.small.setVisibility(View.GONE); // 隐藏减小按钮

        if (full) {
            int white = MDColor.WHITE;
            setTint(binding.up, white);
            setTint(binding.down, white);
            // setTint(binding.large, white); // 不再需要
            // setTint(binding.small, white); // 不再需要
            setTint(binding.reset, white);
        }
    }

    private void setTint(ImageView imageView, int color) {
        if (imageView != null && imageView.getDrawable() != null) {
            imageView.getDrawable().mutate().setTint(color);
        }
    }

    // 应用保存的设置到 SubtitleView (只处理边距)
    private void applySettings() {
        if (subtitleView == null) return;

        // 获取并应用底部边距设置
        currentPaddingFraction = Setting.getSubtitleBottomPadding();
        // 使用一个不太可能由用户设置的值（比如-1）来判断是否是“未设置”或“使用默认”
        if (currentPaddingFraction < MIN_PADDING_FRACTION || currentPaddingFraction > MAX_PADDING_FRACTION ) {
             currentPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;
        }
        // 确保加载的值在合理范围内（这一步可能不需要了，如果上面已经设为默认）
        // currentPaddingFraction = Math.max(MIN_PADDING_FRACTION, Math.min(MAX_PADDING_FRACTION, currentPaddingFraction));
        subtitleView.setBottomPaddingFraction(currentPaddingFraction);

        // 字体大小由系统或默认值决定，不由我们控制
        // 可以考虑调用一次 setUserDefaultTextSize() 确保应用系统设置，但这取决于产品需求
        // subtitleView.setUserDefaultTextSize();
    }

    @Override
    protected void initEvent() {
        binding.up.setOnClickListener(this::onUp);
        binding.down.setOnClickListener(this::onDown);
        // --- 移除字体大小按钮的监听器 ---
        // binding.large.setOnClickListener(this::onLarge);
        // binding.small.setOnClickListener(this::onSmall);
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

    // --- onLarge 和 onSmall 方法已移除 ---
    // private void onLarge(View view) { ... }
    // private void onSmall(View view) { ... }

    // 重置设置 (现在只重置位置)
    private void onReset(View view) {
        if (subtitleView == null) return;
        // 将 Setting 中的边距值设为 0 或其他表示默认的值
        Setting.putSubtitleBottomPadding(0);
        // --- 不再需要清除字体大小设置 ---
        // Setting.putSubtitleTextSize(0);

        // 更新内部状态为默认边距值
        currentPaddingFraction = SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION;

        // 应用到 View (重置边距)
        subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION);

        // 调用 setUserDefaultTextSize() 是可选的，取决于“重置”是否也意味着字体大小恢复系统默认
        // subtitleView.setUserDefaultTextSize();
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