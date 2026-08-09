package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.DiscoverFilterOption;
import com.fongmi.android.tv.bean.DiscoverFilterPanel;
import com.fongmi.android.tv.databinding.AdapterDiscoverFilterPanelBinding;
import com.fongmi.android.tv.ui.custom.JetStreamChipRow;

import java.util.ArrayList;
import java.util.List;

public final class DiscoverFilterPanelPresenter extends Presenter {

    public interface Listener {
        void onFilterClick(int row, DiscoverFilterOption option);
    }

    private final Listener listener;

    public DiscoverFilterPanelPresenter(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverFilterPanelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object object) {
        ((Holder) viewHolder).bind((DiscoverFilterPanel) object, listener);
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        ((Holder) viewHolder).unbind();
    }

    private static final class Holder extends ViewHolder {

        private final JetStreamChipRow[] rows;

        private Holder(AdapterDiscoverFilterPanelBinding binding) {
            super(binding.getRoot());
            rows = new JetStreamChipRow[]{binding.media, binding.genre, binding.region, binding.year, binding.sort};
        }

        private void bind(DiscoverFilterPanel panel, Listener listener) {
            for (int row = 0; row < rows.length; row++) bindRow(rows[row], row, panel.getRow(row), listener);
            for (int row = 0; row < rows.length; row++) {
                rows[row].setNextFocusLeftId(rows[row].getId());
                rows[row].setNextFocusRightId(rows[row].getId());
                if (row > 0) rows[row].setNextFocusUp(rows[row - 1].getId());
                if (row < rows.length - 1) rows[row].setNextFocusDown(rows[row + 1].getId());
            }
        }

        private void bindRow(JetStreamChipRow chipRow, int row, List<DiscoverFilterOption> options, Listener listener) {
            List<String> labels = new ArrayList<>();
            int selected = -1;
            for (int i = 0; i < options.size(); i++) {
                labels.add(options.get(i).getLabel());
                if (options.get(i).isSelected()) selected = i;
            }
            int focused = chipRow.getFocusedPosition();
            chipRow.setItems(labels, selected);
            if (focused >= 0) chipRow.setFocusedPosition(Math.min(focused, Math.max(0, labels.size() - 1)));
            chipRow.setOnChipClickListener(position -> {
                if (position >= 0 && position < options.size()) listener.onFilterClick(row, options.get(position));
            });
        }

        private void unbind() {
            for (JetStreamChipRow row : rows) row.clearListeners();
        }
    }
}
