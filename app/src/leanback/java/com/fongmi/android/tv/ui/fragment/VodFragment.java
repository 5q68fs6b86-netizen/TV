package com.fongmi.android.tv.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Page;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentVodBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.CollectActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.VodAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.CustomScroller;
import com.github.catvod.utils.Prefers;
import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VodFragment extends BaseFragment implements VodAdapter.OnClickListener {

    private HashMap<String, String> mExtends;
    private FragmentVodBinding mBinding;
    private SiteViewModel mViewModel;
    private VodAdapter mAdapter;
    private List<Filter> mFilters;
    private List<Page> mPages;
    private Page mPage;

    public static VodFragment newInstance(String key, String typeId, Style style, HashMap<String, String> extend, boolean folder) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putString("typeId", typeId);
        args.putBoolean("folder", folder);
        args.putParcelable("style", style);
        args.putSerializable("extend", extend);
        VodFragment fragment = new VodFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getKey() {
        return getArguments().getString("key");
    }

    private String getTypeId() {
        return mPages.isEmpty() ? getArguments().getString("typeId") : getLastPage().getVodId();
    }

    private List<Filter> getFilter() {
        return Filter.arrayFrom(Prefers.getString("filter_" + getKey() + "_" + getTypeId()));
    }

    private HashMap<String, String> getExtend() {
        Serializable extend = getArguments().getSerializable("extend");
        return extend == null ? new HashMap<>() : (HashMap<String, String>) extend;
    }

    private boolean isFolder() {
        return getArguments().getBoolean("folder");
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private boolean isIndexs() {
        return getSite().isIndexs();
    }

    private Page getLastPage() {
        return mPages.get(mPages.size() - 1);
    }

    private Style getStyle() {
        return isFolder() ? Style.rect() : getSite().getStyle(mPages.isEmpty() ? getArguments().getParcelable("style") : getLastPage().getStyle());
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentVodBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mPages = new ArrayList<>();
        mExtends = getExtend();
        mFilters = getFilter();
        setRecyclerView();
        setViewModel();
        setFilters();
    }

    @Override
    protected void initData() {
        getVideo();
    }

    private void setRecyclerView() {
        mBinding.recycler.setHasFixedSize(true);
        mBinding.recycler.setLayoutManager(new GridLayoutManager(getContext(), Product.getColumn(getStyle())));
        mBinding.recycler.setAdapter(mAdapter = new VodAdapter(this, new ArrayList<>(), getStyle()));
        mBinding.recycler.addOnScrollListener(new CustomScroller() {
            @Override
            public void onLoadMore(String page) {
                mViewModel.categoryContent(getKey(), getTypeId(), page, true, mExtends);
            }
        });
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.result.observe(getViewLifecycleOwner(), this::setAdapter);
    }

    private void setFilters() {
        mBinding.filter.removeAllViews();
        for (Filter filter : mFilters) {
            for (Value value : filter.getValue()) {
                Chip chip = new Chip(getContext());
                chip.setText(value.getN());
                chip.setCheckable(true);
                chip.setOnClickListener(v -> setClick(filter.getKey(), value));
                if (mExtends.containsKey(filter.getKey()) && mExtends.get(filter.getKey()).equals(value.getV())) chip.setChecked(true);
                mBinding.filter.addView(chip);
            }
        }
    }

    private void setClick(String key, Value item) {
        if (item.isActivated()) mExtends.put(key, item.getV());
        else mExtends.remove(key);
        onRefresh();
    }

    private void getVideo() {
        mAdapter.clear();
        mViewModel.categoryContent(getKey(), getTypeId(), "1", true, mExtends);
    }

    private void onRefresh() {
        getVideo();
    }

    private void setAdapter(Result result) {
        mAdapter.addAll(result.getList());
    }

    public boolean canBack() {
        return !mPages.isEmpty();
    }

    public void goBack() {
        mPages.remove(mPage = getLastPage());
        onRefresh();
    }

    public boolean goRoot() {
        if (mPages.isEmpty()) return false;
        mPages.clear();
        getVideo();
        return true;
    }

    @Override
    public void onItemClick(Vod item) {
        if (item.isAction()) {
            mViewModel.action(getKey(), item.getAction());
        } else if (item.isFolder()) {
            mPages.add(Page.get(item, mBinding.recycler.getLayoutManager() != null ? ((GridLayoutManager) mBinding.recycler.getLayoutManager()).findFirstVisibleItemPosition() : 0));
            getVideo();
        } else {
            if (isIndexs()) CollectActivity.start(getActivity(), item.getVodName());
            else if (!isFolder()) VideoActivity.start(getActivity(), getKey(), item.getVodId(), item.getVodName(), item.getVodPic());
            else VideoActivity.start(getActivity(), getKey(), item.getVodId(), item.getVodName(), item.getVodPic(), item.getVodName());
        }
    }

    @Override
    public boolean onLongClick(Vod item) {
        CollectActivity.start(getActivity(), item.getVodName());
        return true;
    }
}
