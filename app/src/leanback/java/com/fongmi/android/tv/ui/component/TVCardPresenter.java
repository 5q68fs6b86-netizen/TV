package com.fongmi.android.tv.ui.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.R;

/**
 * TV 卡片 Presenter
 * 
 * <p>用于 Leanback 库的卡片呈现器，支持：
 * <ul>
 *   <li>多种卡片类型（海报、横版、方形）</li>
 *   <li>统一的焦点动画效果</li>
 *   <li>可配置的卡片尺寸</li>
 * </ul>
 */
public class TVCardPresenter extends Presenter {

    /**
     * 卡片类型枚举
     */
    public enum CardType {
        POSTER,      // 竖版海报 2:3
        LANDSCAPE,   // 横版 16:9
        SQUARE       // 方形 1:1
    }
    
    /**
     * 卡片尺寸枚举
     */
    public enum CardSize {
        SMALL,
        MEDIUM,
        LARGE
    }
    
    // 配置
    private CardType cardType = CardType.POSTER;
    private CardSize cardSize = CardSize.MEDIUM;
    private boolean showTitle = true;
    private boolean showScrim = true;
    
    // 点击监听器
    @Nullable
    private OnItemClickListener clickListener;
    @Nullable
    private OnItemLongClickListener longClickListener;
    
