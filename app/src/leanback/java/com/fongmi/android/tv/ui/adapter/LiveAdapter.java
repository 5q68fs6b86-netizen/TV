package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.databinding.AdapterChannelBinding;
import com.fongmi.android.tv.databinding.AdapterGroupBinding;

import java.util.ArrayList;
import java.util.List;

public class LiveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final OnClickListener mListener;
    private final List<Object> mItems;

    public LiveAdapter(OnClickListener listener) {
        this.mListener = listener;
        this.mItems = new ArrayList<>();
    }

    public interface OnClickListener {
        void onGroupClick(Group item);

        void onChannelClick(Channel item);

        boolean onChannelLongClick(Channel item);
    }

    static class GroupHolder extends RecyclerView.ViewHolder {

        private final AdapterGroupBinding binding;

        GroupHolder(@NonNull AdapterGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ChannelHolder extends RecyclerView.ViewHolder {

        private final AdapterChannelBinding binding;

        ChannelHolder(@NonNull AdapterChannelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void addAll(List<?> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) instanceof Group ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new GroupHolder(AdapterGroupBinding.inflate(inflater, parent, false));
        } else {
            return new ChannelHolder(AdapterChannelBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            setGroup((GroupHolder) holder, (Group) mItems.get(position));
        } else {
            setChannel((ChannelHolder) holder, (Channel) mItems.get(position));
        }
    }

    private void setGroup(GroupHolder holder, Group item) {
        holder.binding.name.setText(item.getName());
        holder.itemView.setOnClickListener(v -> mListener.onGroupClick(item));
    }

    private void setChannel(ChannelHolder holder, Channel item) {
        holder.binding.name.setText(item.getName());
        holder.binding.number.setText(item.getNumber());
        item.loadLogo(holder.binding.logo);
        holder.itemView.setSelected(item.isSelected());
        holder.itemView.setOnClickListener(v -> mListener.onChannelClick(item));
        holder.itemView.setOnLongClickListener(v -> mListener.onChannelLongClick(item));
    }
}
