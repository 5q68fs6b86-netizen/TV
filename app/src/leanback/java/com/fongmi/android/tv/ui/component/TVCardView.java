package com.fongmi.android.tv.ui.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.fongmi.android.tv.R;

/**
 * TV 卡片视图组件
 * 
 * <p>提供统一的卡片样式，包含：
 * <ul>
 *   <li>海报图片显示</li>
 *   <li>标题/副标题文本</li>
 *   <li>徽章显示（年份、质量等）</li>
 *   <li>焦点动画效果</li>
 *   <li>支持多种卡片类型（海报、横版、方形）</li>
 * </ul>
 * 
 * <p>使用方式：
 * <pre>
 * &lt;com.fongmi.android.tv.ui.component.TVCardView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:cardType="poster"
 *     app:cardSize="medium" /&gt;
 * </pre>
 */
public class TVCardView extends FrameLayout {

    /**
     * 卡片类型枚举
     */
    public enum CardType {
        POSTER(0),      // 竖版海报 2:3
        LANDSCAPE(1),   // 横版 16:9
        SQUARE(2);      // 方形 1:1
        
        private final int value;
        
        CardType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static CardType fromValue(int value) {
            for (CardType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return POSTER;
        }
    }
    
    /**
     * 卡片尺寸枚举
     */
    public enum CardSize {
        SMALL(0),
        MEDIUM(1),
        LARGE(2);
        
        private final int value;
        
        CardSize(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static CardSize fromValue(int value) {
            for (CardSize size : values()) {
                if (size.value == value) {
                    return size;
                }
            }
            return MEDIUM;
        }
    }
    
    // 子视图
    private CardView cardContainer;
    private ImageView imageView;
    private TextView titleView;
    private TextView subtitleView;
    private TextView badgeTopLeft;
    private TextView badgeTopRight;
    private TextView badgeBottomRight;
    private View scrimView;
    private View focusOverlay;
    
    // 配置
    private CardType cardType = CardType.POSTER;
    private CardSize cardSize = CardSize.MEDIUM;
    private boolean showTitle = true;
    private boolean showSubtitle = false;
    private boolean showScrim = true;
    
    // 焦点助手
    private TVFocusHelper focusHelper;
    
    public TVCardView(@NonNull Context context) {
        this(context, null);
    }
    
