package com.fongmi.android.tv.ui.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

final class DialogFocus {

    private DialogFocus() {
    }

    static void requestRecyclerFocus(RecyclerView recycler, int position, int count) {
        if (recycler == null || count <= 0) return;
        int target = Math.max(0, Math.min(position, count - 1));
        recycler.post(() -> {
            recycler.scrollToPosition(target);
            recycler.postDelayed(() -> {
                RecyclerView.Adapter<?> adapter = recycler.getAdapter();
                int size = adapter == null ? count : adapter.getItemCount();
                if (size <= 0) return;
                int safeTarget = Math.max(0, Math.min(target, size - 1));
                RecyclerView.ViewHolder holder = recycler.findViewHolderForAdapterPosition(safeTarget);
                View focus = holder == null ? null : findFocusable(holder.itemView);
                if (focus != null && focus.requestFocus()) return;
                if (canRequestFocus(recycler)) recycler.requestFocus();
            }, 50);
        });
    }

    private static View findFocusable(View view) {
        if (!canRequestFocus(view)) return null;
        if (view.isFocusable()) return view;
        if (!(view instanceof ViewGroup group)) return null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View target = findFocusable(group.getChildAt(i));
            if (target != null) return target;
        }
        return null;
    }

    private static boolean canRequestFocus(View view) {
        return view != null && view.isShown() && view.isEnabled();
    }
}
