# Leanback UI 重构计划

## 项目概述

本文档描述了对 Android TV Leanback UI 的全面重构计划，目标是建立一个统一的设计系统，遵循 Google TV/Android TV 官方设计规范。

---

## 设计原则

参考 Google TV 设计规范，我们将遵循以下原则：

1. **10英尺体验** - 为远距离观看优化，确保足够的文字大小和对比度
2. **清晰的焦点状态** - 明确的焦点指示器，用户始终知道当前选中项
3. **简洁的导航** - D-pad 友好的导航模式，减少按键次数
4. **内容优先** - 让内容成为主角，UI 元素不应喧宾夺主
5. **一致性** - 所有页面使用统一的视觉语言和交互模式

---

## 阶段一：设计系统基础

### 1.1 颜色系统重构

**文件**: `app/src/leanback/res/values/colors.xml`

**当前状态**: 已有 Material Design 3 颜色定义，但缺少统一的 TV 焦点状态颜色

**目标**: 
- 优化 TV 专用颜色，增强对比度和可读性
- 建立统一的焦点状态颜色系统
- 添加语义化颜色命名

**需要添加/更新的颜色**:

```xml
<!-- TV 焦点系统颜色 -->
<color name="tv_focus_primary">#FF00A3FF</color>
<color name="tv_focus_secondary">#80FFFFFF</color>
<color name="tv_focus_ring">#4000A3FF</color>

<!-- 卡片背景层级 -->
<color name="tv_surface_card">#1A1A1A</color>
<color name="tv_surface_card_elevated">#242424</color>
<color name="tv_surface_card_focused">#2A2A2A</color>

<!-- 文字层级 -->
<color name="tv_text_primary">#FFFFFF</color>
<color name="tv_text_secondary">#B3FFFFFF</color>
<color name="tv_text_tertiary">#66FFFFFF</color>
<color name="tv_text_disabled">#4DFFFFFF</color>

<!-- 品牌渐变 -->
<color name="tv_gradient_start">#00000000</color>
<color name="tv_gradient_end">#E6000000</color>
```

### 1.2 尺寸系统重构

**文件**: `app/src/leanback/res/values/dimens.xml`

**目标**: 建立基于 8dp 网格的统一尺寸系统

**需要添加/更新的尺寸**:

```xml
<!-- TV 安全区域 -->
<dimen name="tv_overscan_horizontal">48dp</dimen>
<dimen name="tv_overscan_vertical">27dp</dimen>

<!-- 焦点系统 -->
<dimen name="tv_focus_stroke_width">3dp</dimen>
<dimen name="tv_focus_scale_factor">1.05</dimen>
<dimen name="tv_focus_elevation">12dp</dimen>

<!-- 卡片系统 -->
<dimen name="tv_card_poster_width">160dp</dimen>
<dimen name="tv_card_poster_height">240dp</dimen>
<dimen name="tv_card_landscape_width">280dp</dimen>
<dimen name="tv_card_landscape_height">158dp</dimen>
<dimen name="tv_card_corner_radius">8dp</dimen>
<dimen name="tv_card_spacing">16dp</dimen>

<!-- 文字大小 (TV 最小 14sp) -->
<dimen name="tv_text_display">48sp</dimen>
<dimen name="tv_text_headline">32sp</dimen>
<dimen name="tv_text_title">24sp</dimen>
<dimen name="tv_text_subtitle">18sp</dimen>
<dimen name="tv_text_body">16sp</dimen>
<dimen name="tv_text_caption">14sp</dimen>

<!-- 行高度 -->
<dimen name="tv_row_height_large">300dp</dimen>
<dimen name="tv_row_height_medium">220dp</dimen>
<dimen name="tv_row_height_small">160dp</dimen>
```

### 1.3 主题样式系统

**新文件**: `app/src/leanback/res/values/styles_tv.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 基础 TV 主题 -->
    <style name="Theme.TV" parent="Theme.Leanback">
        <item name="android:colorPrimary">@color/tv_focus_primary</item>
        <item name="android:windowBackground">@color/black</item>
        <item name="android:colorAccent">@color/tv_focus_primary</item>
    </style>

    <!-- TV 卡片基础样式 -->
    <style name="Widget.TV.Card">
        <item name="android:background">@drawable/tv_card_background</item>
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:stateListAnimator">@animator/tv_card_state</item>
    </style>

    <!-- TV 按钮样式 -->
    <style name="Widget.TV.Button">
        <item name="android:minHeight">48dp</item>
        <item name="android:paddingHorizontal">24dp</item>
        <item name="android:background">@drawable/tv_button_background</item>
        <item name="android:textColor">@color/tv_text_primary</item>
        <item name="android:textSize">@dimen/tv_text_body</item>
    </style>

    <!-- 文字样式层级 -->
    <style name="Widget.TV.Text.Display">
        <item name="android:textSize">@dimen/tv_text_display</item>
        <item name="android:textColor">@color/tv_text_primary</item>
        <item name="android:fontFamily">sans-serif-medium</item>
    </style>

    <style name="Widget.TV.Text.Headline">
        <item name="android:textSize">@dimen/tv_text_headline</item>
        <item name="android:textColor">@color/tv_text_primary</item>
        <item name="android:fontFamily">sans-serif-medium</item>
    </style>

    <style name="Widget.TV.Text.Title">
        <item name="android:textSize">@dimen/tv_text_title</item>
        <item name="android:textColor">@color/tv_text_primary</item>
    </style>

    <style name="Widget.TV.Text.Body">
        <item name="android:textSize">@dimen/tv_text_body</item>
        <item name="android:textColor">@color/tv_text_secondary</item>
    </style>

    <style name="Widget.TV.Text.Caption">
        <item name="android:textSize">@dimen/tv_text_caption</item>
        <item name="android:textColor">@color/tv_text_tertiary</item>
    </style>
</resources>
```

