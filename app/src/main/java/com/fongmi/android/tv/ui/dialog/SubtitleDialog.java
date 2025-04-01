import android.util.TypedValue;

// 修正 initView 方法
@Override
protected void initView() {
    int count = binding.getRoot().getChildCount();
    if (full)
        for (int i = 0; i < count; i++)
            ((ImageView) binding.getRoot().getChildAt(i)).getDrawable().setTint(MDColor.WHITE);

    float savedPadding = Setting.getSubtitleBottomPadding() / 1000.0f;
    bottomPaddingFraction = savedPadding;
    subtitleView.setBottomPaddingFraction(bottomPaddingFraction);

    float savedTextSize = Setting.getSubtitleTextSize() / 1000.0f;
    textSize = savedTextSize;
    subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
}

// 修正 onLarge 和 onSmall 方法
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