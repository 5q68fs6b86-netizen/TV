package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.DiscoverFilterOption;
import com.fongmi.android.tv.databinding.AdapterDiscoverFilterBinding;

public class DiscoverFilterPresenter extends Presenter {

    public interface Listener {
        void onFilterClick(int row, DiscoverFilterOption option, android.view.View view);
    }

    private final int row;
    private final Listener listener;

    public DiscoverFilterPresenter(int row, Listener listener) {
        this.row = row;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object object) {
        Holder holder = (Holder) viewHolder;
        DiscoverFilterOption option = (DiscoverFilterOption) object;
        holder.binding.chip.setText(option.getLabel());
        holder.binding.chip.setChecked(option.isSelected());
        holder.binding.chip.setSelected(option.isSelected());
        holder.binding.chip.setOnClickListener(view -> listener.onFilterClick(row, option, view));
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        ((Holder) viewHolder).binding.chip.setOnClickListener(null);
    }

    private static final class Holder extends ViewHolder {
        private final AdapterDiscoverFilterBinding binding;

        private Holder(AdapterDiscoverFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
