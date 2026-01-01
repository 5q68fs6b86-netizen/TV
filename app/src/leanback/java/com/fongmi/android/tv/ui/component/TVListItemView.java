package com.fongmi.android.tv.ui.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.R;

/**
 * TV 列表项视图组件
 * 
 * <p>用于设置页面和其他列表项，包含：
 * <ul>
 *   <li>图标</li>
 *   <li>主标题和副标题</li>
 *   <li>右侧箭头或开关</li>
 *   <li>焦点动画效果</li>
 * </ul>
 */
public class TVListItemView extends FrameLayout {

    /**
     * 列表项高度枚举
     */
    public enum ItemHeight {
        SMALL(0),
        MEDIUM(1),
        LARGE(2);
        
        private final int value;
        
        ItemHeight(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static ItemHeight fromValue(int value) {
            for (ItemHeight h : values()) {
                if (h.value == value) {
                    return h;
                }
            }
            return MEDIUM;
        }
    }
    
    // 子视图
    private View rootContainer;
    private ImageView iconView;
    private TextView titleView;
    private TextView subtitleView;
    private ImageView arrowView;
    private Switch switchView;
    private TextView valueView;
    
    // 配置
    private ItemHeight itemHeight = ItemHeight.MEDIUM;
    private boolean showIcon = true;
    private boolean showArrow = false;
    private boolean showSwitch = false;
    
    // 焦点助手
    private TVFocusHelper focusHelper;
    
    // 开关状态监听
    @Nullable
    private OnSwitchChangeListener switchChangeListener;
    
    /**
     * 开关状态变化监听接口
     */
    public interface OnSwitchChangeListener {
        void onSwitchChanged(boolean isChecked);
    }
    
    public TVListItemView(@NonNull Context context) {
        this(context, null);
    }
    
