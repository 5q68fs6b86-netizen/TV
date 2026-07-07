package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.DialogRestoreBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.ui.adapter.RestoreAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class RestoreDialog extends BaseAlertDialog implements RestoreAdapter.OnClickListener {

    private DialogRestoreBinding binding;
    private RestoreAdapter adapter;
    private Callback callback;

    public static RestoreDialog create() {
        return new RestoreDialog();
    }

    public RestoreDialog callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void show(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogRestoreBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        adapter = new RestoreAdapter(this);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(false);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 16));
        requestFocus(0, View.NO_ID);
    }

    @Override
    public void onItemClick(File item) {
        AppDatabase.restore(item, callback);
        dismiss();
    }

    @Override
    public void onDeleteClick(File item) {
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
                if (holder == null) return;
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
