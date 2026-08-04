package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.DiscoverFacet;
import com.fongmi.android.tv.databinding.AdapterDiscoverFacetBinding;
import com.fongmi.android.tv.utils.ImgUtil;

public class DiscoverFacetPresenter extends Presenter {

    public interface Listener {
        void onFacetClick(DiscoverFacet facet);
    }

    private final Listener listener;

    public DiscoverFacetPresenter(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverFacetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object object) {
        Holder holder = (Holder) viewHolder;
        DiscoverFacet facet = (DiscoverFacet) object;
        holder.binding.name.setText(facet.getName());
        holder.binding.image.setVisibility(facet.getImage().isEmpty() ? View.GONE : View.VISIBLE);
        if (!facet.getImage().isEmpty()) ImgUtil.load(facet.getName(), facet.getImage(), holder.binding.image);
        holder.view.setOnClickListener(view -> listener.onFacetClick(facet));
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        ImgUtil.clear(((Holder) viewHolder).binding.image);
    }

    private static class Holder extends ViewHolder {
        private final AdapterDiscoverFacetBinding binding;

        Holder(AdapterDiscoverFacetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
