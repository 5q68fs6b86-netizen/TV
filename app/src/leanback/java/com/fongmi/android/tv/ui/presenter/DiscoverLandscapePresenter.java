package com.fongmi.android.tv.ui.presenter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterDiscoverLandscapeBinding;
import com.fongmi.android.tv.ui.theme.JetStreamAmbient;
import com.fongmi.android.tv.utils.ImgUtil;

import java.util.ArrayList;
import java.util.List;

public class DiscoverLandscapePresenter extends Presenter {

    private final VodPresenter.OnClickListener listener;

    public DiscoverLandscapePresenter(VodPresenter.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverLandscapeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object item) {
        Holder holder = (Holder) viewHolder;
        Vod vod = (Vod) item;
        holder.binding.name.setText(vod.getName());
        List<String> values = new ArrayList<>();
        if (!TextUtils.isEmpty(vod.getYear())) values.add(vod.getYear());
        if (!TextUtils.isEmpty(vod.getTypeName())) values.add(vod.getTypeName());
        String meta = TextUtils.join(" · ", values);
        holder.binding.meta.setText(meta);
        ImgUtil.load(vod.getName(), vod.getBackdrop(), holder.binding.image);
        holder.view.setOnClickListener(view -> listener.onItemClick(vod, holder.binding.image));
        holder.view.setOnLongClickListener(view -> listener.onLongClick(vod));
        holder.view.setOnFocusChangeListener((view, focused) -> {
            com.fongmi.android.tv.ui.custom.JetStreamAnimator.animateFocus(view, focused, com.fongmi.android.tv.ui.custom.JetStreamAnimator.FOCUS_SCALE_CARD, 0);
            if (focused) JetStreamAmbient.push(vod.getBackdrop());
        });
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        ImgUtil.clear(((Holder) viewHolder).binding.image);
    }

    private static class Holder extends ViewHolder {
        private final AdapterDiscoverLandscapeBinding binding;

        Holder(AdapterDiscoverLandscapeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