    public TVListItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TVListItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, @Nullable AttributeSet attrs) {
        // 解析自定义属性
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TVListItemView);
            try {
                itemHeight = ItemHeight.fromValue(ta.getInt(R.styleable.TVListItemView_itemHeight, ItemHeight.MEDIUM.getValue()));
                showIcon = ta.getBoolean(R.styleable.TVListItemView_showIcon, true);
                showArrow = ta.getBoolean(R.styleable.TVListItemView_showArrow, false);
                showSwitch = ta.getBoolean(R.styleable.TVListItemView_showSwitch, false);
            } finally {
                ta.recycle();
            }
        }
        
        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.component_tv_list_item, this, true);
        
        // 绑定视图
        bindViews();
        
        // 应用配置
        applyConfiguration();
        
        // 设置焦点
        setupFocus();
    }
    
    private void bindViews() {
        rootContainer = findViewById(R.id.root_container);
        iconView = findViewById(R.id.icon);
        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        arrowView = findViewById(R.id.arrow);
        switchView = findViewById(R.id.switch_view);
        valueView = findViewById(R.id.value);
        
        // 设置开关监听
        if (switchView != null) {
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (switchChangeListener != null) {
                    switchChangeListener.onSwitchChanged(isChecked);
                }
            });
        }
    }
    
    private void applyConfiguration() {
        Context context = getContext();
        
        // 应用高度
        int height;
        switch (itemHeight) {
            case SMALL:
                height = (int) context.getResources().getDimension(R.dimen.tv_list_item_height_sm);
                break;
            case LARGE:
                height = (int) context.getResources().getDimension(R.dimen.tv_list_item_height_lg);
                break;
            default: // MEDIUM
                height = (int) context.getResources().getDimension(R.dimen.tv_list_item_height_md);
                break;
        }
        
        if (rootContainer != null) {
            LayoutParams params = (LayoutParams) rootContainer.getLayoutParams();
            params.height = height;
            rootContainer.setLayoutParams(params);
        }
        
        // 应用显示设置
        if (iconView != null) iconView.setVisibility(showIcon ? VISIBLE : GONE);
        if (arrowView != null) arrowView.setVisibility(showArrow ? VISIBLE : GONE);
        if (switchView != null) switchView.setVisibility(showSwitch ? VISIBLE : GONE);
    }
    
    private void setupFocus() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        
        // 设置背景
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tv_setting_item_background));
        
        // 创建焦点助手
        focusHelper = TVFocusHelper.createForListItem();
        focusHelper.attachToView(this);
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 设置图标
     */
    public TVListItemView setIcon(Drawable drawable) {
        if (iconView != null) {
            iconView.setImageDrawable(drawable);
            iconView.setVisibility(drawable != null ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置图标资源
     */
    public TVListItemView setIconResource(@DrawableRes int resId) {
        return setIcon(ContextCompat.getDrawable(getContext(), resId));
    }
    
    /**
     * 设置标题
     */
    public TVListItemView setTitle(CharSequence title) {
        if (titleView != null) {
            titleView.setText(title);
        }
        return this;
    }
    
    /**
     * 设置标题资源
     */
    public TVListItemView setTitle(@StringRes int resId) {
        return setTitle(getContext().getString(resId));
    }
    
    /**
     * 设置副标题
     */
    public TVListItemView setSubtitle(CharSequence subtitle) {
        if (subtitleView != null) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(subtitle != null && subtitle.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置副标题资源
     */
    public TVListItemView setSubtitle(@StringRes int resId) {
        return setSubtitle(getContext().getString(resId));
    }
    
    /**
     * 设置右侧值文本
     */
    public TVListItemView setValue(CharSequence value) {
        if (valueView != null) {
            valueView.setText(value);
            valueView.setVisibility(value != null && value.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置右侧值资源
     */
    public TVListItemView setValue(@StringRes int resId) {
        return setValue(getContext().getString(resId));
    }
    
    /**
     * 设置开关状态
     */
    public TVListItemView setSwitchChecked(boolean checked) {
        if (switchView != null) {
            switchView.setChecked(checked);
        }
        return this;
    }
    
    /**
     * 获取开关状态
     */
    public boolean isSwitchChecked() {
        return switchView != null && switchView.isChecked();
    }
    
    /**
     * 切换开关状态
     */
    public TVListItemView toggleSwitch() {
        if (switchView != null) {
            switchView.setChecked(!switchView.isChecked());
        }
        return this;
    }
    
    /**
     * 设置是否显示图标
     */
    public TVListItemView setShowIcon(boolean show) {
        this.showIcon = show;
        if (iconView != null) {
            iconView.setVisibility(show ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置是否显示箭头
     */
    public TVListItemView setShowArrow(boolean show) {
        this.showArrow = show;
        if (arrowView != null) {
            arrowView.setVisibility(show ? VISIBLE : GONE);
        }
        // 如果显示箭头，隐藏开关
        if (show && switchView != null) {
            switchView.setVisibility(GONE);
            showSwitch = false;
        }
        return this;
    }
    
    /**
     * 设置是否显示开关
     */
    public TVListItemView setShowSwitch(boolean show) {
        this.showSwitch = show;
        if (switchView != null) {
            switchView.setVisibility(show ? VISIBLE : GONE);
        }
        // 如果显示开关，隐藏箭头
        if (show && arrowView != null) {
            arrowView.setVisibility(GONE);
            showArrow = false;
        }
        return this;
    }
    
    /**
     * 设置开关状态变化监听器
     */
    public TVListItemView setOnSwitchChangeListener(@Nullable OnSwitchChangeListener listener) {
        this.switchChangeListener = listener;
        return this;
    }
    
    /**
     * 获取图标视图
     */
    public ImageView getIconView() {
        return iconView;
    }
    
    /**
     * 获取标题视图
     */
    public TextView getTitleView() {
        return titleView;
    }
    
    /**
     * 获取副标题视图
     */
    public TextView getSubtitleView() {
        return subtitleView;
    }
    
    /**
     * 获取值视图
     */
    public TextView getValueView() {
        return valueView;
    }
    
    /**
     * 获取开关视图
     */
    public Switch getSwitchView() {
        return switchView;
    }
}