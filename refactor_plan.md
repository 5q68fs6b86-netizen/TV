# TV 应用 UI 重构计划 (Material Design 3)

## 1. 项目概述

本项目旨在将现有的基于 Android Leanback 的 TV 应用的 UI 从当前的定制化实现重构为使用 Material Design 3 (MD3) 组件和设计规范。这将有助于提升应用的视觉吸引力、用户体验和代码可维护性。

## 2. 总体策略

我们将采用分阶段、逐个屏幕的策略进行重构，以确保每个阶段的稳定性和可测试性。我们将从最核心、最独立的屏幕开始，逐步扩展到整个应用。

## 3. 重构阶段

### 阶段一：基础设置和主题迁移

**目标**：为项目引入 Material Design 3 依赖，并创建一个新的 MD3 主题，为后续的组件迁移做准备。

*   **任务 1：添加 Material Design 3 依赖**
    *   在 `app/build.gradle` 文件中，添加最新的 `com.google.android.material:material` 依赖。
*   **任务 2：创建新的 MD3 主题**
    *   在 `app/src/main/res/values/styles.xml` 中，创建一个继承自 `Theme.Material3.DayNight.NoActionBar` 的新主题。
    *   定义应用所需的核心颜色（primary, secondary, tertiary）和字体样式。
*   **任务 3：应用新主题**
    *   在 `app/src/leanback/AndroidManifest.xml` 中，将 `android:theme` 属性指向新创建的 MD3 主题。

### 阶段二：主屏幕 (`HomeActivity`) 重构

**目标**：将应用的主屏幕重构为使用 MD3 组件，改善导航体验。

*   **任务 1：使用 `MaterialToolbar` 替换自定义工具栏**
    *   在 `activity_home.xml` 中，使用 `com.google.android.material.appbar.MaterialToolbar` 替换顶部的 `LinearLayout` 工具栏。
*   **任务 2：使用 `TabLayout` 和 `ViewPager2` 替换自定义导航**
    *   将 `CustomHorizontalGridView` 和 `CustomViewPager` 替换为 `com.google.android.material.tabs.TabLayout` 和 `androidx.viewpager2.widget.ViewPager2` 的组合。
    *   这将提供一个更符合 MD3 设计规范的顶部导航栏。
*   **任务 3：使用 `Card` 和 `RecyclerView` 重构内容列表**
    *   在 `fragment_home.xml` 中，将 `VerticalGridView` 替换为标准的 `androidx.recyclerview.widget.RecyclerView`。
    *   为列表项创建一个新的布局文件，使用 `com.google.android.material.card.MaterialCardView` 来包裹每个内容项，以提供更好的视觉效果和交互反馈。

### 阶段三：点播详情页 (`VodActivity`) 重构

**目标**：将视频点播详情页重构为使用 MD3 组件，优化信息展示和交互。

*   **任务 1：重构详情页布局**
    *   分析 `activity_vod.xml` 和相关的 Fragment 布局，使用 `ConstraintLayout` 或 `LinearLayout` 重新组织页面结构，使其更清晰、更易于维护。
*   **任务 2：使用 `MaterialButton` 和 `Chip`**
    *   将页面中的操作按钮（如播放、收藏）替换为 `com.google.android.material.button.MaterialButton`。
    *   使用 `com.google.android.material.chip.ChipGroup` 和 `Chip` 来显示视频的标签或分类。
*   **任务 3：优化剧集列表**
    *   与主屏幕类似，使用 `RecyclerView` 和 `MaterialCardView` 来重构剧集列表，提升其视觉效果和可用性。

### 阶段四：其他核心页面重构

**目标**：将剩余的核心页面（如直播、历史、收藏、设置等）逐个迁移到 MD3。

*   **任务 1：直播页 (`LiveActivity`)**
*   **任务 2：历史页 (`HistoryActivity`)**
*   **任务 3：收藏页 (`KeepActivity`)**
*   **任务 4：设置页 (`SettingActivity`)**

对于每个页面，都将遵循与前两个阶段类似的步骤：分析现有布局 -> 使用 MD3 组件替换自定义或过时的组件 -> 优化布局和交互。

## 4. 风险与应对

*   **风险**：高度定制化的现有 UI 可能导致某些功能在迁移过程中难以完全兼容。
*   **应对**：在每个阶段进行充分的测试，确保所有核心功能都能正常工作。对于难以直接迁移的组件，可以考虑寻找 MD3 的替代方案，或者在保持 MD3 风格的前提下进行适度的自定义。

## 5. 交付物

*   **最终交付**：一个完全使用 Material Design 3 组件和设计规范重构的 TV 应用。
*   **阶段性交付**：每个阶段完成后，都将提交一个可运行、可测试的应用版本。