### 1.4 动画资源

**新文件**: `app/src/leanback/res/animator/tv_card_state.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_focused="true">
        <set>
            <objectAnimator
                android:propertyName="scaleX"
                android:valueTo="1.05"
                android:duration="200"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator
                android:propertyName="scaleY"
                android:valueTo="1.05"
                android:duration="200"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator
                android:propertyName="translationZ"
                android:valueTo="12dp"
                android:duration="200"/>
        </set>
    </item>
    <item android:state_focused="false">
        <set>
            <objectAnimator
                android:propertyName="scaleX"
                android:valueTo="1.0"
                android:duration="200"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator
                android:propertyName="scaleY"
                android:valueTo="1.0"
                android:duration="200"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator
                android:propertyName="translationZ"
                android:valueTo="0dp"
                android:duration="200"/>
        </set>
    </item>
</selector>
```

### 1.5 Drawable 资源

**需要创建的 Drawable 文件**:

| 文件名 | 用途 |
|--------|------|
| `tv_card_background.xml` | 卡片背景（支持焦点状态） |
| `tv_button_background.xml` | 按钮背景（支持按下/焦点状态） |
| `tv_focus_ring.xml` | 焦点光环效果 |
| `tv_gradient_scrim.xml` | 内容遮罩渐变 |
| `tv_row_background.xml` | 行背景 |

---

## 阶段二：核心组件库

### 2.1 基础卡片组件

**目标**: 创建统一的卡片组件，支持多种布局模式

**组件结构**:

```
com.fongmi.android.tv.ui.widget/
├── TVCard.java                 - 基础卡片视图
├── TVPosterCard.java           - 海报卡片（竖版）
├── TVLandscapeCard.java        - 横版卡片
├── TVListCard.java             - 列表项卡片
└── TVFocusHelper.java          - 焦点处理辅助类
```

**TVCard 关键特性**:
- 统一的焦点状态动画
- 可配置的圆角和阴影
- 支持加载状态和错误状态
- 支持徽章（如 HD、4K 标识）

### 2.2 行组件

**目标**: 替代现有的 CustomRowPresenter，提供更好的焦点管理

**组件**:
- `TVHorizontalRow.java` - 水平滚动行
- `TVGridRow.java` - 网格布局行
- `TVRowHeader.java` - 行标题组件

### 2.3 对话框组件

**目标**: 统一所有对话框样式

**需要重构的对话框**:
- MenuDialog → TVMenuDialog
- SiteDialog → TVSiteDialog
- EpisodeDialog → TVEpisodeDialog
- SettingDialog系列 → TV风格统一

### 2.4 导航组件

**目标**: 创建统一的顶部导航和侧边栏组件

**组件**:
- `TVTopBar.java` - 顶部导航栏
- `TVSideNav.java` - 侧边导航（可选）
- `TVTabRow.java` - 标签行

---

## 阶段三：主页重构 (HomeActivity)

### 3.1 布局结构重新设计

**当前结构分析**:

```
activity_home.xml
├── toolbar (LinearLayout)
│   ├── logo
│   ├── title (CustomTitleView)
│   └── clock
├── blank (间隔)
├── recycler (CustomHorizontalGridView) - 分类标签
└── pager (CustomViewPager) - 内容页面
```

**新结构设计**:

```
activity_home_new.xml
├── topBar (TVTopBar)
│   ├── logo
│   ├── title
│   ├── clock
│   └── userActions (搜索、设置快捷入口)
├── categoryRow (TVTabRow) - 分类标签，更好的焦点管理
└── contentContainer
    ├── featuredBanner (可选：精选内容横幅)
    └── pager (ViewPager2) - 升级到 ViewPager2
```

### 3.2 HomeFragment 重构

**当前结构**:
- 使用 ArrayObjectAdapter + ListRow
- FuncPresenter 显示功能按钮
- HistoryPresenter 显示历史记录
- VodPresenter 显示推荐内容

**优化方向**:
1. 功能按钮行优化 - 使用新的卡片样式
2. 历史记录行优化 - 添加进度条显示
3. 推荐内容行优化 - 支持多种卡片尺寸

### 3.3 焦点管理优化

**问题**: 当前焦点切换可能不够流畅

