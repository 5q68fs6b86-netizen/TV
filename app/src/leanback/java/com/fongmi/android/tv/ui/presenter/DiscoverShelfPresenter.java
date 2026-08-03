package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.DiscoverShelf;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterDiscoverShelfBinding;
import com.fongmi.android.tv.ui.theme.JetStreamAmbient;
import com.fongmi.android.tv.utils.ImgUtil;

public class DiscoverShelfPresenter extends Presenter {

    public interface Listener {
        void onShelfClick(DiscoverShelf shelf);
    }

    private final Listener listener;

    public DiscoverShelfPresenter(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverShelfBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object object) {
        Holder holder = (Holder) viewHolder;
        DiscoverShelf shelf = (DiscoverShelf) object;
        holder.binding.title.setText(shelf.getTitle());
        bind(holder.binding.hero, shelf, 0, true);
        bind(holder.binding.previewA, shelf, 0, false);
        bind(holder.binding.previewB, shelf, 1, false);
        bind(holder.binding.previewC, shelf, 2, false);
        holder.view.setOnClickListener(view -> listener.onShelfClick(shelf));
        holder.view.setOnFocusChangeListener((view, focused) -> {
            com.fongmi.android.tv.ui.custom.JetStreamAnimator.animateFocus(view, focused, com.fongmi.android.tv.ui.custom.JetStreamAnimator.FOCUS_SCALE_LIST, 0);
            if (focused && !shelf.getItems().isEmpty()) JetStreamAmbient.push(shelf.getItems().get(0).getBackdrop());
        });
    }

    private void bind(android.widget.ImageView image, DiscoverShelf shelf, int index, boolean backdrop) {
        if (index >= shelf.getItems().size()) {
            image.setVisibility(View.INVISIBLE);
            return;
        }
        image.setVisibility(View.VISIBLE);
        Vod item = shelf.getItems().get(index);
        ImgUtil.load(item.getName(), backdrop ? item.getBackdrop() : item.getPic(), image);
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        Holder holder = (Holder) viewHolder;
        ImgUtil.clear(holder.binding.hero);
        ImgUtil.clear(holder.binding.previewA);
        ImgUtil.clear(holder.binding.previewB);
        ImgUtil.clear(holder.binding.previewC);
    }

    private static class Holder extends ViewHolder {
        private final AdapterDiscoverShelfBinding binding;

        Holder(AdapterDiscoverShelfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
