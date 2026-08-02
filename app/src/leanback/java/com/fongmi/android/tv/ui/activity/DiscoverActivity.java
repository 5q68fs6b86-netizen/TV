package com.fongmi.android.tv.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.DiscoverApi;
import com.fongmi.android.tv.bean.DiscoverFacet;
import com.fongmi.android.tv.bean.DiscoverShelf;
import com.fongmi.android.tv.bean.FeaturedVodRow;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.ActivityDiscoverBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.CustomRowPresenter;
import com.fongmi.android.tv.ui.custom.CustomSelector;
import com.fongmi.android.tv.ui.presenter.DiscoverFacetPresenter;
import com.fongmi.android.tv.ui.presenter.DiscoverLandscapePresenter;
import com.fongmi.android.tv.ui.presenter.DiscoverShelfPresenter;
import com.fongmi.android.tv.ui.presenter.FeaturedVodPresenter;
import com.fongmi.android.tv.ui.presenter.HeaderPresenter;
import com.fongmi.android.tv.ui.presenter.ProgressPresenter;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DiscoverActivity extends BaseActivity implements VodPresenter.OnClickListener, DiscoverShelfPresenter.Listener, DiscoverFacetPresenter.Listener {

    private static final int REQUEST_COUNT = 9;
    private static final int SKELETON_ROWS = 3;

    private final Map<DiscoverApi.Row, List<Vod>> content = new EnumMap<>(DiscoverApi.Row.class);
    private ActivityDiscoverBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private List<DiscoverFacet> genres = List.of();
    private List<DiscoverFacet> providers = List.of();
    private int pending;
    private boolean shown;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DiscoverActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityDiscoverBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setRecyclerView();
        load();
    }

    @SuppressLint("RestrictedApi")
    private void setRecyclerView() {
        CustomSelector selector = new CustomSelector();
        selector.addPresenter(Integer.class, new HeaderPresenter());
        selector.addPresenter(String.class, new ProgressPresenter());
        selector.addPresenter(FeaturedVodRow.class, new FeaturedVodPresenter(this, R.string.discover_view_details));
        selector.addPresenter(ListRow.class, new CustomRowPresenter(16, FocusHighlight.ZOOM_FACTOR_NONE));
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(selector)));
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(10));
    }

    private void load() {
        content.clear();
        genres = List.of();
        providers = List.of();
        shown = false;
        pending = REQUEST_COUNT;
        showSkeletons();
        DiscoverApi.Row[] rows = {
                DiscoverApi.Row.TMDB_DAY, DiscoverApi.Row.TMDB_WEEK, DiscoverApi.Row.TMDB_NOW_PLAYING,
                DiscoverApi.Row.TMDB_POPULAR_MOVIE, DiscoverApi.Row.TMDB_POPULAR_TV,
                DiscoverApi.Row.TMDB_TOP_MOVIE, DiscoverApi.Row.TMDB_TOP_TV
        };
        for (DiscoverApi.Row row : rows) DiscoverApi.fetch(row, BuildConfig.TMDB_API_KEY, listener());
        DiscoverApi.fetchGenres(BuildConfig.TMDB_API_KEY, facetListener(true));
        DiscoverApi.fetchProviders(BuildConfig.TMDB_API_KEY, facetListener(false));
    }

    private DiscoverApi.Listener listener() {
        return new DiscoverApi.Listener() {
            @Override
            public void onSuccess(DiscoverApi.Row row, List<Vod> items) {
                if (!items.isEmpty()) content.put(row, items);
                completeRequest();
            }

            @Override
            public void onError(DiscoverApi.Row row, Exception e) {
                completeRequest();
            }
        };
    }

    private DiscoverApi.FacetListener facetListener(boolean genre) {
        return new DiscoverApi.FacetListener() {
            @Override
            public void onSuccess(List<DiscoverFacet> items) {
                if (genre) genres = items;
                else providers = items;
                completeRequest();
            }

            @Override
            public void onError(Exception e) {
                completeRequest();
            }
        };
    }

    private void completeRequest() {
        if (isFinishing()) return;
        pending--;
        if (!shown && hasContent()) {
            shown = true;
            buildPage();
            mBinding.progressLayout.showContent();
            requestFocus();
        } else if (shown) {
            buildPage();
        }
        if (pending == 0 && !shown) {
            mAdapter.clear();
            mBinding.progressLayout.showContent(true, 0);
        }
    }

    private boolean hasContent() {
        return !content.isEmpty() || !genres.isEmpty() || !providers.isEmpty();
    }

    private void buildPage() {
        mAdapter.clear();
        List<Vod> hero = first(DiscoverApi.Row.TMDB_DAY, DiscoverApi.Row.TMDB_WEEK);
        if (!hero.isEmpty()) mAdapter.add(FeaturedVodRow.create(hero.subList(0, Math.min(5, hero.size()))));
        addLandscape(R.string.discover_today_trending, content.get(DiscoverApi.Row.TMDB_DAY));
        addLandscape(R.string.discover_week_trending, content.get(DiscoverApi.Row.TMDB_WEEK));

        List<DiscoverShelf> features = new ArrayList<>();
        addShelf(features, DiscoverShelf.Type.FEATURE, getString(R.string.discover_popular_movie), DiscoverFacet.TOP_MOVIE, content.get(DiscoverApi.Row.TMDB_POPULAR_MOVIE));
        addShelf(features, DiscoverShelf.Type.FEATURE, getString(R.string.discover_popular_tv), DiscoverFacet.TOP_TV, content.get(DiscoverApi.Row.TMDB_POPULAR_TV));
        addObjectRow(features, new DiscoverShelfPresenter(this));

        addPosterRow(R.string.discover_now_playing, content.get(DiscoverApi.Row.TMDB_NOW_PLAYING));

        List<DiscoverShelf> ranking = new ArrayList<>();
        addShelf(ranking, DiscoverShelf.Type.RANKING, getString(R.string.discover_top_tv), DiscoverFacet.TOP_TV, content.get(DiscoverApi.Row.TMDB_TOP_TV));
        addShelf(ranking, DiscoverShelf.Type.RANKING, getString(R.string.discover_top_movie), DiscoverFacet.TOP_MOVIE, content.get(DiscoverApi.Row.TMDB_TOP_MOVIE));
        addObjectRow(ranking, new DiscoverShelfPresenter(this));

        addFacetRow(R.string.discover_categories, genres);
        addFacetRow(R.string.discover_companies, companies());
        addFacetRow(R.string.discover_providers, providers);
    }

    private List<Vod> first(DiscoverApi.Row first, DiscoverApi.Row second) {
        List<Vod> items = content.get(first);
        return items == null || items.isEmpty() ? content.getOrDefault(second, List.of()) : items;
    }

    private void addLandscape(int title, List<Vod> items) {
        if (items == null || items.isEmpty()) return;
        mAdapter.add(title);
        ArrayObjectAdapter row = new ArrayObjectAdapter(new DiscoverLandscapePresenter(this));
        row.addAll(0, items);
        mAdapter.add(new ListRow(row));
    }

    private void addPosterRow(int title, List<Vod> items) {
        if (items == null || items.isEmpty()) return;
        mAdapter.add(title);
        ArrayObjectAdapter row = new ArrayObjectAdapter(new VodPresenter(this, Style.rect(), new int[]{ResUtil.dp2px(124), ResUtil.dp2px(186)}));
        row.addAll(0, items);
        mAdapter.add(new ListRow(row));
    }

    private void addShelf(List<DiscoverShelf> shelves, DiscoverShelf.Type type, String title, String kind, List<Vod> items) {
        if (items != null && !items.isEmpty()) shelves.add(new DiscoverShelf(type, title, kind, items.subList(0, Math.min(3, items.size()))));
    }

    private void addObjectRow(List<?> items, androidx.leanback.widget.Presenter presenter) {
        if (items.isEmpty()) return;
        ArrayObjectAdapter row = new ArrayObjectAdapter(presenter);
        row.addAll(0, items);
        mAdapter.add(new ListRow(row));
    }

    private void addFacetRow(int title, List<DiscoverFacet> items) {
        if (items == null || items.isEmpty()) return;
        mAdapter.add(title);
        addObjectRow(items, new DiscoverFacetPresenter(this));
    }

    private List<DiscoverFacet> companies() {
        return List.of(
                new DiscoverFacet(DiscoverFacet.COMPANY, "33", "Universal", ""),
                new DiscoverFacet(DiscoverFacet.COMPANY, "4", "Paramount", ""),
                new DiscoverFacet(DiscoverFacet.COMPANY, "5", "Columbia", ""),
                new DiscoverFacet(DiscoverFacet.COMPANY, "420", "Marvel", "")
        );
    }

    private void showSkeletons() {
        mAdapter.clear();
        for (int i = 0; i < SKELETON_ROWS; i++) mAdapter.add("discover_progress_" + i);
        mBinding.progressLayout.showContent();
    }

    private void requestFocus() {
        mBinding.recycler.postDelayed(() -> {
            RecyclerView.ViewHolder holder = mBinding.recycler.findViewHolderForAdapterPosition(mBinding.recycler.getSelectedPosition());
            View focus = holder == null ? null : findFocusable(holder.itemView);
            if (focus != null && focus.requestFocus()) return;
            if (mBinding.recycler.isShown()) mBinding.recycler.requestFocus();
        }, 80);
    }

    private View findFocusable(View view) {
        if (!view.isShown() || !view.isEnabled()) return null;
        if (view.isFocusable()) return view;
        if (!(view instanceof android.view.ViewGroup group)) return null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View target = findFocusable(group.getChildAt(i));
            if (target != null) return target;
        }
        return null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!shown && pending == 0 && event.getAction() == KeyEvent.ACTION_UP &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            load();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onItemClick(Vod item) {
        com.fongmi.android.tv.ui.dialog.DiscoverDialog.create(item).show(this);
    }

    @Override
    public boolean onLongClick(Vod item) {
        return false;
    }

    @Override
    public void onShelfClick(DiscoverShelf shelf) {
        DiscoverResultActivity.start(this, new DiscoverFacet(shelf.getFacetKind(), "", shelf.getTitle(), ""));
    }

    @Override
    public void onFacetClick(DiscoverFacet facet) {
        DiscoverResultActivity.start(this, facet);
    }

    @Override
    protected void onDestroy() {
        DiscoverApi.cancel();
        super.onDestroy();
    }
}
