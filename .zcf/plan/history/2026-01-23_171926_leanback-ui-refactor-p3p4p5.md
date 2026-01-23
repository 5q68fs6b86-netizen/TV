# Leanback UI 重構計劃（P3 + P4 + P5）

## 任務概述
- **目標**：完成 P3 設置頁 + P4 播放器控制 + P5 首頁的 Material 3 重構
- **範圍**：15 個文件
- **原則**：樣式優先，複用已建立的設計系統

---

## 階段 1：擴展樣式系統

### 文件：styles.xml

**新增樣式：**

```xml
<!-- 設置頁行容器 -->
<style name="TVSettingRow">
    layout_width: match_parent
    layout_height: wrap_content
    layout_marginTop: @dimen/spacing_normal
    orientation: horizontal
    gravity: center_vertical
</style>

<!-- 設置項（可點擊） -->
<style name="TVSettingItem">
    layout_width: 0dp
    layout_height: wrap_content
    layout_weight: 1
    layout_marginEnd: @dimen/spacing_normal
    background: @drawable/selector_item_enhanced
    focusable: true
    orientation: horizontal
    paddingHorizontal: @dimen/spacing_normal
    paddingVertical: @dimen/spacing_medium
</style>

<!-- 設置項標籤文字 -->
<style name="TVSettingLabel" parent="TVTextAppearance.Body">
    textColor: @color/text_primary_high_emphasis
</style>

<!-- 設置項值文字 -->
<style name="TVSettingValue" parent="TVTextAppearance.Body">
    textColor: @color/text_secondary
    gravity: end
</style>
```

---

## 階段 2：P3 設置頁重構（4 個文件）

### 2.1 activity_setting.xml
**改動要點：**
- 根容器 padding → @dimen/spacing_xlarge
- 每行使用 TVSettingRow 樣式
- 設置項使用 TVSettingItem 樣式
- 圖標按鈕使用 TVIconButton 樣式
- 文字使用 TVSettingLabel/TVSettingValue 樣式
- 所有 16dp/18sp/24dp 等硬編碼 → @dimen 引用

### 2.2 activity_setting_player.xml
**改動要點：**
- 同 2.1 標準化改造

### 2.3 activity_setting_danmu.xml
**改動要點：**
- 同 2.1 標準化改造

### 2.4 activity_setting_custom.xml
**改動要點：**
- 同 2.1 標準化改造

---

## 階段 3：P4 播放器控制（8 個文件）

### 已有樣式（可複用）
- VideoControlButton.IconOnly
- VideoControlButton.Secondary
- VideoControlButton.Accent
- control_group_bg

### 3.1-3.4 view_control_*.xml
**改動要點：**
- padding 硬編碼 → @dimen 引用
- margin 硬編碼 → @dimen 引用
- 保持現有 VideoControlButton 樣式

### 3.5-3.8 view_widget_*.xml
**改動要點：**
- 同上，統一 dimen 引用

---

## 階段 4：P5 首頁重構（3 個文件）

### 4.1 activity_home.xml
**改動要點：**
- toolbar padding → @dimen/spacing_xlarge
- logo/title 尺寸 → @dimen 引用
- clock 文字 → TVTextAppearance.Headline
- recycler padding → @dimen 引用

### 4.2 fragment_home.xml
**改動要點：**
- 統一 dimen 引用

### 4.3 fragment_vod.xml
**改動要點：**
- 統一 dimen 引用

---

## 執行順序

1. styles.xml - 新增 TVSettingRow, TVSettingItem, TVSettingLabel, TVSettingValue
2. activity_setting.xml
3. activity_setting_player.xml
4. activity_setting_danmu.xml
5. activity_setting_custom.xml
6. view_control_vod.xml
7. view_control_live.xml
8. view_control_cast.xml
9. view_control_seek.xml
10. view_widget_vod.xml
11. view_widget_live.xml
12. view_widget_cast.xml
13. view_widget_display.xml
14. activity_home.xml
15. fragment_home.xml
16. fragment_vod.xml

---

## 驗證標準
- [ ] XML 語法驗證通過
- [ ] 無硬編碼尺寸/顏色
- [ ] 樣式引用正確