**解决方案**:
- 实现 `FocusSearchListener` 优化焦点查找
- 使用 `nextFocusUp/Down/Left/Right` 明确焦点路径
- 记住最后焦点位置

---

## 阶段四：视频播放页重构 (VideoActivity)

### 4.1 详情页布局优化

**当前布局分析** (`activity_video.xml`):
- 左侧：视频播放器
- 右侧：详情信息（标题、描述、选集等）

**优化方向**:
1. 全屏模式下的控制栏优化
2. 详情信息的层级优化
3. 选集列表的焦点管理

### 4.2 播放控制栏重构

**当前状态** (`view_control_vod.xml`):
- 已有分组按钮设计
- 使用 HorizontalScrollView

**优化方向**:
1. 更好的按钮分组视觉效果
2. 焦点状态增强
3. 进度条交互优化

### 4.3 选集对话框优化

**目标**: 提供更好的选集选择体验

**设计要点**:
- 清晰的当前集数指示
- 支持按组/按集排列
- 快速跳转功能

---

## 阶段五：其他页面重构

### 5.1 设置页面 (SettingActivity)

**当前状态**: 传统列表布局

**优化方向**:
- 分组卡片式布局
- 图标+文字的设置项
- 开关/选择器的统一样式

### 5.2 搜索页面 (SearchActivity)

**优化方向**:
- 虚拟键盘优化
- 搜索建议展示
- 结果列表焦点管理

### 5.3 直播页面 (LiveActivity)

**优化方向**:
- 频道列表样式
- EPG 展示优化
- 频道分组导航

### 5.4 历史/收藏页面

**优化方向**:
- 卡片样式统一
- 批量操作支持
- 空状态设计

---

## 阶段六：测试与优化

### 6.1 焦点导航测试

**测试内容**:
- [ ] 所有页面的 D-pad 导航
- [ ] 焦点状态视觉反馈
- [ ] 跨组件焦点切换
- [ ] 返回键行为

### 6.2 性能优化

**优化点**:
- RecyclerView 预加载优化
- 图片加载优化 (Glide 配置)
- 动画性能 (使用硬件加速)
- 内存使用监控

### 6.3 适配测试

**设备适配**:
- [ ] 1080p 电视
- [ ] 4K 电视
- [ ] 不同 DPI 设备
- [ ] 不同遥控器

---

## 文件变更清单

### 新建文件

| 路径 | 说明 |
|------|------|
| `res/values/styles_tv.xml` | TV 专用样式 |
| `res/animator/tv_card_state.xml` | 卡片状态动画 |
| `res/animator/tv_focus_scale.xml` | 焦点缩放动画 |
| `res/drawable/tv_card_background.xml` | 卡片背景 |
| `res/drawable/tv_button_background.xml` | 按钮背景 |
| `res/drawable/tv_focus_ring.xml` | 焦点光环 |
| `java/.../ui/widget/TVCard.java` | 基础卡片组件 |
| `java/.../ui/widget/TVFocusHelper.java` | 焦点辅助类 |

### 修改文件

| 路径 | 修改内容 |
|------|----------|
| `res/values/colors.xml` | 添加 TV 专用颜色 |
| `res/values/dimens.xml` | 添加 TV 尺寸规范 |
| `res/layout/activity_home.xml` | 主页布局重构 |
| `res/layout/view_control_vod.xml` | 播放控制栏优化 |
| `res/layout/adapter_vod_*.xml` | 卡片布局统一 |
| `java/.../ui/presenter/*.java` | Presenter 优化 |

---

## 实施时间线

```
阶段一 (设计系统基础)     ████████░░░░░░░░░░░░ 
阶段二 (核心组件库)       ░░░░░░░░████████░░░░
阶段三 (主页重构)         ░░░░░░░░░░░░████░░░░
阶段四 (视频播放页重构)   ░░░░░░░░░░░░░░██░░░░
阶段五 (其他页面重构)     ░░░░░░░░░░░░░░░░████
阶段六 (测试优化)         ░░░░░░░░░░░░░░░░░░██
```

---

## 设计参考

### Google TV 设计规范关键点

1. **焦点状态**
   - 使用缩放 (1.05-1.1x) + 阴影表示焦点
   - 避免使用边框作为唯一焦点指示

2. **卡片设计**
   - 圆角: 8-12dp
   - 内容与边缘保持 16dp 间距
   - 文字信息在卡片下方或覆盖层

3. **文字规范**
   - 最小字号: 14sp
   - 标题: 24-32sp
   - 正文: 16-18sp

4. **颜色对比**
   - 文字与背景对比度 ≥ 4.5:1
   - 焦点状态使用高饱和度颜色

5. **动画**
   - 持续时间: 200-300ms
   - 使用 fast_out_slow_in 插值器
   - 避免过度复杂的动画

---

## 下一步行动

1. **开始阶段一**: 更新 colors.xml 和 dimens.xml
2. **创建基础样式文件**: styles_tv.xml
3. **创建动画资源**: tv_card_state.xml
4. **创建基础 Drawable**: 卡片和按钮背景

准备好后，切换到 Code 模式开始实施！