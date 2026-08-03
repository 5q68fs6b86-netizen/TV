package com.fongmi.android.tv.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.DoubanDetail;
import com.fongmi.android.tv.databinding.DialogDiscoverBinding;
import com.fongmi.android.tv.ui.activity.CollectActivity;
import com.fongmi.android.tv.utils.ImgUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DiscoverDialog extends BaseAlertDialog {

    private static final String ITEM = "item";

    private DialogDiscoverBinding binding;
    private DoubanDetail item;

    public static DiscoverDialog create(DoubanDetail item) {
        DiscoverDialog dialog = new DiscoverDialog();
        Bundle args = new Bundle();
        args.putParcelable(ITEM, item);
        dialog.setArguments(args);
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
        item = getArguments() == null ? null : getArguments().getParcelable(ITEM);
        if (item == null) {
            dismissAllowingStateLoss();
            return;
        }
        ImgUtil.load(item.getTitle(), item.getPoster(), binding.poster);
        binding.poster.setContentDescription(item.getTitle());
        binding.name.setText(item.getTitle());
        bindPill(binding.year, item.getYear());
        bindPill(binding.rating, item.getRating());
        bindPill(binding.type, item.getGenres());
        bindPill(binding.region, item.getRegion());
        bindPill(binding.duration, item.getDuration());
        bindLine(binding.directors, R.string.discover_douban_directors, item.getDirectors());
        bindLine(binding.actors, R.string.discover_douban_actors, item.getActors());
        binding.content.setText(item.getComment());
        binding.content.setVisibility(TextUtils.isEmpty(item.getComment()) ? View.GONE : View.VISIBLE);
        binding.search.requestFocus();
    }

    @Override
    protected void initEvent() {
        binding.search.setOnClickListener(this::onSearch);
        binding.cancel.setOnClickListener(view -> dismiss());
    }

    private void bindPill(android.widget.TextView view, String text) {
        view.setText(text);
        view.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private void bindLine(android.widget.TextView view, int label, String text) {
        view.setText(TextUtils.isEmpty(text) ? "" : getString(label, text));
        view.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private void onSearch(View view) {
        FragmentActivity activity = getActivity();
        dismiss();
        if (activity != null) CollectActivity.start(activity, item.getTitle());
    }

    @Override
    public void onStart() {
        super.onStart();
        setWidth(0.72f);
    }
}