    public TVCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TVCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, @Nullable AttributeSet attrs) {
        // 解析自定义属性
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TVCardView);
            try {
                cardType = CardType.fromValue(ta.getInt(R.styleable.TVCardView_cardType, CardType.POSTER.getValue()));
                cardSize = CardSize.fromValue(ta.getInt(R.styleable.TVCardView_cardSize, CardSize.MEDIUM.getValue()));
                showTitle = ta.getBoolean(R.styleable.TVCardView_showTitle, true);
                showSubtitle = ta.getBoolean(R.styleable.TVCardView_showSubtitle, false);
                showScrim = ta.getBoolean(R.styleable.TVCardView_showScrim, true);
            } finally {
                ta.recycle();
            }
        }
        
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.component_tv_card, this, true);
        
        // 绑定视图
        bindViews();
        
        // 设置尺寸
        applyCardSize();
        
        // 设置焦点
        setupFocus();
    }
    
    private void bindViews() {
        cardContainer = findViewById(R.id.card_container);
        imageView = findViewById(R.id.image);
        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        badgeTopLeft = findViewById(R.id.badge_top_left);
        badgeTopRight = findViewById(R.id.badge_top_right);
        badgeBottomRight = findViewById(R.id.badge_bottom_right);
        scrimView = findViewById(R.id.scrim);
        focusOverlay = findViewById(R.id.focus_overlay);
        
        // 应用显示设置
        if (titleView != null) titleView.setVisibility(showTitle ? VISIBLE : GONE);
        if (subtitleView != null) subtitleView.setVisibility(showSubtitle ? VISIBLE : GONE);
        if (scrimView != null) scrimView.setVisibility(showScrim ? VISIBLE : GONE);
    }
    
    private void applyCardSize() {
        int width, height;
        Context context = getContext();
        
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
        
        if (cardContainer != null) {
            LayoutParams params = (LayoutParams) cardContainer.getLayoutParams();
            params.width = width;
            params.height = height;
            cardContainer.setLayoutParams(params);
        }
    }
    
    private void setupFocus() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        // 创建焦点助手
        focusHelper = TVFocusHelper.createForCard();
        
        // 设置焦点监听
        setOnFocusChangeListener((v, hasFocus) -> {
            focusHelper.handleFocusChange(cardContainer != null ? cardContainer : v, hasFocus);
            
            // 更新焦点叠加层
            if (focusOverlay != null) {
                focusOverlay.setActivated(hasFocus);
            }
        });
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 设置卡片图片
     */
    public TVCardView setImage(Drawable drawable) {
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
        }
        return this;
    }
    
    /**
     * 设置卡片图片资源
     */
    public TVCardView setImageResource(@DrawableRes int resId) {
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
        return this;
    }
    
    /**
     * 获取图片视图（用于 Glide 等加载库）
     */
    public ImageView getImageView() {
        return imageView;
    }
    
    /**
     * 设置标题
     */
    public TVCardView setTitle(CharSequence title) {
        if (titleView != null) {
            titleView.setText(title);
            titleView.setVisibility(title != null && title.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置副标题
     */
    public TVCardView setSubtitle(CharSequence subtitle) {
        if (subtitleView != null) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(subtitle != null && subtitle.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置左上角徽章
     */
    public TVCardView setBadgeTopLeft(CharSequence text) {
        if (badgeTopLeft != null) {
            badgeTopLeft.setText(text);
            badgeTopLeft.setVisibility(text != null && text.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置右上角徽章
     */
    public TVCardView setBadgeTopRight(CharSequence text) {
        if (badgeTopRight != null) {
            badgeTopRight.setText(text);
            badgeTopRight.setVisibility(text != null && text.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置右下角徽章（通常用于质量标签）
     */
    public TVCardView setBadgeBottomRight(CharSequence text) {
        if (badgeBottomRight != null) {
            badgeBottomRight.setText(text);
            badgeBottomRight.setVisibility(text != null && text.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置卡片类型
     */
    public TVCardView setCardType(CardType type) {
        if (this.cardType != type) {
            this.cardType = type;
            applyCardSize();
        }
        return this;
    }
    
    /**
     * 设置卡片尺寸
     */
    public TVCardView setCardSize(CardSize size) {
        if (this.cardSize != size) {
            this.cardSize = size;
            applyCardSize();
        }
        return this;
    }
    
    /**
     * 设置是否显示遮罩
     */
    public TVCardView setShowScrim(boolean show) {
        this.showScrim = show;
        if (scrimView != null) {
            scrimView.setVisibility(show ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置焦点回调
     */
    public TVCardView setFocusCallback(@Nullable TVFocusHelper.OnFocusChangeCallback callback) {
        if (focusHelper != null) {
            focusHelper.setCallback(callback);
        }
        return this;
    }
    
    /**
     * 获取卡片容器
     */
    public CardView getCardContainer() {
        return cardContainer;
    }
    
    /**
     * 重置卡片状态
     */
    public void reset() {
        if (imageView != null) imageView.setImageDrawable(null);
        if (titleView != null) titleView.setText(null);
        if (subtitleView != null) subtitleView.setText(null);
        if (badgeTopLeft != null) {
            badgeTopLeft.setText(null);
            badgeTopLeft.setVisibility(GONE);
        }
        if (badgeTopRight != null) {
            badgeTopRight.setText(null);
            badgeTopRight.setVisibility(GONE);
        }
        if (badgeBottomRight != null) {
            badgeBottomRight.setText(null);
            badgeBottomRight.setVisibility(GONE);
        }
    }
}