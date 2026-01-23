# Leanback UI 重構計劃

## 任務概述
- **目標**：將 leanback UI 重構為現代簡約風格（Material 3）
- **範圍**：adapter 布局 → 設置頁 → 播放器 → 首頁
- **原則**：複用已建立的樣式系統（TVCard, TVTextAppearance, dimens）

## 已有基礎設施
- `styles.xml`: Material 3 主題 + TV 組件樣式系統
- `dimens.xml`: 完整設計令牌（間距/圓角/陰影/字體）
- `colors.xml`: Material 3 色彩體系
- `selector_item_enhanced.xml`: 現代焦點效果（已有外發光）

---

## P1：核心列表項 Adapter（10個文件）

### 設計規範
- 統一使用 `selector_item_enhanced.xml` 作為背景
- 文字樣式引用 `TVTextAppearance.*`
- 尺寸引用 `@dimen/*`
- 顏色引用 `@color/*`

### 1.1 adapter_episode.xml
**當前狀態**：簡單 TextView，硬編碼樣式
**改造方案**：
```xml
<!-- 改為帶圓角的 chip 樣式 -->
<TextView
    style="@style/TVChip"
    android:background="@drawable/selector_item_enhanced"
    android:textAppearance="@style/TVTextAppearance.Body"
    android:paddingHorizontal="@dimen/spacing_normal"
    android:paddingVertical="@dimen/spacing_small"
    ... />
```
**預期結果**：圓角膠囊按鈕，帶焦點發光效果

### 1.2 adapter_flag.xml
**當前狀態**：簡單 TextView，使用 textbgbl 顏色選擇器
**改造方案**：同 1.1，統一 chip 樣式
**預期結果**：與 episode 風格一致

### 1.3 adapter_part.xml
**當前狀態**：簡單 TextView，白色文字
**改造方案**：同 1.1
**預期結果**：統一風格

### 1.4 adapter_parse.xml
**當前狀態**：已使用 VideoControlButton.Secondary 樣式
**改造方案**：微調 padding 使用 dimen 引用
**預期結果**：保持現有風格，統一尺寸引用

### 1.5 adapter_quick.xml
**當前狀態**：LinearLayout + 3個 TextView，硬編碼顏色 #ccffffff
**改造方案**：
```xml
<androidx.cardview.widget.CardView style="@style/TVCard.Small">
    <LinearLayout ...>
        <TextView style="@style/TVTextAppearance.Body" />      <!-- name -->
        <TextView style="@style/TVTextAppearance.Caption" />   <!-- site -->
        <TextView style="@style/TVTextAppearance.Caption" />   <!-- remark -->
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
**預期結果**：卡片式快捷項，帶陰影和圓角

### 1.6 adapter_type.xml
**當前狀態**：CustomTypeView，使用 selector_item_round
**改造方案**：更新為 selector_item_enhanced，引用 dimen
**預期結果**：統一焦點效果

### 1.7 adapter_header.xml
**當前狀態**：簡單 TextView，24sp 白色
**改造方案**：
```xml
<TextView style="@style/TVTextAppearance.Headline" />
```
**預期結果**：統一標題樣式

### 1.8 adapter_array.xml
**當前狀態**：簡單 TextView，selector_item 背景
**改造方案**：同 1.1 chip 樣式
**預期結果**：統一風格

### 1.9 adapter_filter.xml
**當前狀態**：TextView，selector_item_round，textbgbl
**改造方案**：更新為 selector_item_enhanced
**預期結果**：統一焦點效果

### 1.10 adapter_group.xml
**當前狀態**：需檢查
**改造方案**：統一卡片/chip 樣式
**預期結果**：統一風格

---

## P1 補充：新增樣式定義

### styles.xml 新增
```xml
<!-- Chip/Tag 按鈕樣式（用於 episode, flag, part 等） -->
<style name="TVChip" parent="">
    <item name="android:layout_width">wrap_content</item>
    <item name="android:layout_height">wrap_content</item>
    <item name="android:background">@drawable/selector_item_enhanced</item>
    <item name="android:paddingHorizontal">@dimen/spacing_normal</item>
    <item name="android:paddingVertical">@dimen/spacing_small</item>
    <item name="android:gravity">center</item>
    <item name="android:focusable">true</item>
    <item name="android:focusableInTouchMode">true</item>
    <item name="android:singleLine">true</item>
    <item name="android:ellipsize">marquee</item>
</style>

<style name="TVChip.Text" parent="TVChip">
    <item name="android:textAppearance">@style/TVTextAppearance.Body</item>
    <item name="android:textColor">@color/text_primary_high_emphasis</item>
</style>
```

---

## P2：VOD 卡片系列（4個文件）

### 2.1 adapter_vod.xml
**當前狀態**：RelativeLayout + 多個 TextView，硬編碼尺寸
**改造方案**：
- 根容器改為 CardView + style="@style/TVVodCard"
- 標籤使用 TVTag 樣式
- 名稱使用 TVVodTitle 樣式
- 尺寸全部引用 dimen
**預期結果**：現代卡片效果

### 2.2-2.4 adapter_vod_rect/oval/list.xml
**改造方案**：同 2.1，根據形狀調整圓角

---

## P3：設置相關（待 P1/P2 完成後細化）

## P4：播放器控制（待 P3 完成後細化）

## P5：首頁（待 P4 完成後細化）

---

## 執行順序

### 第一階段：樣式補充
1. 在 styles.xml 新增 TVChip 系列樣式

### 第二階段：P1 Adapter 重構
2. adapter_episode.xml
3. adapter_flag.xml
4. adapter_part.xml
5. adapter_array.xml
6. adapter_filter.xml
7. adapter_type.xml
8. adapter_header.xml
9. adapter_quick.xml
10. adapter_parse.xml（微調）
11. adapter_group.xml

### 第三階段：P2 VOD 卡片
12. adapter_vod.xml
13. adapter_vod_rect.xml
14. adapter_vod_oval.xml
15. adapter_vod_list.xml

---

## 驗證標準
- [ ] 構建成功：`./gradlew assembleLeanbackJavaArm64_v8aDebug`
- [ ] 無硬編碼尺寸/顏色
- [ ] 焦點效果統一
- [ ] 視覺風格一致
