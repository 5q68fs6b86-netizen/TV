# TV Leanback UI 设计系统

## 概述

本设计系统遵循 **Google TV 设计规范**，为 Android TV Leanback 应用提供统一的视觉语言和组件库。

---

## 设计原则

### 1. 10-Foot Experience (十英尺体验)
- 针对远距离观看优化
- 最小字体 14sp
- 清晰的焦点状态
- 高对比度颜色

### 2. D-Pad 导航优先
- 所有可交互元素可聚焦
- 清晰的焦点指示器
- 合理的焦点移动顺序

### 3. 内容优先
- 简洁的界面设计
- 突出内容展示
- 减少视觉干扰

---

## 颜色系统

### 基础颜色 (colors_tv.xml)

| Token | 值 | 用途 |
|-------|-----|------|
| `tv_background_primary` | #0F0F0F | 主背景色 |
| `tv_background_secondary` | #1A1A1A | 次级背景 |
| `tv_surface_default` | #1F1F1F | 卡片/容器表面 |
| `tv_focus_primary` | #8AB4F8 | 焦点色 (Google TV 蓝) |
| `tv_text_primary` | #FFFFFF | 主要文字 |
| `tv_text_secondary` | #B3FFFFFF | 次要文字 |

### 焦点系统颜色

| Token | 用途 |
|-------|------|
| `tv_focus_ring` | 焦点边框 |
| `tv_focus_glow` | 焦点光晕 |
| `tv_focus_gradient_*` | 焦点渐变 |

### 播放器颜色

| Token | 用途 |
|-------|------|
| `tv_player_control_bg` | 控制栏背景 |
| `tv_player_progress_played` | 已播放进度 |
| `tv_player_button_bg` | 播放器按钮背景 |

---

## 尺寸系统 (dimens_tv.xml)

### 间距系统 (8dp 网格)

| Token | 值 | 用途 |
|-------|-----|------|
| `tv_spacing_xs` | 8dp | 小间距 |
| `tv_spacing_sm` | 12dp | 紧凑间距 |
| `tv_spacing_md` | 16dp | 标准间距 |
| `tv_spacing_lg` | 24dp | 大间距 |
| `tv_spacing_xl` | 32dp | 特大间距 |

### 字体大小

| Token | 值 | 用途 |
|-------|-----|------|
| `tv_text_display_large` | 56sp | Hero 标题 |
| `tv_text_headline_small` | 24sp | 页面标题 |
| `tv_text_title_medium` | 20sp | 卡片标题 |
| `tv_text_body_medium` | 16sp | 正文 |
| `tv_text_body_small` | 14sp | 最小可读文字 |

### 卡片尺寸

| 类型 | 宽度 | 高度 | 比例 |
|------|------|------|------|
| 海报卡片 (MD) | 160dp | 240dp | 2:3 |
| 横版卡片 (MD) | 284dp | 160dp | 16:9 |
| 方形卡片 (MD) | 160dp | 160dp | 1:1 |

### 圆角系统

| Token | 值 |
|-------|-----|
| `tv_radius_sm` | 6dp |
| `tv_radius_md` | 8dp |
| `tv_radius_lg` | 12dp |
| `tv_radius_full` | 9999dp |

---

## 样式系统 (styles_tv.xml)

### 主题

```xml
<style name="Theme.TV" parent="Theme.Leanback">
    <!-- 基础 TV 主题 -->
</style>

<style name="Theme.TV.Fullscreen" parent="Theme.TV.NoActionBar">
    <!-- 全屏主题 -->
</style>
```

### 文字样式

| 样式 | 用途 |
|------|------|
| `TV.Text.Display.*` | 大标题 |
| `TV.Text.Headline.*` | 区域标题 |
| `TV.Text.Title.*` | 项目标题 |
| `TV.Text.Body.*` | 正文内容 |
| `TV.Text.Label.*` | 标签/辅助文字 |

### 卡片样式

| 样式 | 用途 |
|------|------|
| `TV.Card` | 基础卡片 |
| `TV.Card.Poster` | 海报卡片 |
| `TV.Card.Landscape` | 横版卡片 |
| `TV.Card.Square` | 方形卡片 |

### 按钮样式

| 样式 | 用途 |
|------|------|
| `TV.Button.Primary` | 主要按钮 |
| `TV.Button.Secondary` | 次要按钮 |
| `TV.Button.Outline` | 轮廓按钮 |
| `TV.Button.Icon` | 图标按钮 |

---

## 组件库

### Java 组件

#### TVFocusHelper
焦点动画工具类，提供统一的焦点缩放和动画效果。

```java
TVFocusHelper.apply(view)
    .scaleOnFocus(1.05f)
    .elevationOnFocus(12f)
    .build();
```

