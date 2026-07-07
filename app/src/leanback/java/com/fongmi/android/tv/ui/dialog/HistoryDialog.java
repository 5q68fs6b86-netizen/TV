package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.DialogHistoryBinding;
import com.fongmi.android.tv.impl.ConfigListener;
import com.fongmi.android.tv.ui.adapter.ConfigAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HistoryDialog extends BaseAlertDialog implements ConfigAdapter.OnClickListener {

    private DialogHistoryBinding binding;
    private ConfigAdapter adapter;
    private boolean readOnly;
    private int type;

    public static HistoryDialog create() {
        return new HistoryDialog();
    }

    public HistoryDialog vod() {
        type = 0;
        return this;
    }

    public HistoryDialog live() {
        type = 1;
        return this;
    }

    public HistoryDialog wall() {
        type = 2;
        return this;
    }

    public HistoryDialog readOnly() {
        readOnly = true;
        return this;
    }

    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogHistoryBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        adapter = new ConfigAdapter(this);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(false);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 16));
        binding.recycler.setAdapter(adapter.readOnly(readOnly).addAll(type));
        requestFocus(0, View.NO_ID);
    }

    @Override
    public void onTextClick(Config item) {
        ((ConfigListener) requireActivity()).setConfig(item);
        dismiss();
    }

    @Override
    public void onDeleteClick(Config item) {
        int position = adapter.indexOf(item);
        int focusId = getFocusId();
        int count = adapter.remove(item);
        if (count == 0) dismiss();
        else if (position >= 0) requestFocus(Math.min(position, count - 1), focusId);
    }

    private int getFocusId() {
        View focus = getDialog() == null ? null : getDialog().getCurrentFocus();
        return focus == null ? View.NO_ID : focus.getId();
    }

    private void requestFocus(int position, int focusId) {
        binding.recycler.post(() -> {
            if (position < 0 || position >= adapter.getItemCount()) return;
            binding.recycler.scrollToPosition(position);
            binding.recycler.postDelayed(() -> {
                RecyclerView.ViewHolder holder = binding.recycler.findViewHolderForAdapterPosition(position);
                if (holder == null) {
                    if (canRequestFocus(binding.recycler)) binding.recycler.requestFocus();
                    return;
                }
                View target = focusId == View.NO_ID ? null : holder.itemView.findViewById(focusId);
                if (canRequestFocus(target) && target.requestFocus()) return;
                target = findFocusable(holder.itemView);
                if (target != null && target.requestFocus()) return;
                if (canRequestFocus(binding.recycler)) binding.recycler.requestFocus();
            }, 50);
        });
    }

    private View findFocusable(View view) {
        if (!canRequestFocus(view)) return null;
        if (view.isFocusable()) return view;
        if (!(view instanceof ViewGroup)) return null;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            View target = findFocusable(group.getChildAt(i));
            if (target != null) return target;
        }
        return null;
    }

    private boolean canRequestFocus(View view) {
        return view != null && view.isShown() && view.isEnabled();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() == 0) dismiss();
        else setWidth(0.4f);
    }
}
