package com.fongmi.android.tv.ui.component;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.ImgUtil;

/**
 * Vod 专用卡片 Presenter
 * 
 * <p>继承自 TVCardPresenter，专门用于显示 Vod 数据类型
 */
public class VodCardPresenter extends TVCardPresenter {

    // 点击监听器
    private OnVodClickListener vodClickListener;
    private OnVodLongClickListener vodLongClickListener;
    
    /**
     * Vod 点击监听接口
     */
    public interface OnVodClickListener {
        void onVodClick(Vod vod);
    }
    
    /**
     * Vod 长按监听接口
     */
    public interface OnVodLongClickListener {
        boolean onVodLongClick(Vod vod);
    }
    
    // ==================== 构造函数 ====================
    
    public VodCardPresenter() {
        super();
        setupInternalListeners();
    }
    
    public VodCardPresenter(CardType cardType) {
        super(cardType);
        setupInternalListeners();
    }
    
    public VodCardPresenter(CardType cardType, CardSize cardSize) {
        super(cardType, cardSize);
        setupInternalListeners();
    }
    
    private void setupInternalListeners() {
        // 设置内部点击监听器
        setOnItemClickListener((item, view) -> {
            if (item instanceof Vod && vodClickListener != null) {
                vodClickListener.onVodClick((Vod) item);
            }
        });
        
        setOnItemLongClickListener((item, view) -> {
            if (item instanceof Vod && vodLongClickListener != null) {
                return vodLongClickListener.onVodLongClick((Vod) item);
            }
            return false;
        });
    }
    
    // ==================== 绑定数据 ====================
    
    @Override
    protected void onBindItem(@NonNull ViewHolder holder, @NonNull Object item) {
        if (!(item instanceof Vod)) return;
        
        Vod vod = (Vod) item;
        
        // 设置标题
        holder.setTitle(vod.getVodName());
        
        // 设置徽章
        String year = vod.getVodYear();
        if (year != null && !year.isEmpty()) {
            holder.setBadgeTopLeft(year);
        }
        
        String siteName = vod.getSiteName();
        if (siteName != null && !siteName.isEmpty() && vod.getSiteVisible() == android.view.View.VISIBLE) {
            holder.setBadgeTopRight(siteName);
        }
        
        String remarks = vod.getVodRemarks();
        if (remarks != null && !remarks.isEmpty() && vod.getRemarkVisible() == android.view.View.VISIBLE) {
            holder.setBadgeBottomRight(remarks);
        }
        
        // 加载图片
        ImageView imageView = holder.getImageView();
        if (imageView != null) {
            loadVodImage(vod, imageView);
        }
    }
    
    /**
     * 加载 Vod 图片
     * 可以在子类中重写以自定义图片加载逻辑
     */
    protected void loadVodImage(Vod vod, ImageView imageView) {
        // 根据卡片类型选择不同的加载方式
        switch (getCardType()) {
            case LANDSCAPE:
                ImgUtil.rect(vod.getVodName(), vod.getVodPic(), imageView);
                break;
            case SQUARE:
                ImgUtil.rect(vod.getVodName(), vod.getVodPic(), imageView);
                break;
            default: // POSTER
                ImgUtil.rect(vod.getVodName(), vod.getVodPic(), imageView);
                break;
        }
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 设置 Vod 点击监听器
     */
    public VodCardPresenter setOnVodClickListener(OnVodClickListener listener) {
        this.vodClickListener = listener;
        return this;
    }
    
    /**
     * 设置 Vod 长按监听器
     */
    public VodCardPresenter setOnVodLongClickListener(OnVodLongClickListener listener) {
        this.vodLongClickListener = listener;
        return this;
    }
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建海报卡片 Presenter
     */
    public static VodCardPresenter createPoster() {
        return new VodCardPresenter(CardType.POSTER, CardSize.MEDIUM);
    }
    
    /**
     * 创建小海报卡片 Presenter
     */
    public static VodCardPresenter createSmallPoster() {
        return new VodCardPresenter(CardType.POSTER, CardSize.SMALL);
    }
    
    /**
     * 创建大海报卡片 Presenter
     */
    public static VodCardPresenter createLargePoster() {
        return new VodCardPresenter(CardType.POSTER, CardSize.LARGE);
    }
    
    /**
     * 创建横版卡片 Presenter
     */
    public static VodCardPresenter createLandscape() {
        return new VodCardPresenter(CardType.LANDSCAPE, CardSize.MEDIUM);
    }
    
    /**
     * 创建方形卡片 Presenter
     */
    public static VodCardPresenter createSquare() {
        return new VodCardPresenter(CardType.SQUARE, CardSize.MEDIUM);
    }
}