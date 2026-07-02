package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.slider.Slider;

final class SliderFocusHelper {

    private SliderFocusHelper() {
    }

    static void bindLabelRow(Slider slider) {
        View row = findLabelRow(slider);
        if (row == null) return;
        row.setSelected(slider.hasFocus());
        slider.setOnFocusChangeListener((view, focused) -> row.setSelected(focused));
    }

    private static View findLabelRow(Slider slider) {
        if (!(slider.getParent() instanceof ViewGroup parent)) return null;
        int index = parent.indexOfChild(slider);
        return index > 0 ? parent.getChildAt(index - 1) : null;
    }
}
