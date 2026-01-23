# Leanback UI 重構計劃（剩餘文件）

## 任務概述
- **目標**：完成剩餘 P4 播放器控制 + P5 首頁的 Material 3 重構
- **範圍**：9 個文件
- **原則**：統一 dimen 引用，複用已建立的樣式系統

---

## 待重構文件清單

### P4 播放器控制（7 個）
1. view_control_live.xml - 硬編碼 padding/margin
2. view_control_cast.xml - 硬編碼 padding/margin
3. view_control_seek.xml - 需檢查
4. view_widget_vod.xml - 需檢查
5. view_widget_live.xml - 需檢查
6. view_widget_cast.xml - 需檢查
7. view_widget_display.xml - 需檢查

### P5 首頁（2 個）
8. fragment_home.xml - 硬編碼 padding (26dp/12dp/24dp)
9. fragment_vod.xml - 硬編碼 padding (24dp/12dp/24dp)

---

## 重構規則

### 統一替換映射
| 硬編碼值 | 替換為 |
|----------|--------|
| 4dp | @dimen/spacing_tiny |
| 8dp | @dimen/spacing_small |
| 12dp | @dimen/spacing_medium |
| 16dp | @dimen/spacing_normal |
| 20dp | @dimen/spacing_large |
| 24dp | @dimen/spacing_xlarge |
| 26dp | @dimen/spacing_xlarge |

---

## 執行順序

1. view_control_live.xml
2. view_control_cast.xml
3. view_control_seek.xml
4. view_widget_vod.xml
5. view_widget_live.xml
6. view_widget_cast.xml
7. view_widget_display.xml
8. fragment_home.xml
9. fragment_vod.xml

---

## 驗證標準
- [ ] XML 語法驗證通過
- [ ] 無硬編碼尺寸
