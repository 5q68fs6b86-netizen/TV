package com.fongmi.android.tv.ui.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.R;

/**
 * TV 按钮视图组件
 * 
 * <p>提供统一的按钮样式，包含：
 * <ul>
 *   <li>多种按钮类型（主要、次要、轮廓、文字、图标）</li>
 *   <li>多种尺寸</li>
 *   <li>图标支持</li>
 *   <li>焦点动画效果</li>
 * </ul>
 */
public class TVButtonView extends LinearLayout {

    /**
     * 按钮类型枚举
     */
    public enum ButtonType {
        PRIMARY(0),     // 主要按钮 - 高对比度
        SECONDARY(1),   // 次要按钮 - 中等对比度
        OUTLINE(2),     // 轮廓按钮 - 边框样式
        TEXT(3),        // 文字按钮 - 仅文字
        ICON(4);        // 图标按钮 - 仅图标
        
        private final int value;
        
        ButtonType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static ButtonType fromValue(int value) {
            for (ButtonType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return PRIMARY;
        }
    }
    
    /**
     * 按钮尺寸枚举
     */
    public enum ButtonSize {
        SMALL(0),
        MEDIUM(1),
        LARGE(2);
        
        private final int value;
        
        ButtonSize(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static ButtonSize fromValue(int value) {
            for (ButtonSize size : values()) {
                if (size.value == value) {
                    return size;
                }
            }
            return MEDIUM;
        }
    }
    
    /**
     * 图标位置枚举
     */
    public enum IconPosition {
        START(0),
        END(1),
        TOP(2),
        BOTTOM(3);
        
        private final int value;
        
        IconPosition(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static IconPosition fromValue(int value) {
            for (IconPosition pos : values()) {
                if (pos.value == value) {
                    return pos;
                }
            }
            return START;
        }
    }
    
    // 子视图
    private ImageView iconView;
    private TextView textView;
    
    // 配置
    private ButtonType buttonType = ButtonType.PRIMARY;
    private ButtonSize buttonSize = ButtonSize.MEDIUM;
    private IconPosition iconPosition = IconPosition.START;
    private Drawable iconDrawable;
    private CharSequence text;
    
    // 焦点助手
    private TVFocusHelper focusHelper;
    
    public TVButtonView(@NonNull Context context) {
        this(context, null);
    }
    
    public TVButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TVButtonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, @Nullable AttributeSet attrs) {
        // 解析自定义属性
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TVButtonView);
            try {
                buttonType = ButtonType.fromValue(ta.getInt(R.styleable.TVButtonView_buttonType, ButtonType.PRIMARY.getValue()));
                buttonSize = ButtonSize.fromValue(ta.getInt(R.styleable.TVButtonView_buttonSize, ButtonSize.MEDIUM.getValue()));
                iconPosition = IconPosition.fromValue(ta.getInt(R.styleable.TVButtonView_iconPosition, IconPosition.START.getValue()));
                iconDrawable = ta.getDrawable(R.styleable.TVButtonView_buttonIcon);
                text = ta.getString(R.styleable.TVButtonView_android_text);
            } finally {
                ta.recycle();
            }
        }
        
        // 设置布局方向
        updateOrientation();
        setGravity(Gravity.CENTER);
        
        // 创建子视图
        createViews(context);
        
        // 应用样式
        applyStyle();
        
        // 设置焦点
        setupFocus();
    }
    
    private void updateOrientation() {
        switch (iconPosition) {
            case TOP:
            case BOTTOM:
                setOrientation(VERTICAL);
                break;
            default:
                setOrientation(HORIZONTAL);
                break;
        }
    }
    
    private void createViews(Context context) {
        // 创建图标视图
        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        
        // 创建文字视图
        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(true);
        
        // 根据图标位置添加视图
        switch (iconPosition) {
            case END:
            case BOTTOM:
                addView(textView);
                addView(iconView);
                break;
            default: // START, TOP
                addView(iconView);
                addView(textView);
                break;
        }
        
        // 设置内容
        if (iconDrawable != null) {
            iconView.setImageDrawable(iconDrawable);
            iconView.setVisibility(VISIBLE);
        } else {
            iconView.setVisibility(GONE);
        }
        
        if (text != null && text.length() > 0) {
            textView.setText(text);
            textView.setVisibility(VISIBLE);
        } else if (buttonType == ButtonType.ICON) {
            textView.setVisibility(GONE);
        }
    }
    
