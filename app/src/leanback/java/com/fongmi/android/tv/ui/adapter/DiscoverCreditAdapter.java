package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.DiscoverCredit;
import com.fongmi.android.tv.databinding.AdapterDiscoverCreditBinding;
import com.fongmi.android.tv.utils.ImgUtil;

import java.util.ArrayList;
import java.util.List;

public class DiscoverCreditAdapter extends RecyclerView.Adapter<DiscoverCreditAdapter.ViewHolder> {

    private final List<DiscoverCredit> items = new ArrayList<>();

    public void setItems(List<DiscoverCredit> values) {
        items.clear();
        if (values != null) items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterDiscoverCreditBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoverCredit item = items.get(position);
        holder.binding.name.setText(item.getName());
        holder.binding.character.setText(item.getCharacter());
        holder.binding.character.setVisibility(item.getCharacter().isEmpty() ? View.GONE : View.VISIBLE);
        ImgUtil.load(item.getName(), item.getProfile(), holder.binding.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AdapterDiscoverCreditBinding binding;

        private ViewHolder(AdapterDiscoverCreditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
