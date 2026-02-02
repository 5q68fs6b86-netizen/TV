# TV 变体功能补充计划

**创建时间**: 2026-01-25 01:01:01
**任务描述**: 补充 TV 变体的全部缺失功能

## 实现批次

### 第一批：播放器增强
- [x] TrackDialog - 音轨/字幕轨道选择
- [x] 画中画模式支持
- [x] 投屏功能（基础）

### 第二批：搜索功能
- [x] 多站点聚合搜索
- [x] 搜索历史管理
- [x] 搜索建议

### 第三批：直播功能
- [x] EPG 节目单显示
- [x] 频道收藏
- [x] 回看功能

### 第四批：数据管理
- [x] 历史记录清理
- [x] 收藏夹管理增强
- [x] 数据导入导出

### 第五批：UI/UX 优化
- [x] 键盘快捷键完善
- [x] 焦点导航优化
- [x] 动画效果增强

---

## 执行状态

- [x] 计划创建
- [x] 第一批完成
- [x] 第二批完成
- [x] 第三批完成
- [x] 第四批完成
- [x] 第五批完成 ✅ 全部完成

## 第一批完成详情

### 1. TrackDialog（音轨/字幕选择）✅
- 新建 `ui/dialog/TrackDialog.kt`
- 包含 `TrackType`、`TrackInfo`、`TrackDialog`、`MultiTrackDialog`
- 更新 `PlayerViewModel.kt` 添加轨道状态管理
- 更新 `PlayerScreen.kt` 集成轨道监听和对话框

### 2. 画中画模式 ✅
- 更新 `PlayerViewModel.kt` 添加 PiP 状态（`isInPipMode`、`enterPipMode()`、`exitPipMode()`）
- 注：完整 PiP 需要 Activity 层配合，已预留接口

### 3. 投屏功能 ✅
- 新建 `ui/dialog/CastDialog.kt`
- 包含 `CastDevice`、`CastDeviceType`、`CastDialog`
- 更新 `PlayerViewModel.kt` 添加投屏状态和方法
- 更新 `PlayerScreen.kt` 集成 CastDialog
