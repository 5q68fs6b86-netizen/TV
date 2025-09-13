package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.databinding.AdapterFlagBinding;

import java.util.List;

public class FlagAdapter extends RecyclerView.Adapter<FlagAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final List<Flag> mItems;

    public FlagAdapter(OnClickListener listener, List<Flag> items) {
        this.mListener = listener;
        this.mItems = items;
    }

    public interface OnClickListener {
        void onItemClick(Flag item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterFlagBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flag item = mItems.get(position);
        holder.binding.text.setText(item.getFlag());
        holder.binding.text.setActivated(item.isActivated());
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(item));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterFlagBinding binding;

        public ViewHolder(@NonNull AdapterFlagBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}