#### TVCardView
自定义卡片组件，集成焦点处理和内容显示。

```java
TVCardView card = new TVCardView(context);
card.setTitle("标题");
card.setSubtitle("副标题");
card.setImageUrl("https://...");
```

#### TVButtonView
自定义按钮组件，支持多种类型和尺寸。

```java
TVButtonView button = new TVButtonView(context);
button.setButtonType(TVButtonView.TYPE_PRIMARY);
button.setButtonSize(TVButtonView.SIZE_MEDIUM);
```

#### TVListItemView
设置/列表项组件。

### Leanback Presenters

#### TVCardPresenter
用于 Leanback ObjectAdapter 的卡片 Presenter。

#### VodCardPresenter
专用于 Vod 数据类型的 Presenter。

---

## Drawable 资源

### 卡片背景
- `tv_card_background.xml` - 卡片背景选择器

### 按钮背景
- `tv_button_primary_background.xml`
- `tv_button_secondary_background.xml`
- `tv_button_outline_background.xml`
- `tv_button_icon_background.xml`

### 视频控制
- `tv_video_button_primary.xml`
- `tv_video_button_secondary.xml`
- `tv_video_button_accent.xml`
- `tv_video_button_icon.xml`
- `tv_video_control_group.xml`
- `tv_player_control_gradient.xml`

### 设置页面
- `tv_setting_item_background.xml`

### 其他
- `tv_badge_*.xml` - 徽章背景
- `tv_widget_background.xml` - Widget 背景
- `tv_display_progress.xml` - 进度条

---

## 动画资源

### StateListAnimator
- `tv_card_state.xml` - 卡片焦点/按下动画
- `tv_button_state.xml` - 按钮焦点/按下动画
- `tv_focus_scale.xml` - 通用焦点缩放

### 动画参数

| 参数 | 值 |
|------|-----|
| 焦点动画时长 | 150ms |
| 缩放比例 | 1.05 |
| 焦点 Elevation | 12dp |

---

## 布局更新

### 已更新的布局文件

| 文件 | 描述 |
|------|------|
| `activity_home.xml` | 主页 |
| `fragment_home.xml` | 主页 Fragment |
| `adapter_func.xml` | 功能卡片 |
| `adapter_vod_rect.xml` | VOD 海报卡片 |
| `adapter_vod_list.xml` | VOD 列表项 |
| `adapter_vod_oval.xml` | VOD 圆形卡片 |
| `activity_video.xml` | 视频播放页 |
| `view_control_vod.xml` | VOD 控制栏 |
| `view_widget_vod.xml` | VOD Widget |
| `view_widget_display.xml` | 显示信息层 |
| `activity_setting.xml` | 设置页 |
| `activity_search.xml` | 搜索页 |
| `activity_live.xml` | 直播页 |

---

## 使用指南

### 1. 添加卡片

```xml
<com.fongmi.android.tv.ui.component.TVCardView
    android:layout_width="@dimen/tv_card_poster_width_md"
    android:layout_height="@dimen/tv_card_poster_height_md"
    app:cardTitle="标题"
    app:cardSubtitle="副标题" />
```

### 2. 使用设计 Token

```xml
<!-- 使用颜色 Token -->
android:background="@color/tv_surface_default"
android:textColor="@color/tv_text_primary"

<!-- 使用尺寸 Token -->
android:padding="@dimen/tv_spacing_md"
android:textSize="@dimen/tv_text_body_medium"

<!-- 使用样式 -->
style="@style/TV.Card.Poster"
style="@style/TV.Button.Primary"
```

### 3. 应用焦点动画

```java
// 使用 TVFocusHelper
TVFocusHelper.apply(myView)
    .scaleOnFocus(1.05f)
    .build();

// 或使用 StateListAnimator
android:stateListAnimator="@animator/tv_card_state"
```

---

## 迁移指南

### 从旧样式迁移

| 旧样式 | 新样式 |
|--------|--------|
| `@drawable/selector_item` | `@drawable/tv_card_background` |
| `@color/white` | `@color/tv_text_primary` |
| `24dp` | `@dimen/tv_spacing_lg` |
| `16sp` | `@dimen/tv_text_body_medium` |

---

## 最佳实践

1. **始终使用设计 Token** - 不要硬编码颜色和尺寸值
2. **确保焦点可见性** - 所有可交互元素必须有清晰的焦点状态
3. **保持一致性** - 使用预定义的样式和组件
4. **遵循 8dp 网格** - 所有间距使用 8dp 的倍数
5. **测试焦点导航** - 确保 D-Pad 导航流畅

---

## 版本历史

- **v1.0** (2026-01-01) - 初始版本
  - 建立设计系统基础
  - 创建核心组件库
  - 更新主要页面布局
