package com.fongmi.android.tv.ui.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.FeaturedVodRow;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterFeaturedVodBinding;
import com.fongmi.android.tv.ui.custom.JetStreamFeaturedIndicatorDotView;
import com.fongmi.android.tv.ui.custom.JetStreamAnimator;
import com.fongmi.android.tv.ui.theme.JetStreamAmbient;
import com.fongmi.android.tv.utils.FeaturedPosterCache;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.TmdbLogoHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeaturedVodPresenter extends Presenter {

    private final VodPresenter.OnClickListener listener;
    private final int actionText;

    public FeaturedVodPresenter(VodPresenter.OnClickListener listener) {
        this(listener, R.string.play);
    }

    public FeaturedVodPresenter(VodPresenter.OnClickListener listener, @StringRes int actionText) {
        this.listener = listener;
        this.actionText = actionText;
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(AdapterFeaturedVodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener, actionText);
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        ((ViewHolder) viewHolder).bind((FeaturedVodRow) object);
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).unbind();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull Presenter.ViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);
        ((ViewHolder) viewHolder).attach();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull Presenter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).detach();
        super.onViewDetachedFromWindow(viewHolder);
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private static final long AUTO_DELAY = 6500;
        private static final long CROSS_FADE = 900;
        private static final long FOCUS_FADE = 160;

        private final AdapterFeaturedVodBinding binding;
        private final VodPresenter.OnClickListener listener;
        private final Handler handler;
        private final Runnable rotate;
        private final ViewTreeObserver.OnWindowFocusChangeListener windowFocusListener;
        private final Map<String, String> artworkCache;
        private final Set<String> artworkMissing;
        private final FeaturedPosterCache posterCache;
        private FeaturedVodRow row;
        private ShapeableImageView front;
        private String artworkRequest;
        private String currentArtwork;
        private boolean windowFocusListenerRegistered;
        private int index;

        public ViewHolder(@NonNull AdapterFeaturedVodBinding binding, VodPresenter.OnClickListener listener, @StringRes int actionText) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.handler = new Handler(Looper.getMainLooper());
            this.artworkCache = new HashMap<>();
            this.artworkMissing = new HashSet<>();
            this.posterCache = new FeaturedPosterCache();
            this.rotate = () -> {
                // 页面失去窗口焦点（进后台/被覆盖）时跳过本轮，避免向全局 JetStreamAmbient 推送污染前台页面背景
                if (binding.getRoot().hasWindowFocus()) show(index + 1, true);
                schedule();
            };
            this.windowFocusListener = hasFocus -> {
                if (hasFocus) publishCurrentArtwork();
            };
            this.front = binding.imageA;
            binding.actionText.setText(actionText);
            setListeners();
        }

        private void setListeners() {
            binding.getRoot().setOnFocusChangeListener((view, hasFocus) -> {
                JetStreamAnimator.animateFocus(view, hasFocus, JetStreamAnimator.FOCUS_SCALE_LIST, 12);
                setActionVisible(hasFocus);
            });
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
            this.artworkRequest = null;
            this.currentArtwork = null;
            if (posterCache.prepare(row.getItems())) {
                artworkCache.clear();
                artworkMissing.clear();
            }
            binding.imageA.animate().cancel();
            binding.imageB.animate().cancel();
            binding.imageA.setAlpha(1f);
            binding.imageA.setVisibility(View.VISIBLE);
            binding.imageB.setAlpha(0f);
            binding.imageB.setVisibility(View.GONE);
            bindIndicator(row.size());
            setActionVisible(binding.getRoot().hasFocus(), false);
            show(0, false);
            binding.getRoot().setOnClickListener(view -> {
                Vod item = getCurrent();
                if (item != null) listener.onItemClick(item, front);
            });
            restart();
        }

        private void show(int position, boolean animate) {
            if (row == null || row.isEmpty()) return;
            index = Math.floorMod(position, row.size());
            Vod item = row.get(index);
            bindText(item);
            updateIndicator();
            ShapeableImageView next = front == binding.imageA ? binding.imageB : binding.imageA;
            if (!animate) {
                loadFeaturedArtwork(item, front);
                return;
            }
            next.animate().cancel();
            front.animate().cancel();
            next.setAlpha(0f);
            next.setVisibility(View.VISIBLE);
            ShapeableImageView old = front;
            front = next;
            loadFeaturedArtwork(item, next);
            next.animate().alpha(1f).setDuration(CROSS_FADE).start();
            old.animate().alpha(0f).setDuration(CROSS_FADE).withEndAction(() -> {
                if (old != front) old.setVisibility(View.GONE);
            }).start();
        }

        private void loadFeaturedArtwork(Vod item, ShapeableImageView target) {
            String key = FeaturedPosterCache.keyOf(item);
            String requestSignature = posterCache.getSignature();
            artworkRequest = key;
            String cached = posterCache.get(item);
            if (!TextUtils.isEmpty(cached)) {
                artworkCache.put(key, cached);
                loadArtwork(item, cached, target);
                return;
            }
            cached = artworkCache.get(key);
            if (!TextUtils.isEmpty(cached)) {
                loadArtwork(item, cached, target);
                return;
            }
            loadArtwork(item, item.getPic(), target);
            if (TextUtils.isEmpty(item.getName()) || artworkMissing.contains(key)) return;
            TmdbLogoHelper.findPoster(BuildConfig.TMDB_API_KEY, item.getName(), item.getYear(), item.getTypeName(), new TmdbLogoHelper.ImageCallback() {
                @Override
                public void onFound(@NonNull String imageUrl) {
                    if (!posterCache.isCurrent(requestSignature)) return;
                    artworkCache.put(key, imageUrl);
                    if (isArtworkRequestActive(key)) loadArtwork(item, imageUrl, front);
                    posterCache.put(requestSignature, item, imageUrl, new FeaturedPosterCache.Callback() {
                        @Override
                        public void success(@NonNull String cachedUrl) {
                            artworkCache.put(key, cachedUrl);
                            if (isArtworkRequestActive(key)) loadArtwork(item, cachedUrl, front);
                        }

                        @Override
                        public void error(@NonNull Exception error) {
                        }
                    });
                }

                @Override
                public void onNotFound() {
                    if (!posterCache.isCurrent(requestSignature)) return;
                    artworkMissing.add(key);
                }

                @Override
                public void onError(@NonNull Exception error) {
                    if (!posterCache.isCurrent(requestSignature)) return;
                    artworkMissing.add(key);
                }
            });
        }

        private void loadArtwork(Vod item, String url, ShapeableImageView target) {
            ImgUtil.load(item.getName(), url, target);
            currentArtwork = url;
            publishCurrentArtwork();
        }

        private void publishCurrentArtwork() {
            // JetStreamAmbient 是全局单例，只有当前窗口可更新；重新获得焦点时补发此前被抑制的图片。
            if (!TextUtils.isEmpty(currentArtwork) && binding.getRoot().isAttachedToWindow() && binding.getRoot().hasWindowFocus()) {
                JetStreamAmbient.push(currentArtwork);
            }
        }

        private boolean isArtworkRequestActive(String key) {
            return row != null && TextUtils.equals(key, artworkRequest);
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

        private void bindIndicator(int count) {
            binding.indicator.removeAllViews();
            binding.indicator.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
            for (int i = 0; i < count; i++) {
                View dot = new JetStreamFeaturedIndicatorDotView(binding.indicator.getContext());
                int size = ResUtil.dp2px(7);
                int margin = ResUtil.dp2px(3);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMarginStart(margin);
                params.setMarginEnd(margin);
                binding.indicator.addView(dot, params);
            }
            updateIndicator();
        }

        private void updateIndicator() {
            for (int i = 0; i < binding.indicator.getChildCount(); i++) {
                binding.indicator.getChildAt(i).setSelected(i == index);
            }
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

        private void attach() {
            if (!windowFocusListenerRegistered) {
                binding.getRoot().getViewTreeObserver().addOnWindowFocusChangeListener(windowFocusListener);
                windowFocusListenerRegistered = true;
            }
            publishCurrentArtwork();
            restart();
        }

        private void detach() {
            stop();
            ViewTreeObserver observer = binding.getRoot().getViewTreeObserver();
            if (windowFocusListenerRegistered && observer.isAlive()) observer.removeOnWindowFocusChangeListener(windowFocusListener);
            windowFocusListenerRegistered = false;
        }

        private void schedule() {
            if (row != null && row.size() > 1) handler.postDelayed(rotate, AUTO_DELAY);
        }

        private void stop() {
            handler.removeCallbacks(rotate);
        }

        private void unbind() {
            detach();
            row = null;
            artworkRequest = null;
            currentArtwork = null;
            binding.getRoot().setOnClickListener(null);
            JetStreamAnimator.reset(binding.getRoot());
            ImgUtil.clear(binding.imageA);
            ImgUtil.clear(binding.imageB);
        }
    }
}
