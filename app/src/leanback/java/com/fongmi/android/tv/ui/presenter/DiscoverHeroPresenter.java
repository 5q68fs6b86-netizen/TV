package com.fongmi.android.tv.ui.presenter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.DiscoverHero;
import com.fongmi.android.tv.bean.DiscoverMediaKey;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterDiscoverHeroBinding;
import com.fongmi.android.tv.ui.custom.JetStreamAnimator;
import com.fongmi.android.tv.ui.custom.JetStreamDiscoverHeroPosterView;
import com.fongmi.android.tv.ui.theme.JetStreamAmbient;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public final class DiscoverHeroPresenter extends Presenter {

    private final VodPresenter.OnClickListener listener;

    public DiscoverHeroPresenter(VodPresenter.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(AdapterDiscoverHeroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, Object object) {
        ((Holder) viewHolder).bind((DiscoverHero) object);
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {
        ((Holder) viewHolder).unbind();
    }

    private static final class Holder extends ViewHolder {

        private static final int MAX_POSTERS = 5;
        private static final int OVERLAP_DP = 34;
        private final AdapterDiscoverHeroBinding binding;
        private final VodPresenter.OnClickListener listener;
        private final List<JetStreamDiscoverHeroPosterView> posters = new ArrayList<>();
        private final List<Vod> items = new ArrayList<>();

        private Holder(AdapterDiscoverHeroBinding binding, VodPresenter.OnClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        private void bind(DiscoverHero hero) {
            unbind();
            items.addAll(hero.getItems().subList(0, Math.min(MAX_POSTERS, hero.getItems().size())));
            if (items.isEmpty()) return;
            int[] spec = Product.getSpec(Style.rect());
            int overlap = Math.min(ResUtil.dp2px(OVERLAP_DP), Math.max(0, spec[0] / 3));
            int stackWidth = spec[0] + Math.max(0, items.size() - 1) * (spec[0] - overlap);
            binding.posters.getLayoutParams().width = stackWidth;
            binding.posters.getLayoutParams().height = spec[1] + ResUtil.dp2px(24);
            binding.getRoot().getLayoutParams().height = spec[1] + ResUtil.dp2px(48);
            for (int i = 0; i < items.size(); i++) addPoster(items.get(i), i, spec, overlap);
            show(items.get(0));
        }

        private void addPoster(Vod item, int index, int[] spec, int overlap) {
            JetStreamDiscoverHeroPosterView poster = new JetStreamDiscoverHeroPosterView(binding.posters.getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(spec[0], spec[1]);
            params.leftMargin = index * (spec[0] - overlap);
            params.topMargin = index % 2 == 0 ? ResUtil.dp2px(2) : ResUtil.dp2px(16);
            poster.setLayoutParams(params);
            poster.setElevation(index);
            poster.setContentDescription(item.getName());
            poster.setOnFocusChangeListener((view, hasFocus) -> {
                JetStreamAnimator.animateFocus(view, hasFocus, JetStreamAnimator.FOCUS_SCALE_CARD, 16);
                view.setElevation(hasFocus ? ResUtil.dp2px(24) : index);
                if (hasFocus) show(item);
            });
            poster.setOnClickListener(view -> listener.onItemClick(item, poster));
            ImgUtil.load(item.getName(), item.getPic(), poster);
            posters.add(poster);
            binding.posters.addView(poster);
        }

        private void show(Vod item) {
            binding.source.setText(source(item));
            binding.name.setText(item.getName());
            List<String> meta = new ArrayList<>();
            add(meta, item.getYear());
            add(meta, item.getRemarks());
            add(meta, mediaLabel(item));
            binding.meta.setText(TextUtils.join("  |  ", meta));
            binding.meta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
            binding.content.setText(item.getContent());
            binding.content.setVisibility(TextUtils.isEmpty(item.getContent()) ? View.GONE : View.VISIBLE);
            JetStreamAmbient.push(item.getBackdrop());
        }

        private String source(Vod item) {
            String id = item.getId();
            if (id.startsWith("douban:")) return binding.getRoot().getContext().getString(R.string.discover_source_douban);
            return binding.getRoot().getContext().getString(R.string.discover_source_tmdb);
        }

        private String mediaLabel(Vod item) {
            if (DiscoverMediaKey.TV.equals(item.getTypeName())) return binding.getRoot().getContext().getString(R.string.discover_media_tv);
            if (DiscoverMediaKey.MOVIE.equals(item.getTypeName())) return binding.getRoot().getContext().getString(R.string.discover_media_movie);
            return item.getTypeName();
        }

        private void add(List<String> values, String value) {
            if (!TextUtils.isEmpty(value)) values.add(value);
        }

        private void unbind() {
            for (JetStreamDiscoverHeroPosterView poster : posters) {
                poster.setOnFocusChangeListener(null);
                poster.setOnClickListener(null);
                JetStreamAnimator.reset(poster);
                ImgUtil.clear(poster);
            }
            posters.clear();
            items.clear();
            binding.posters.removeAllViews();
        }
    }
}
