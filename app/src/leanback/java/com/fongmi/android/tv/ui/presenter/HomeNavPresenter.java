package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.Func;
import com.fongmi.android.tv.databinding.AdapterHomeNavBinding;

public class HomeNavPresenter extends Presenter {

    private final FuncPresenter.OnClickListener listener;

    public HomeNavPresenter(FuncPresenter.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(AdapterHomeNavBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        Func item = (Func) object;
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.binding.text.setText(item.getText());
        holder.binding.icon.setImageResource(item.getDrawable());
        holder.binding.getRoot().setOnClickListener(view -> listener.onItemClick(item));
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).binding.getRoot().setOnClickListener(null);
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final AdapterHomeNavBinding binding;

        public ViewHolder(@NonNull AdapterHomeNavBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
