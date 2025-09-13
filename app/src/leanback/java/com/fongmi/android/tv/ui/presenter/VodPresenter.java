package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterVodRectBinding;
import com.fongmi.android.tv.ui.base.BaseVodHolder;
import com.fongmi.android.tv.ui.base.ViewType;
import com.fongmi.android.tv.ui.holder.VodRectHolder;

public class VodPresenter extends Presenter {

    private final OnClickListener mListener;
    private final Style style;
    private final int[] size;

    public VodPresenter(OnClickListener listener) {
        this(listener, Style.rect());
    }

    public VodPresenter(OnClickListener listener, Style style) {
        this.mListener = listener;
        this.style = style;
        this.size = Product.getSpec(style);
    }

    public interface OnClickListener {

        void onItemClick(Vod item);

        boolean onLongClick(Vod item);
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new VodRectHolder(AdapterVodRectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mListener).size(size);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
        ((BaseVodHolder) viewHolder).initView((Vod) object);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }
}