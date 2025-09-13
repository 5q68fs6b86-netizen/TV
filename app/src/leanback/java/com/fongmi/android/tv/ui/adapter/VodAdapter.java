package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterVodListBinding;
import com.fongmi.android.tv.databinding.AdapterVodRectBinding;
import com.fongmi.android.tv.utils.ImgUtil;

import java.util.List;

public class VodAdapter extends RecyclerView.Adapter<VodAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final List<Vod> mItems;
    private final Style mStyle;

    public VodAdapter(OnClickListener listener, List<Vod> items, Style style) {
        this.mListener = listener;
        this.mItems = items;
        this.mStyle = style;
    }

    public interface OnClickListener {
        void onItemClick(Vod item);
        boolean onLongClick(Vod item);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Vod> items) {
        mItems.addAll(items);
        notifyItemRangeInserted(mItems.size() - items.size(), items.size());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mStyle.isList() ? 0 : 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) return new ViewHolder(AdapterVodListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else return new ViewHolder(AdapterVodRectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vod item = mItems.get(position);
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> mListener.onLongClick(item));
        if (mStyle.isList()) {
            AdapterVodListBinding binding = (AdapterVodListBinding) holder.binding;
            binding.title.setText(item.getVodName());
            binding.remark.setText(item.getVodRemark());
            ImgUtil.load(item.getVodPic(), binding.poster);
        } else {
            AdapterVodRectBinding binding = (AdapterVodRectBinding) holder.binding;
            binding.title.setText(item.getVodName());
            ImgUtil.load(item.getVodPic(), binding.poster);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewBinding binding;

        public ViewHolder(@NonNull ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}