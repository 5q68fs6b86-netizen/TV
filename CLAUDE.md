# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

基於 CatVod 的 Android TV 影視播放應用，支持點播/直播聚合，通過插件化方式（Jar/JS/Python）解析多種視頻源。

## Build Commands

```bash
# 構建指定變體 APK（格式：mode-api-abi）
./gradlew assembleTvJavaArm64_v8aRelease      # TV Compose UI + Java API + ARM64
./gradlew assembleLeanbackJavaArm64_v8aRelease # Leanback UI + Java API + ARM64
./gradlew assembleMobileJavaArm64_v8aRelease   # Mobile UI + Java API + ARM64

# 使用 Python 腳本引擎
./gradlew assembleTvPythonArm64_v8aRelease

# 清理和檢查
./gradlew clean
./gradlew lint

# 列出所有可用變體
./gradlew tasks --group=build
```

## Product Flavors

項目採用三維度變體配置：

| Dimension | Flavors | Description |
|-----------|---------|-------------|
| mode | `tv`, `leanback`, `mobile` | UI 框架：Compose TV / Leanback / Mobile |
| api | `java`, `python` | 腳本引擎：Java-only / 含 Python 支持 |
| abi | `arm64_v8a`, `armeabi_v7a`, `x86` | CPU 架構 |

## Architecture

### Source Set Layout

```
app/src/
├── main/          # 共享核心代碼
├── tv/            # Compose for TV (MVVM + Hilt)
├── leanback/      # 傳統 Leanback UI
├── mobile/        # 手機端 UI
├── arm64_v8a/     # ARM64 native libs
├── armeabi_v7a/   # ARMv7 native libs
└── x86/           # x86 native libs
```

### Key Modules

| Module | Purpose |
|--------|---------|
| `:app` | 主應用，包含 UI、播放器、業務邏輯 |
| `:catvod` | 插件加載核心（Jar/JS/Python Loader） |
| `:quickjs` | JavaScript 腳本執行引擎 |
| `:pyramid` | Python 腳本執行支持（python flavor） |
| `:ijkplayer` | IJKPlayer 播放器封裝 |
| `:thunder`, `:youtube`, `:tvbus`, `:zlive`, `:jianpian`, `:forcetech` | 協議/平台特定解析庫 |

### Core Components (main source set)

- `App.java` - Application 入口
- `Setting.java` - 應用設置管理
- `api/` - VodConfig/LiveConfig 配置加載
- `player/` - ExoPlayer/IJKPlayer 播放器實現
- `server/` - 本地 HTTP 服務器（API 接口、緩存、代理）
- `db/` - Room 數據庫（歷史、收藏、站點配置）
- `bean/` - 數據模型

### TV Compose Flavor (tv source set)

- `TvApp.kt` - Hilt Application
- `di/` - 依賴注入模塊
- `ui/` - Compose UI 組件
- `data/` - Repository 層

## Key Dependencies

- **UI**: Jetpack Compose for TV (`tv` flavor), Leanback (`leanback` flavor)
- **DI**: Hilt/Dagger
- **Player**: Media3 (ExoPlayer), IJKPlayer
- **Network**: OkHttp 5.x, Cronet
- **Database**: Room
- **Events**: EventBus

## Local API Endpoints

應用運行時提供本地服務器（端口 9978）：

```
http://127.0.0.1:9978/action?do=refresh&type=detail  # 刷新詳情
http://127.0.0.1:9978/action?do=refresh&type=player  # 刷新播放
http://127.0.0.1:9978/action?do=refresh&type=live    # 刷新直播
http://127.0.0.1:9978/cache?do=set&key=xxx&value=xxx # 設置緩存
http://127.0.0.1:9978/cache?do=get&key=xxx           # 獲取緩存
```

## Configuration Fields

### VOD (點播)

| Field | Default | Description |
|-------|---------|-------------|
| searchable | 1 | 是否可搜索 |
| changeable | 1 | 是否換源 |
| playerType | none | 0:系統 / 1:IJK / 2:EXO |
| timeout | 15 | 播放超時（秒） |

### Live (直播)

| Field | Default | Description |
|-------|---------|-------------|
| ua/origin/referer | none | 請求標頭 |
| epg | none | 節目地址 |
| logo | none | 台標地址 |
| playerType | none | 0:系統 / 1:IJK / 2:EXO |