    private void applyStyle() {
        Context context = getContext();
        
        // 应用背景
        switch (buttonType) {
            case PRIMARY:
                setBackground(ContextCompat.getDrawable(context, R.drawable.tv_button_primary_background));
                break;
            case SECONDARY:
                setBackground(ContextCompat.getDrawable(context, R.drawable.tv_button_secondary_background));
                break;
            case OUTLINE:
                setBackground(ContextCompat.getDrawable(context, R.drawable.tv_button_outline_background));
                break;
            case ICON:
                setBackground(ContextCompat.getDrawable(context, R.drawable.tv_button_icon_background));
                break;
            case TEXT:
            default:
                setBackground(null);
                break;
        }
        
        // 应用尺寸
        int height, paddingH, paddingV, iconSize, textSize, iconMargin;
        switch (buttonSize) {
            case SMALL:
                height = (int) context.getResources().getDimension(R.dimen.tv_button_height_sm);
                paddingH = (int) context.getResources().getDimension(R.dimen.tv_spacing_md);
                paddingV = (int) context.getResources().getDimension(R.dimen.tv_spacing_xs);
                iconSize = (int) context.getResources().getDimension(R.dimen.tv_icon_sm);
                textSize = (int) context.getResources().getDimension(R.dimen.tv_text_body_small);
                iconMargin = (int) context.getResources().getDimension(R.dimen.tv_spacing_xxs);
                break;
            case LARGE:
                height = (int) context.getResources().getDimension(R.dimen.tv_button_height_lg);
                paddingH = (int) context.getResources().getDimension(R.dimen.tv_spacing_xl);
                paddingV = (int) context.getResources().getDimension(R.dimen.tv_spacing_md);
                iconSize = (int) context.getResources().getDimension(R.dimen.tv_icon_lg);
                textSize = (int) context.getResources().getDimension(R.dimen.tv_text_body_large);
                iconMargin = (int) context.getResources().getDimension(R.dimen.tv_spacing_sm);
                break;
            default: // MEDIUM
                height = (int) context.getResources().getDimension(R.dimen.tv_button_height_md);
                paddingH = (int) context.getResources().getDimension(R.dimen.tv_spacing_lg);
                paddingV = (int) context.getResources().getDimension(R.dimen.tv_spacing_sm);
                iconSize = (int) context.getResources().getDimension(R.dimen.tv_icon_md);
                textSize = (int) context.getResources().getDimension(R.dimen.tv_text_body_medium);
                iconMargin = (int) context.getResources().getDimension(R.dimen.tv_spacing_xs);
                break;
        }
        
        // 图标按钮特殊处理
        if (buttonType == ButtonType.ICON) {
            int size;
            switch (buttonSize) {
                case SMALL:
                    size = (int) context.getResources().getDimension(R.dimen.tv_icon_button_sm);
                    break;
                case LARGE:
                    size = (int) context.getResources().getDimension(R.dimen.tv_icon_button_lg);
                    break;
                default:
                    size = (int) context.getResources().getDimension(R.dimen.tv_icon_button_md);
                    break;
            }
            setMinimumHeight(size);
            setMinimumWidth(size);
            int padding = (size - iconSize) / 2;
            setPadding(padding, padding, padding, padding);
        } else {
            setMinimumHeight(height);
            setPadding(paddingH, paddingV, paddingH, paddingV);
        }
        
        // 设置图标尺寸
        LayoutParams iconParams = new LayoutParams(iconSize, iconSize);
        if (iconView.getVisibility() == VISIBLE && textView.getVisibility() == VISIBLE) {
            switch (iconPosition) {
                case END:
                    iconParams.setMarginStart(iconMargin);
                    break;
                case BOTTOM:
                    iconParams.topMargin = iconMargin;
                    break;
                case TOP:
                    iconParams.bottomMargin = iconMargin;
                    break;
                default: // START
                    iconParams.setMarginEnd(iconMargin);
                    break;
            }
        }
        iconView.setLayoutParams(iconParams);
        
        // 设置文字样式
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        
        // 设置文字和图标颜色
        int textColor, iconTint;
        switch (buttonType) {
            case PRIMARY:
                textColor = ContextCompat.getColor(context, R.color.tv_button_primary_text);
                iconTint = textColor;
                break;
            case OUTLINE:
            case TEXT:
                textColor = ContextCompat.getColor(context, R.color.tv_button_text_default);
                iconTint = textColor;
                break;
            default: // SECONDARY, ICON
                textColor = ContextCompat.getColor(context, R.color.tv_button_secondary_text);
                iconTint = textColor;
                break;
        }
        
        textView.setTextColor(textColor);
        iconView.setImageTintList(ColorStateList.valueOf(iconTint));
    }
    
    private void setupFocus() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        
        // 创建焦点助手
        focusHelper = TVFocusHelper.createForButton();
        focusHelper.attachToView(this);
    }
    
    // ==================== 公共 API ====================
    
    /**
     * 设置按钮文字
     */
    public TVButtonView setText(CharSequence text) {
        this.text = text;
        if (textView != null) {
            textView.setText(text);
            textView.setVisibility(text != null && text.length() > 0 ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置按钮文字（资源ID）
     */
    public TVButtonView setText(@StringRes int resId) {
        return setText(getContext().getString(resId));
    }
    
    /**
     * 获取按钮文字
     */
    public CharSequence getText() {
        return text;
    }
    
    /**
     * 设置图标
     */
    public TVButtonView setIcon(Drawable drawable) {
        this.iconDrawable = drawable;
        if (iconView != null) {
            iconView.setImageDrawable(drawable);
            iconView.setVisibility(drawable != null ? VISIBLE : GONE);
        }
        return this;
    }
    
    /**
     * 设置图标（资源ID）
     */
    public TVButtonView setIconResource(@DrawableRes int resId) {
        return setIcon(ContextCompat.getDrawable(getContext(), resId));
    }
    
    /**
     * 设置按钮类型
     */
    public TVButtonView setButtonType(ButtonType type) {
        if (this.buttonType != type) {
            this.buttonType = type;
            applyStyle();
        }
        return this;
    }
    
    /**
     * 设置按钮尺寸
     */
    public TVButtonView setButtonSize(ButtonSize size) {
        if (this.buttonSize != size) {
            this.buttonSize = size;
            applyStyle();
        }
        return this;
    }
    
    /**
     * 设置图标位置
     */
    public TVButtonView setIconPosition(IconPosition position) {
        if (this.iconPosition != position) {
            this.iconPosition = position;
            removeAllViews();
            updateOrientation();
            createViews(getContext());
            applyStyle();
        }
        return this;
    }
    
    /**
     * 获取图标视图
     */
    public ImageView getIconView() {
        return iconView;
    }
    
    /**
     * 获取文字视图
     */
    public TextView getTextView() {
        return textView;
    }
    
    /**
     * 设置按钮是否可用
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.5f);
        if (textView != null) textView.setEnabled(enabled);
        if (iconView != null) iconView.setEnabled(enabled);
    }
}