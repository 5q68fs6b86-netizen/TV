package com.fongmi.android.tv.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.ActivityDiscoverBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.CustomRowPresenter;
import com.fongmi.android.tv.ui.custom.CustomSelector;
import com.fongmi.android.tv.ui.dialog.DiscoverDialog;
import com.fongmi.android.tv.ui.presenter.HeaderPresenter;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiscoverActivity extends BaseActivity implements VodPresenter.OnClickListener {

    private static final int ROW_SPACING = 16;

    private ActivityDiscoverBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private Map<DiscoverApi.Row, Integer> mHeaders;
    private Map<DiscoverApi.Row, ArrayObjectAdapter> mRowAdapters;
    private int mPending;
    private boolean mShown;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DiscoverActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityDiscoverBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.progressLayout.showProgress();
        setRecyclerView();
        setRows();
        getContent();
    }

    @SuppressLint("RestrictedApi")
    private void setRecyclerView() {
        CustomSelector selector = new CustomSelector();
        selector.addPresenter(Integer.class, new HeaderPresenter());
        selector.addPresenter(ListRow.class, new CustomRowPresenter(ROW_SPACING, FocusHighlight.ZOOM_FACTOR_NONE), VodPresenter.class);
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(selector)));
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(10));
    }

    private void setRows() {
        mHeaders = new LinkedHashMap<>();
        mHeaders.put(DiscoverApi.Row.DOUBAN_HOT_MOVIE, R.string.discover_douban_hot_movie);
        mHeaders.put(DiscoverApi.Row.DOUBAN_HOT_TV, R.string.discover_douban_hot_tv);
        mHeaders.put(DiscoverApi.Row.DOUBAN_NEW_MOVIE, R.string.discover_douban_new_movie);
        mHeaders.put(DiscoverApi.Row.TMDB_TRENDING, R.string.discover_tmdb_trending);
        mHeaders.put(DiscoverApi.Row.TMDB_TOP_MOVIE, R.string.discover_tmdb_top_movie);
        mHeaders.put(DiscoverApi.Row.TMDB_TOP_TV, R.string.discover_tmdb_top_tv);
        mRowAdapters = new EnumMap<>(DiscoverApi.Row.class);
        VodPresenter presenter = new VodPresenter(this, Style.rect());
        for (Map.Entry<DiscoverApi.Row, Integer> entry : mHeaders.entrySet()) {
            ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenter);
            mRowAdapters.put(entry.getKey(), adapter);
            mAdapter.add(entry.getValue());
            mAdapter.add(new ListRow(adapter));
        }
    }

    private void getContent() {
        mPending = mHeaders.size();
        for (DiscoverApi.Row row : mHeaders.keySet()) {
            DiscoverApi.fetch(row, BuildConfig.TMDB_API_KEY, new DiscoverApi.Listener() {
                @Override
                public void onSuccess(DiscoverApi.Row row, List<Vod> items) {
                    setRowContent(row, items);
                }

                @Override
                public void onError(DiscoverApi.Row row, Exception e) {
                    setRowContent(row, List.of());
                }
            });
        }
    }

    private void setRowContent(DiscoverApi.Row row, List<Vod> items) {
        if (isFinishing()) return;
        mPending--;
        if (items.isEmpty()) removeRow(row);
        else {
            ArrayObjectAdapter adapter = mRowAdapters.get(row);
            if (adapter != null) adapter.setItems(items, null);
            showContent();
        }
        if (mPending == 0 && !mShown) mBinding.progressLayout.showContent(true, 0);
    }

    private void removeRow(DiscoverApi.Row row) {
        Integer header = mHeaders.get(row);
        if (header == null) return;
        int index = mAdapter.indexOf(header);
        if (index >= 0) mAdapter.removeItems(index, 2);
        mRowAdapters.remove(row);
    }

    private void showContent() {
        if (mShown) return;
        mShown = true;
        mBinding.progressLayout.showContent();
        requestFocus();
    }

    private void requestFocus() {
        mBinding.recycler.postDelayed(() -> {
            if (mBinding.recycler.hasFocus()) return;
            RecyclerView.ViewHolder holder = mBinding.recycler.findViewHolderForAdapterPosition(mBinding.recycler.getSelectedPosition());
            View focus = holder == null ? null : findFocusable(holder.itemView);
            if (focus != null && focus.requestFocus()) return;
            if (mBinding.recycler.isShown()) mBinding.recycler.requestFocus();
        }, 50);
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
    public void onItemClick(Vod item) {
        DiscoverDialog.create(item).show(this);
    }

    @Override
    public boolean onLongClick(Vod item) {
        return false;
    }

    @Override
    protected void onDestroy() {
        DiscoverApi.cancel();
        super.onDestroy();
    }
}
