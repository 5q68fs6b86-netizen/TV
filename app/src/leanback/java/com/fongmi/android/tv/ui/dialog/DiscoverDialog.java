package com.fongmi.android.tv.ui.dialog;

import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.DialogDiscoverBinding;
import com.fongmi.android.tv.ui.activity.CollectActivity;
import com.fongmi.android.tv.utils.ImgUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class DiscoverDialog extends BaseAlertDialog {

    private DialogDiscoverBinding binding;
    private Vod item;

    public static DiscoverDialog create(Vod item) {
        DiscoverDialog dialog = new DiscoverDialog();
        dialog.item = item;
        return dialog;
    }

    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogDiscoverBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        if (item == null) {
            dismissAllowingStateLoss();
            return;
        }
        ImgUtil.load(item.getName(), item.getPic(), binding.poster);
        binding.poster.setContentDescription(item.getName());
        binding.name.setText(item.getName());
        binding.meta.setText(getMeta());
        binding.meta.setVisibility(TextUtils.isEmpty(binding.meta.getText()) ? View.GONE : View.VISIBLE);
        binding.content.setText(item.getContent());
        binding.content.setVisibility(TextUtils.isEmpty(item.getContent()) ? View.GONE : View.VISIBLE);
        binding.search.requestFocus();
    }

    @Override
    protected void initEvent() {
        binding.search.setOnClickListener(this::onSearch);
        binding.cancel.setOnClickListener(view -> dismiss());
    }

    private String getMeta() {
        List<String> values = new ArrayList<>();
        if (!TextUtils.isEmpty(item.getYear())) values.add(item.getYear());
        if (!TextUtils.isEmpty(item.getRemarks())) values.add(item.getRemarks());
        return TextUtils.join("  ·  ", values);
    }

    private void onSearch(View view) {
        FragmentActivity activity = getActivity();
        dismiss();
        if (activity != null) CollectActivity.start(activity, item.getName());
    }
}
