package com.fongmi.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogUaBinding;
import com.fongmi.android.tv.impl.DanmakuListener;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DanmakuApiDialog extends BaseAlertDialog {

    private DialogUaBinding binding;
    private boolean logvar;

    public static void show(Fragment fragment) {
        new DanmakuApiDialog().show(fragment.getChildFragmentManager(), null);
    }

    public static void showLogvar(Fragment fragment) {
        DanmakuApiDialog dialog = new DanmakuApiDialog();
        dialog.logvar = true;
        dialog.show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogUaBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setTitle(logvar ? R.string.danmaku_logvar_api : R.string.danmaku_api).setView(getBinding().getRoot()).setPositiveButton(R.string.dialog_positive, this::onPositive).setNegativeButton(R.string.dialog_negative, null);
    }

    @Override
    protected void initView() {
        String text;
        binding.text.setText(text = logvar ? DanmakuSetting.getEffectiveLogvarUrl() : DanmakuSetting.getEffectiveApiUrl());
        binding.text.setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
    }

    @Override
    protected void initEvent() {
        binding.text.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) onPositive(null, 0);
            return true;
        });
    }

    private void onPositive(DialogInterface dialog, int which) {
        if (logvar) ((DanmakuListener) requireParentFragment()).setLogvarApi(binding.text.getText().toString().trim());
        else ((DanmakuListener) requireParentFragment()).setDanmakuApi(binding.text.getText().toString().trim());
        dismiss();
    }
}
