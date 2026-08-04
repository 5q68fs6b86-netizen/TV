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
import com.fongmi.android.tv.bean.DiscoverFilterOption;
import com.fongmi.android.tv.bean.DiscoverMediaKey;
import com.fongmi.android.tv.bean.DiscoverQuery;
import com.fongmi.android.tv.bean.DiscoverRequestState;
import com.fongmi.android.tv.bean.DiscoverShelf;
import com.fongmi.android.tv.bean.FeaturedVodRow;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.ActivityDiscoverBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.CustomRowPresenter;
import com.fongmi.android.tv.ui.custom.CustomScroller;
import com.fongmi.android.tv.ui.custom.CustomSelector;
import com.fongmi.android.tv.ui.presenter.DiscoverFilterPresenter;
import com.fongmi.android.tv.ui.presenter.DiscoverLandscapePresenter;
import com.fongmi.android.tv.ui.presenter.DiscoverShelfPresenter;
import com.fongmi.android.tv.ui.presenter.FeaturedVodPresenter;
import com.fongmi.android.tv.ui.presenter.HeaderPresenter;
import com.fongmi.android.tv.ui.presenter.ProgressPresenter;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.common.collect.Lists;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DiscoverActivity extends BaseActivity implements VodPresenter.OnClickListener, DiscoverShelfPresenter.Listener,
        DiscoverFilterPresenter.Listener, CustomScroller.Callback {

    private static final int COLUMN = 6;
    private static final int FEATURE_REQUESTS = 7;
    private static final int FILTER_MEDIA = 0;
    private static final int FILTER_GENRE = 1;
    private static final int FILTER_REGION = 2;
    private static final int FILTER_YEAR = 3;
    private static final int FILTER_SORT = 4;

    private final Map<DiscoverApi.Row, List<Vod>> content = new EnumMap<>(DiscoverApi.Row.class);
    private final DiscoverRequestState requestState = new DiscoverRequestState();
    private final Object requestTag = new Object();
    private ActivityDiscoverBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private CustomScroller scroller;
    private List<DiscoverFacet> genres = List.of();
    private String mediaType = DiscoverMediaKey.MOVIE;
    private String genreId = "";
    private String region = "";
    private String dateStart = "";
    private String dateEnd = "";
    private String sort = DiscoverQuery.SORT_POPULAR;
    private DiscoverQuery lastQuery;
    private int featurePending;
    private int queryGeneration;
    private int page = 1;
    private int totalPages = 1;
    private int resultStartPosition = -1;
    private boolean featureFinished;
    private boolean queryFinished;
    private boolean pendingResultFocus;
    private View pendingFilterFocus;

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
        loadFeatured();
        loadGenres(mediaType, false);
        refreshResults(null, true);
    }

    @SuppressLint("RestrictedApi")
    private void setRecyclerView() {
        CustomSelector selector = new CustomSelector();
        selector.addPresenter(Integer.class, new HeaderPresenter());
        selector.addPresenter(String.class, new ProgressPresenter());
        selector.addPresenter(FeaturedVodRow.class, new FeaturedVodPresenter(this, R.string.discover_view_details));
        selector.addPresenter(ListRow.class, new CustomRowPresenter(14, FocusHighlight.ZOOM_FACTOR_NONE));
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(selector)));
        mBinding.recycler.setItemAnimator(null);
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(8));
        mBinding.recycler.addOnScrollListener(scroller = new CustomScroller(this));
        mBinding.progressLayout.showProgress();
    }

    private void loadFeatured() {
        content.clear();
        featurePending = FEATURE_REQUESTS;
        featureFinished = false;
        DiscoverApi.Row[] rows = {
                DiscoverApi.Row.TMDB_DAY, DiscoverApi.Row.TMDB_WEEK, DiscoverApi.Row.TMDB_NOW_PLAYING,
                DiscoverApi.Row.TMDB_POPULAR_MOVIE, DiscoverApi.Row.TMDB_POPULAR_TV,
                DiscoverApi.Row.TMDB_TOP_MOVIE, DiscoverApi.Row.TMDB_TOP_TV
        };
        for (DiscoverApi.Row row : rows) DiscoverApi.fetch(row, BuildConfig.TMDB_API_KEY, requestTag, new DiscoverApi.Listener() {
            @Override
            public void onSuccess(DiscoverApi.Row value, List<Vod> items) {
                if (!items.isEmpty()) content.put(value, items);
                completeFeatured();
            }

            @Override
            public void onError(DiscoverApi.Row value, Exception e) {
                completeFeatured();
            }
        });
    }

    private void completeFeatured() {
        if (isInactive()) return;
        featurePending--;
        featureFinished = featurePending <= 0;
        rebuildPage(false, null);
    }

    private void loadGenres(String type, boolean refreshPage) {
        DiscoverApi.fetchGenres(type, BuildConfig.TMDB_API_KEY, requestTag, new DiscoverApi.FacetListener() {
            @Override
            public void onSuccess(List<DiscoverFacet> items) {
                if (isInactive() || !mediaType.equals(type)) return;
                genres = items;
                if (refreshPage) rebuildPage(false, null);
            }

            @Override
            public void onError(Exception e) {
                if (!isInactive() && mediaType.equals(type)) genres = List.of();
            }
        });
    }

    private void refreshResults(View focus, boolean force) {
        DiscoverQuery query = currentQuery(1);
        if (!force && query.equals(lastQuery)) return;
        lastQuery = query;
        queryGeneration = requestState.reset();
        page = 1;
        totalPages = 1;
        if (scroller != null) {
            scroller.reset();
            scroller.setEnable(2);
        }
        queryFinished = false;
        pendingFilterFocus = focus;
        pendingResultFocus = focus == null;
        rebuildPage(false, focus);
        loadQuery(query, queryGeneration);
    }

    private void loadQuery(DiscoverQuery query, int generation) {
        DiscoverApi.fetch(query, BuildConfig.TMDB_API_KEY, requestTag, new DiscoverApi.QueryListener() {
            @Override
            public void onSuccess(List<Vod> items, int responsePage, int responseTotalPages) {
                if (isInactive() || !requestState.accepts(generation) || !query.equals(currentQuery(query.getPage()))) return;
                requestState.addAll(generation, items);
                page = responsePage;
                totalPages = responseTotalPages;
                queryFinished = true;
                if (scroller != null) {
                    if (query.getPage() > 1) scroller.endLoading(newResult(items));
                    scroller.setEnable(responseTotalPages);
                }
                boolean firstPage = query.getPage() == 1;
                boolean focusResults = firstPage && pendingResultFocus;
                View focus = firstPage ? pendingFilterFocus : null;
                pendingResultFocus = false;
                pendingFilterFocus = null;
                rebuildPage(focusResults, focus);
            }

            @Override
            public void onError(Exception e) {
                if (isInactive() || !requestState.accepts(generation)) return;
                queryFinished = true;
                if (scroller != null) {
                    if (query.getPage() > 1) scroller.endLoading(newResult(List.of()));
                    scroller.setEnable(totalPages);
                }
                View focus = query.getPage() == 1 ? pendingFilterFocus : null;
                pendingResultFocus = false;
                pendingFilterFocus = null;
                rebuildPage(false, focus);
            }
        });
    }

    private com.fongmi.android.tv.bean.Result newResult(List<Vod> items) {
        com.fongmi.android.tv.bean.Result result = new com.fongmi.android.tv.bean.Result();
        result.setList(items);
        return result;
    }

    private DiscoverQuery currentQuery(int targetPage) {
        return new DiscoverQuery(mediaType, genreId, region, dateStart, dateEnd, sort, targetPage);
    }

    private void rebuildPage(boolean focusResults, View preservedFocus) {
        if (isInactive()) return;
        FocusedItem focusedItem = captureFocusedItem();
        int selectedPosition = mBinding.recycler.getSelectedPosition();
        mAdapter.clear();
        addFeaturedSections();
        addFilters();
        int resultStart = mAdapter.size();
        resultStartPosition = resultStart;
        addResults();
        boolean finished = featureFinished && queryFinished;
        if (finished) mBinding.progressLayout.showContent(true, mAdapter.size());
        else if (mAdapter.size() > 0) mBinding.progressLayout.showContent();
        if (focusResults && !requestState.isEmpty()) requestFocus(resultStart);
        else if (preservedFocus != null) restoreFilterFocus(selectedPosition);
        else if (focusedItem != null) restoreVodFocus(focusedItem);
        else if (selectedPosition >= 0) restorePosition(Math.min(selectedPosition, Math.max(0, mAdapter.size() - 1)));
    }

    private void addFeaturedSections() {
        List<Vod> hero = first(DiscoverApi.Row.TMDB_DAY, DiscoverApi.Row.TMDB_WEEK);
        if (!hero.isEmpty()) mAdapter.add(FeaturedVodRow.create(hero.subList(0, Math.min(5, hero.size()))));
        addLandscape(R.string.discover_today_trending, content.get(DiscoverApi.Row.TMDB_DAY));
        addLandscape(R.string.discover_week_trending, content.get(DiscoverApi.Row.TMDB_WEEK));
        List<DiscoverShelf> popular = new ArrayList<>();
        addShelf(popular, DiscoverShelf.Type.FEATURE, getString(R.string.discover_popular_movie), DiscoverFacet.TOP_MOVIE, content.get(DiscoverApi.Row.TMDB_POPULAR_MOVIE));
        addShelf(popular, DiscoverShelf.Type.FEATURE, getString(R.string.discover_popular_tv), DiscoverFacet.TOP_TV, content.get(DiscoverApi.Row.TMDB_POPULAR_TV));
        addObjectRow(popular, new DiscoverShelfPresenter(this));
        addPosterRow(R.string.discover_now_playing, content.get(DiscoverApi.Row.TMDB_NOW_PLAYING));
        List<DiscoverShelf> top = new ArrayList<>();
        addShelf(top, DiscoverShelf.Type.RANKING, getString(R.string.discover_top_tv), DiscoverFacet.TOP_TV, content.get(DiscoverApi.Row.TMDB_TOP_TV));
        addShelf(top, DiscoverShelf.Type.RANKING, getString(R.string.discover_top_movie), DiscoverFacet.TOP_MOVIE, content.get(DiscoverApi.Row.TMDB_TOP_MOVIE));
        addObjectRow(top, new DiscoverShelfPresenter(this));
    }

    private void addFilters() {
        mAdapter.add(R.string.discover_filter_title);
        addFilterRow(FILTER_MEDIA, mediaOptions());
        addFilterRow(FILTER_GENRE, genreOptions());
        addFilterRow(FILTER_REGION, regionOptions());
        addFilterRow(FILTER_YEAR, yearOptions());
        addFilterRow(FILTER_SORT, sortOptions());
        mAdapter.add(R.string.discover_filter_results);
    }

    private void addFilterRow(int row, List<DiscoverFilterOption> options) {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new DiscoverFilterPresenter(row, this));
        adapter.addAll(0, options);
        mAdapter.add(new ListRow(adapter));
    }

    private void addResults() {
        if (requestState.isEmpty()) {
            if (!queryFinished) mAdapter.add("discover_filter_progress");
            return;
        }
        int width = (ResUtil.getScreenWidth() - ResUtil.dp2px(32 + 14 * (COLUMN - 1))) / COLUMN;
        VodPresenter presenter = new VodPresenter(this, Style.rect(), new int[]{width, Math.round(width / 0.75f)});
        for (List<Vod> part : Lists.partition(requestState.getItems(), COLUMN)) {
            ArrayObjectAdapter row = new ArrayObjectAdapter(presenter);
            row.addAll(0, part);
            mAdapter.add(new ListRow(row));
        }
    }

    private List<DiscoverFilterOption> mediaOptions() {
        return List.of(
                option(DiscoverMediaKey.MOVIE, getString(R.string.discover_media_movie), mediaType),
                option(DiscoverMediaKey.TV, getString(R.string.discover_media_tv), mediaType));
    }

    private List<DiscoverFilterOption> genreOptions() {
        List<DiscoverFilterOption> items = new ArrayList<>();
        items.add(option("", getString(R.string.discover_all), genreId));
        for (DiscoverFacet item : genres) items.add(option(item.getId(), item.getName(), genreId));
        return items;
    }

    private List<DiscoverFilterOption> regionOptions() {
        return List.of(
                option("", getString(R.string.discover_all), region), option("CN", getString(R.string.discover_region_cn), region),
                option("HK", getString(R.string.discover_region_hk), region), option("TW", getString(R.string.discover_region_tw), region),
                option("US", getString(R.string.discover_region_us), region), option("JP", getString(R.string.discover_region_jp), region),
                option("KR", getString(R.string.discover_region_kr), region), option("GB", getString(R.string.discover_region_gb), region),
                option("FR", getString(R.string.discover_region_fr), region));
    }

    private List<DiscoverFilterOption> yearOptions() {
        int current = LocalDate.now().getYear();
        List<DiscoverFilterOption> items = new ArrayList<>();
        items.add(yearOption("", getString(R.string.discover_all), "", ""));
        for (int value = current; value >= current - 2; value--) items.add(yearOption(String.valueOf(value), String.valueOf(value), value + "-01-01", value + "-12-31"));
        items.add(yearOption("recent", getString(R.string.discover_year_recent, current - 7, current - 3), (current - 7) + "-01-01", (current - 3) + "-12-31"));
        int decade = (current / 10) * 10;
        for (int start = decade; start >= 2000; start -= 10) items.add(yearOption("decade_" + start, getString(R.string.discover_year_decade, start), start + "-01-01", (start + 9) + "-12-31"));
        items.add(yearOption("before_2000", getString(R.string.discover_year_before_2000), "", "1999-12-31"));
        return items;
    }

    private List<DiscoverFilterOption> sortOptions() {
        return List.of(
                option(DiscoverQuery.SORT_POPULAR, getString(R.string.discover_sort_popular), sort),
                option(DiscoverQuery.SORT_RATING, getString(R.string.discover_sort_rating), sort),
                option(DiscoverQuery.SORT_LATEST, getString(R.string.discover_sort_latest), sort));
    }

    private DiscoverFilterOption option(String value, String label, String selected) {
        return new DiscoverFilterOption(value, label, value.equals(selected));
    }

    private DiscoverFilterOption yearOption(String value, String label, String start, String end) {
        return new DiscoverFilterOption(value, label, start, end, start.equals(dateStart) && end.equals(dateEnd));
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

    private void requestFocus(int position) {
        mBinding.recycler.setSelectedPosition(Math.max(0, position));
        restorePosition(Math.max(0, position));
    }

    private void restorePosition(int position) {
        mBinding.recycler.postDelayed(() -> {
            if (isInactive()) return;
            RecyclerView.ViewHolder holder = mBinding.recycler.findViewHolderForAdapterPosition(position);
            View target = holder == null ? null : findFocusable(holder.itemView);
            if (target != null) target.requestFocus();
        }, 80);
    }

    private void restoreFilterFocus(int position) {
        mBinding.recycler.postDelayed(() -> {
            if (isInactive()) return;
            RecyclerView.ViewHolder holder = mBinding.recycler.findViewHolderForAdapterPosition(position);
            View target = holder == null ? null : findSelected(holder.itemView);
            if (target != null) target.requestFocus();
            else restorePosition(position);
        }, 80);
    }

    private FocusedItem captureFocusedItem() {
        if (resultStartPosition < 0) return null;
        View focused = getCurrentFocus();
        if (focused == null) return null;
        RecyclerView.ViewHolder rowHolder = mBinding.recycler.findContainingViewHolder(focused);
        if (rowHolder == null || rowHolder.getBindingAdapterPosition() < resultStartPosition) return null;
        List<Vod> values = requestState.getItems();
        int index = (rowHolder.getBindingAdapterPosition() - resultStartPosition) * COLUMN;
        RecyclerView row = findRecycler(rowHolder.itemView);
        if (row != null) {
            RecyclerView.ViewHolder itemHolder = row.findContainingViewHolder(focused);
            if (itemHolder != null) index += itemHolder.getBindingAdapterPosition();
        }
        if (index < 0 || index >= values.size()) return null;
        return new FocusedItem(values.get(index).getId(), index);
    }

    private RecyclerView findRecycler(View view) {
        if (view instanceof RecyclerView recycler && view != mBinding.recycler) return recycler;
        if (!(view instanceof android.view.ViewGroup group)) return null;
        for (int i = 0; i < group.getChildCount(); i++) {
            RecyclerView result = findRecycler(group.getChildAt(i));
            if (result != null) return result;
        }
        return null;
    }

    private void restoreVodFocus(FocusedItem focused) {
        int index = focused.index;
        List<Vod> values = requestState.getItems();
        for (int i = 0; i < values.size(); i++) if (values.get(i).getId().equals(focused.id)) { index = i; break; }
        int rowPosition = resultStartPosition + index / COLUMN;
        int column = index % COLUMN;
        int finalIndex = index;
        mBinding.recycler.setSelectedPosition(rowPosition);
        mBinding.recycler.postDelayed(() -> {
            if (isInactive()) return;
            RecyclerView.ViewHolder rowHolder = mBinding.recycler.findViewHolderForAdapterPosition(rowPosition);
            RecyclerView row = rowHolder == null ? null : findRecycler(rowHolder.itemView);
            RecyclerView.ViewHolder itemHolder = row == null ? null : row.findViewHolderForAdapterPosition(column);
            if (itemHolder != null) itemHolder.itemView.requestFocus();
            else if (finalIndex >= 0) restorePosition(rowPosition);
        }, 100);
    }

    private static final class FocusedItem {
        private final String id;
        private final int index;

        private FocusedItem(String id, int index) {
            this.id = id;
            this.index = index;
        }
    }

    private View findSelected(View view) {
        if (view == null || !view.isShown() || !view.isEnabled()) return null;
        if (view.isSelected() || (view instanceof android.widget.Checkable checkable && checkable.isChecked())) return view;
        if (!(view instanceof android.view.ViewGroup group)) return null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View result = findSelected(group.getChildAt(i));
            if (result != null) return result;
        }
        return null;
    }

    private View findFocusable(View view) {
        if (view == null || !view.isShown() || !view.isEnabled()) return null;
        if (view.isFocusable()) return view;
        if (!(view instanceof android.view.ViewGroup group)) return null;
        for (int i = 0; i < group.getChildCount(); i++) {
            View target = findFocusable(group.getChildAt(i));
            if (target != null) return target;
        }
        return null;
    }

    @Override
    public void onFilterClick(int row, DiscoverFilterOption option, View view) {
        boolean changed;
        switch (row) {
            case FILTER_MEDIA -> {
                changed = !mediaType.equals(option.getValue());
                if (changed) {
                    mediaType = option.getValue();
                    genreId = "";
                    genres = List.of();
                    loadGenres(mediaType, true);
                }
            }
            case FILTER_GENRE -> { changed = !genreId.equals(option.getValue()); genreId = option.getValue(); }
            case FILTER_REGION -> { changed = !region.equals(option.getValue()); region = option.getValue(); }
            case FILTER_YEAR -> { changed = !dateStart.equals(option.getStartDate()) || !dateEnd.equals(option.getEndDate()); dateStart = option.getStartDate(); dateEnd = option.getEndDate(); }
            case FILTER_SORT -> { changed = !sort.equals(option.getValue()); sort = option.getValue(); }
            default -> changed = false;
        }
        if (changed) refreshResults(view, false);
    }

    @Override
    public void onItemClick(Vod item) {
        openItem(item, null);
    }

    @Override
    public void onItemClick(Vod item, View poster) {
        openItem(item, poster);
    }

    private void openItem(Vod item, View poster) {
        DiscoverMediaKey key = DiscoverMediaKey.parse(item.getId());
        if (key == null) return;
        DiscoverDetailActivity.start(this, key, item.getName(), item.getPic(), item.getBackdrop(), item.getContent(), item.getYear(), item.getRemarks(), poster);
    }

    @Override
    public boolean onLongClick(Vod item) {
        return false;
    }

    @Override
    public void onShelfClick(DiscoverShelf shelf) {
        if (DiscoverFacet.TOP_TV.equals(shelf.getFacetKind())) mediaType = DiscoverMediaKey.TV;
        else mediaType = DiscoverMediaKey.MOVIE;
        genreId = "";
        sort = shelf.getType() == DiscoverShelf.Type.RANKING ? DiscoverQuery.SORT_RATING : DiscoverQuery.SORT_POPULAR;
        loadGenres(mediaType, true);
        refreshResults(null, false);
    }

    @Override
    public boolean onLoadMore(String ignored) {
        if (isInactive() || !queryFinished || page >= totalPages) return false;
        queryFinished = false;
        loadQuery(currentQuery(page + 1), queryGeneration);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (featureFinished && queryFinished && mAdapter.size() == 0 && event.getAction() == KeyEvent.ACTION_UP &&
                (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            loadFeatured();
            refreshResults(null, true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        DiscoverApi.cancel(requestTag);
        if (scroller != null) mBinding.recycler.removeOnScrollListener(scroller);
        super.onDestroy();
    }

    private boolean isInactive() {
        return isFinishing() || isDestroyed();
    }
}