    /**
     * 点击监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(Object item, View view);
    }
    
    /**
     * 长按监听接口
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Object item, View view);
    }
    
    // ==================== ViewHolder ====================
    
    public static class ViewHolder extends Presenter.ViewHolder {
        private final View rootView;
        private final CardView cardContainer;
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView badgeTopLeft;
        private final TextView badgeTopRight;
        private final TextView badgeBottomRight;
        private final View scrimView;
        private final TVFocusHelper focusHelper;
        
        private Object boundItem;
        
        public ViewHolder(View view) {
            super(view);
            this.rootView = view;
            this.cardContainer = view.findViewById(R.id.card_container);
            this.imageView = view.findViewById(R.id.image);
            this.titleView = view.findViewById(R.id.title);
            this.subtitleView = view.findViewById(R.id.subtitle);
            this.badgeTopLeft = view.findViewById(R.id.badge_top_left);
            this.badgeTopRight = view.findViewById(R.id.badge_top_right);
            this.badgeBottomRight = view.findViewById(R.id.badge_bottom_right);
            this.scrimView = view.findViewById(R.id.scrim);
            this.focusHelper = TVFocusHelper.createForCard();
            
            // 设置焦点处理
            setupFocus();
        }
        
        private void setupFocus() {
            rootView.setFocusable(true);
            rootView.setFocusableInTouchMode(true);
            
            rootView.setOnFocusChangeListener((v, hasFocus) -> {
                focusHelper.handleFocusChange(cardContainer != null ? cardContainer : v, hasFocus);
            });
        }
        
        public View getRootView() {
            return rootView;
        }
        
        public CardView getCardContainer() {
            return cardContainer;
        }
        
        public ImageView getImageView() {
            return imageView;
        }
        
        public TextView getTitleView() {
            return titleView;
        }
        
        public TextView getSubtitleView() {
            return subtitleView;
        }
        
        public TextView getBadgeTopLeft() {
            return badgeTopLeft;
        }
        
        public TextView getBadgeTopRight() {
            return badgeTopRight;
        }
        
        public TextView getBadgeBottomRight() {
            return badgeBottomRight;
        }
        
        public View getScrimView() {
            return scrimView;
        }
        
        public Object getBoundItem() {
            return boundItem;
        }
        
        void setBoundItem(Object item) {
            this.boundItem = item;
        }
        
        /**
         * 设置标题
         */
        public void setTitle(CharSequence title) {
            if (titleView != null) {
                titleView.setText(title);
                titleView.setVisibility(title != null && title.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        /**
         * 设置副标题
         */
        public void setSubtitle(CharSequence subtitle) {
            if (subtitleView != null) {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(subtitle != null && subtitle.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        /**
         * 设置左上角徽章
         */
        public void setBadgeTopLeft(CharSequence text) {
            if (badgeTopLeft != null) {
                badgeTopLeft.setText(text);
                badgeTopLeft.setVisibility(text != null && text.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        /**
         * 设置右上角徽章
         */
        public void setBadgeTopRight(CharSequence text) {
            if (badgeTopRight != null) {
                badgeTopRight.setText(text);
                badgeTopRight.setVisibility(text != null && text.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        /**
         * 设置右下角徽章
         */
        public void setBadgeBottomRight(CharSequence text) {
            if (badgeBottomRight != null) {
                badgeBottomRight.setText(text);
                badgeBottomRight.setVisibility(text != null && text.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        /**
         * 重置视图状态
         */
        public void reset() {
            if (imageView != null) imageView.setImageDrawable(null);
            if (titleView != null) titleView.setText(null);
            if (subtitleView != null) {
                subtitleView.setText(null);
                subtitleView.setVisibility(View.GONE);
            }
            if (badgeTopLeft != null) {
                badgeTopLeft.setText(null);
                badgeTopLeft.setVisibility(View.GONE);
            }
            if (badgeTopRight != null) {
                badgeTopRight.setText(null);
                badgeTopRight.setVisibility(View.GONE);
            }
            if (badgeBottomRight != null) {
                badgeBottomRight.setText(null);
                badgeBottomRight.setVisibility(View.GONE);
            }
            boundItem = null;
        }
    }
    
    // ==================== 构造函数 ====================
    
    public TVCardPresenter() {
        super();
    }
    
    public TVCardPresenter(CardType cardType) {
        this();
        this.cardType = cardType;
    }
    
    public TVCardPresenter(CardType cardType, CardSize cardSize) {
        this(cardType);
        this.cardSize = cardSize;
    }
    
    // ==================== Presenter 方法 ====================
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.component_tv_card, parent, false);
        
        // 应用卡片尺寸
        applyCardSize(context, view);
        
        ViewHolder holder = new ViewHolder(view);
        
        // 设置点击监听
        if (clickListener != null) {
            view.setOnClickListener(v -> clickListener.onItemClick(holder.getBoundItem(), v));
        }
        
        if (longClickListener != null) {
            view.setOnLongClickListener(v -> longClickListener.onItemLongClick(holder.getBoundItem(), v));
        }
        
        // 设置遮罩显示
        View scrim = view.findViewById(R.id.scrim);
        if (scrim != null) {
            scrim.setVisibility(showScrim ? View.VISIBLE : View.GONE);
        }
        
        return holder;
    }
    
    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.setBoundItem(item);
        
        // 子类需要重写此方法来绑定具体数据
        onBindItem(holder, item);
    }
    
    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.reset();
    }
    
    /**
     * 绑定数据到 ViewHolder
     * 子类应重写此方法以绑定具体的数据类型
     */
    protected void onBindItem(@NonNull ViewHolder holder, @NonNull Object item) {
        // 默认实现：什么都不做
        // 子类应重写此方法
    }
    
    // ==================== 配置方法 ====================
    
    private void applyCardSize(Context context, View view) {
        CardView cardContainer = view.findViewById(R.id.card_container);
        if (cardContainer == null) return;
        
        int width, height;
        
        switch (cardType) {
            case LANDSCAPE:
                switch (cardSize) {
                    case SMALL:
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_width_sm);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_height_sm);
                        break;
                    case LARGE:
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_width_lg);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_height_lg);
                        break;
                    default: // MEDIUM
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_width_md);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_landscape_height_md);
                        break;
                }
                break;
                
            case SQUARE:
                switch (cardSize) {
                    case SMALL:
                        width = height = (int) context.getResources().getDimension(R.dimen.tv_card_square_sm);
                        break;
                    case LARGE:
                        width = height = (int) context.getResources().getDimension(R.dimen.tv_card_square_lg);
                        break;
                    default: // MEDIUM
                        width = height = (int) context.getResources().getDimension(R.dimen.tv_card_square_md);
                        break;
                }
                break;
                
            default: // POSTER
                switch (cardSize) {
                    case SMALL:
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_poster_width_sm);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_poster_height_sm);
                        break;
                    case LARGE:
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_poster_width_lg);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_poster_height_lg);
                        break;
                    default: // MEDIUM
                        width = (int) context.getResources().getDimension(R.dimen.tv_card_poster_width_md);
                        height = (int) context.getResources().getDimension(R.dimen.tv_card_poster_height_md);
                        break;
                }
                break;
        }
        
        ViewGroup.LayoutParams params = cardContainer.getLayoutParams();
        params.width = width;
        params.height = height;
        cardContainer.setLayoutParams(params);
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 设置卡片类型
     */
    public TVCardPresenter setCardType(CardType type) {
        this.cardType = type;
        return this;
    }
    
    /**
     * 设置卡片尺寸
     */
    public TVCardPresenter setCardSize(CardSize size) {
        this.cardSize = size;
        return this;
    }
    
    /**
     * 设置是否显示标题
     */
    public TVCardPresenter setShowTitle(boolean show) {
        this.showTitle = show;
        return this;
    }
    
    /**
     * 设置是否显示遮罩
     */
    public TVCardPresenter setShowScrim(boolean show) {
        this.showScrim = show;
        return this;
    }
    
    /**
     * 设置点击监听器
     */
    public TVCardPresenter setOnItemClickListener(@Nullable OnItemClickListener listener) {
        this.clickListener = listener;
        return this;
    }
    
    /**
     * 设置长按监听器
     */
    public TVCardPresenter setOnItemLongClickListener(@Nullable OnItemLongClickListener listener) {
        this.longClickListener = listener;
        return this;
    }
    
    // Getters
    public CardType getCardType() {
        return cardType;
    }
    
    public CardSize getCardSize() {
        return cardSize;
    }
    
    public boolean isShowTitle() {
        return showTitle;
    }
    
    public boolean isShowScrim() {
        return showScrim;
    }
}