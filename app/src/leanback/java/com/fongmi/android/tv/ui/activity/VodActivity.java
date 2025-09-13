package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.ActivityVodBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.fragment.VodFragment;
import com.fongmi.android.tv.utils.KeyUtil;
import com.github.catvod.utils.Prefers;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VodActivity extends BaseActivity {

    private ActivityVodBinding mBinding;
    private PageAdapter mPageAdapter;
    private List<Class> mTypes;

    public static void start(Activity activity, Result result) {
        start(activity, VodConfig.get().getHome().getKey(), result);
    }

    public static void start(Activity activity, String key, Result result) {
        if (result == null || result.getTypes().isEmpty()) return;
        Intent intent = new Intent(activity, VodActivity.class);
        intent.putExtra("key", key);
        intent.putExtra("result", result);
        for (Map.Entry<String, List<Filter>> entry : result.getFilters().entrySet()) Prefers.put("filter_" + key + "_" + entry.getKey(), App.gson().toJson(entry.getValue()));
        activity.startActivity(intent);
    }

    private String getKey() {
        return getIntent().getStringExtra("key");
    }

    private Result getResult() {
        return getIntent().getParcelableExtra("result");
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityVodBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mTypes = getTypes(getResult());
        setPager();
        setTab();
    }

    private List<Class> getTypes(Result result) {
        List<Class> items = new ArrayList<>();
        for (String cate : getSite().getCategories()) for (Class item : result.getTypes()) if (cate.equals(item.getTypeName())) items.add(item);
        return items;
    }

    private void setPager() {
        mBinding.pager.setAdapter(mPageAdapter = new PageAdapter(this));
    }

    private void setTab() {
        new TabLayoutMediator(mBinding.tab, mBinding.pager, (tab, position) -> tab.setText(mTypes.get(position).getTypeName())).attach();
    }

    private VodFragment getFragment() {
        return (VodFragment) getSupportFragmentManager().findFragmentByTag("f" + mBinding.pager.getCurrentItem());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyUtil.isBackKey(event) && event.isLongPress() && getFragment().goRoot()) return true;
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (getFragment().canBack()) getFragment().goBack();
        else super.onBackPressed();
    }

    class PageAdapter extends FragmentStateAdapter {

        public PageAdapter(@NonNull FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Class type = mTypes.get(position);
            return VodFragment.newInstance(getKey(), type.getTypeId(), type.getStyle(), type.getExtend(false), "1".equals(type.getTypeFlag()));
        }

        @Override
        public int getItemCount() {
            return mTypes.size();
        }
    }
}
