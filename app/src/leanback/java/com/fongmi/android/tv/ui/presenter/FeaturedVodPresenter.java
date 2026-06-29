package com.fongmi.android.tv.ui.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.bean.FeaturedVodRow;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterFeaturedVodBinding;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class FeaturedVodPresenter extends Presenter {

    private final VodPresenter.OnClickListener listener;

    public FeaturedVodPresenter(VodPresenter.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(AdapterFeaturedVodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        ((ViewHolder) viewHolder).bind((FeaturedVodRow) object);
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).unbind();
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private static final long AUTO_DELAY = 6500;
        private static final long CROSS_FADE = 900;
        private static final long FOCUS_FADE = 160;

        private final AdapterFeaturedVodBinding binding;
        private final VodPresenter.OnClickListener listener;
        private final Handler handler;
        private final Runnable rotate;
        private FeaturedVodRow row;
        private ShapeableImageView front;
        private int index;

        public ViewHolder(@NonNull AdapterFeaturedVodBinding binding, VodPresenter.OnClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.handler = new Handler(Looper.getMainLooper());
            this.rotate = () -> {
                show(index + 1, true);
                schedule();
            };
            this.front = binding.imageA;
            setListeners();
        }

        private void setListeners() {
            binding.getRoot().setOnFocusChangeListener((view, hasFocus) -> setActionVisible(hasFocus));
            binding.getRoot().setOnKeyListener((view, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN || row == null || row.size() < 2) return false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    show(index - 1, true);
                    restart();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    show(index + 1, true);
                    restart();
                    return true;
                }
                return false;
            });
        }

        private void bind(FeaturedVodRow row) {
            this.row = row;
            this.index = 0;
            this.front = binding.imageA;
            binding.imageA.animate().cancel();
            binding.imageB.animate().cancel();
            binding.imageA.setAlpha(1f);
            binding.imageA.setVisibility(View.VISIBLE);
            binding.imageB.setAlpha(0f);
            binding.imageB.setVisibility(View.GONE);
            setActionVisible(binding.getRoot().hasFocus(), false);
            show(0, false);
            binding.getRoot().setOnClickListener(view -> {
                Vod item = getCurrent();
                if (item != null) listener.onItemClick(item);
            });
            restart();
        }

        private void show(int position, boolean animate) {
            if (row == null || row.isEmpty()) return;
            index = Math.floorMod(position, row.size());
            Vod item = row.get(index);
            bindText(item);
            ShapeableImageView next = front == binding.imageA ? binding.imageB : binding.imageA;
            if (!animate) {
                ImgUtil.load(item.getName(), item.getPic(), front);
                return;
            }
            ImgUtil.load(item.getName(), item.getPic(), next);
            next.animate().cancel();
            front.animate().cancel();
            next.setAlpha(0f);
            next.setVisibility(View.VISIBLE);
            ShapeableImageView old = front;
            front = next;
            next.animate().alpha(1f).setDuration(CROSS_FADE).start();
            old.animate().alpha(0f).setDuration(CROSS_FADE).withEndAction(() -> {
                if (old != front) old.setVisibility(View.GONE);
            }).start();
        }

        private void bindText(Vod item) {
            binding.name.setText(item.getName());
            binding.meta.setText(getMeta(item));
            binding.meta.setVisibility(TextUtils.isEmpty(binding.meta.getText()) ? View.GONE : View.VISIBLE);
        }

        private String getMeta(Vod item) {
            List<String> values = new ArrayList<>();
            add(values, item.getYear());
            add(values, item.getSiteName());
            add(values, item.getRemarks());
            return TextUtils.join("  |  ", values);
        }

        private void add(List<String> values, String value) {
            if (!TextUtils.isEmpty(value)) values.add(value);
        }

        private Vod getCurrent() {
            if (row == null || row.isEmpty()) return null;
            return row.get(index);
        }

        private void setActionVisible(boolean visible) {
            setActionVisible(visible, true);
        }

        private void setActionVisible(boolean visible, boolean animate) {
            binding.action.animate().cancel();
            if (visible) binding.action.setVisibility(View.VISIBLE);
            if (!animate) {
                binding.action.setAlpha(visible ? 1f : 0f);
                binding.action.setTranslationY(visible ? 0 : ResUtil.dp2px(8));
                binding.action.setVisibility(visible ? View.VISIBLE : View.GONE);
                return;
            }
            binding.action.animate()
                    .alpha(visible ? 1f : 0f)
                    .translationY(visible ? 0 : ResUtil.dp2px(8))
                    .setDuration(FOCUS_FADE)
                    .withEndAction(() -> {
                        if (!visible) binding.action.setVisibility(View.GONE);
                    })
                    .start();
        }

        private void restart() {
            stop();
            schedule();
        }

        private void schedule() {
            if (row != null && row.size() > 1) handler.postDelayed(rotate, AUTO_DELAY);
        }

        private void stop() {
            handler.removeCallbacks(rotate);
        }

        private void unbind() {
            stop();
            binding.getRoot().setOnClickListener(null);
            Glide.with(binding.imageA).clear(binding.imageA);
            Glide.with(binding.imageB).clear(binding.imageB);
        }
    }
}
