# 丰富 TV 变体 TMDB 首页内容

## 任务概述
参考 /temp 下的5张参考图片，将现有 TMDB 首页从仅有「Featured Banner + 热门电影 + 热门剧集」三个板块，扩展为包含约12个内容板块的丰富首页。

## 方案：单一大页面 + 统一并行数据加载

---

## 执行步骤

### Step 1: 扩展 TmdbItem 数据模型 & 新增数据类

**文件**: `app/src/tv/java/com/fongmi/android/tv/data/repository/TmdbRepository.kt`

- 为 `TmdbItem` 添加 `genreIds: List<Int>` 字段（用于类型标签显示）
- 新增数据类：
  - `TmdbGenre(id: Int, name: String)` - 影视类型
  - `TmdbProvider(id: Int, name: String, logoPath: String?)` - 播出平台
  - `TmdbCompany(id: Int, name: String, logoPath: String?)` - 电影公司
  - `TmdbSection(title: String, items: List<TmdbItem>)` - 通用板块
- 扩展 `TmdbResult.Success`，新增所有板块数据字段

### Step 2: 扩展 TmdbRepository API 调用

**文件**: `app/src/tv/java/com/fongmi/android/tv/data/repository/TmdbRepository.kt`

新增 API 调用方法（全部并行执行）：

| 方法 | API 端点 | 用途 |
|------|---------|------|
| `fetchTrendingDay()` | `trending/all/day` | 今日趋势（Hero 轮播 + 今日趋势行） |
| `fetchTrendingWeek()` | `trending/all/week` | 本周趋势 |
| `fetchPopularMovies()` | `movie/popular` | 备受欢迎·电影 |
| `fetchPopularTv()` | `tv/popular` | 备受欢迎·剧集 |
| `fetchNowPlaying()` | `movie/now_playing` | 正在热映 |
| `fetchTrendingAnime()` | `discover/tv?with_genres=16&sort_by=popularity.desc` | 今日动漫 |
| `fetchTopRatedMovies()` | `movie/top_rated` | 高分电影 |
| `fetchTopRatedTv()` | `tv/top_rated` | 高分剧集 |
| `fetchGenres()` | `genre/movie/list` + `genre/tv/list` | 类型列表 |
| `fetchWatchProviders()` | `watch/providers/movie?watch_region=CN` | 播出平台列表 |
| `fetchProviderContent(id)` | `discover/movie?with_watch_providers=X&watch_region=CN` | 平台热门内容 |
| `fetchCompanyContent(id)` | `discover/movie?with_companies=X` | 公司热门内容 |

- 修改 `loadTrendingAll()` 为 `loadAllContent()` —— 并行加载所有数据
- 使用 `coroutineScope { async {} }` 并行请求

### Step 3: 扩展 TmdbResult 和 HomeUiState

**文件**: `app/src/tv/java/com/fongmi/android/tv/data/repository/TmdbRepository.kt`

扩展 `TmdbResult.Success`:
```kotlin
data class Success(
    val trendingToday: List<TmdbItem>,    // 今日趋势（前10做轮播）
    val trendingWeek: List<TmdbItem>,     // 本周趋势
    val popularMovies: List<TmdbItem>,    // 备受欢迎·电影
    val popularTv: List<TmdbItem>,        // 备受欢迎·剧集
    val nowPlaying: List<TmdbItem>,       // 正在热映
    val trendingAnime: List<TmdbItem>,    // 今日动漫
    val topRatedMovies: List<TmdbItem>,   // 高分电影
    val topRatedTv: List<TmdbItem>,       // 高分剧集
    val genres: List<TmdbGenre>,          // 类型列表
    val providers: List<TmdbProvider>,    // 播出平台
    val companies: List<TmdbCompany>,     // 电影公司
    val providerContent: Map<Int, List<TmdbItem>>,  // 平台→内容
    val companyContent: Map<Int, List<TmdbItem>>,    // 公司→内容
    val genreContent: Map<Int, List<TmdbItem>>       // 类型→内容
) : TmdbResult()
```

**文件**: `app/src/tv/java/com/fongmi/android/tv/ui/state/UiStates.kt`

扩展 `HomeUiState` 添加对应字段。

### Step 4: 更新 HomeViewModel

**文件**: `app/src/tv/java/com/fongmi/android/tv/ui/viewmodel/HomeViewModel.kt`

- 修改 `loadTmdbContent()` 使用新的 `loadAllContent()`
- 映射 `TmdbResult.Success` 到 `HomeUiState` 的所有新字段

### Step 5: 重写 TmdbPosterWall 组件

**文件**: `app/src/tv/java/com/fongmi/android/tv/ui/components/TmdbPosterWall.kt`

重写 `TmdbContent`，按参考图顺序排列板块：

1. **TmdbHeroBanner** - 全屏轮播（取 trendingToday 前10）
   - 全屏背景图 + 标题 + 日期 + 类型标签 + 简介 + 评分
   - 自动轮播 + 分页指示器

2. **今日趋势** - `TmdbContentRow`（海报卡片行）

3. **本周趋势** - `TmdbContentRow`（海报卡片行）

4. **备受欢迎** - 三个大卡片（综艺/剧集/电影），带桂冠装饰
   - 新组件 `PopularShowcase`

5. **正在热映** - `TmdbContentRow`（横版卡片行）

6. **今日动漫** - `TmdbContentRow`（海报卡片行）

7. **播出平台** - 新组件 `ProviderRow`
   - 大卡片：Logo + 代表作背景

8. **分类浏览** - 新组件 `GenreBrowseRow`
   - 带图标 + 文字 + 缩略图的卡片

9. **电影公司** - 新组件 `CompanyRow`
   - Logo + 代表作背景

10. **高分剧集 / 高分电影** - 新组件 `TopRatedCard`
    - 桂冠装饰 + Top 3 列表 + 缩略图

### Step 6: 新增 UI 子组件文件

**新文件**: `app/src/tv/java/com/fongmi/android/tv/ui/components/TmdbHeroBanner.kt`
- TMDB 专用的全屏 Hero Banner 轮播组件
- 支持多评分源显示（IMDb, TMDb 评分）
- 类型标签、日期、简介
- 自动轮播 + 分页指示器

**新文件**: `app/src/tv/java/com/fongmi/android/tv/ui/components/TmdbSections.kt`
- `PopularShowcase` - 备受欢迎大卡片展示
- `ProviderCard` / `ProviderRow` - 播出平台卡片行
- `GenreBrowseRow` - 分类浏览行
- `CompanyCard` / `CompanyRow` - 电影公司卡片行
- `TopRatedShowcase` - 高分榜单卡片

### Step 7: 更新 HomeScreen 传参

**文件**: `app/src/tv/java/com/fongmi/android/tv/ui/screens/home/HomeScreen.kt`

- 更新 `TmdbPosterWall` 调用，传入所有新数据字段

---

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `TmdbRepository.kt` | 修改 | 扩展数据模型 + 新增 API 调用 |
| `UiStates.kt` | 修改 | 扩展 HomeUiState |
| `HomeViewModel.kt` | 修改 | 适配新数据加载 |
| `TmdbPosterWall.kt` | 重写 | 添加所有板块 |
| `TmdbHeroBanner.kt` | 新建 | TMDB 专用 Hero Banner |
| `TmdbSections.kt` | 新建 | 新板块组件集合 |
| `HomeScreen.kt` | 修改 | 更新传参 |

## 预期结果
TMDB 首页展示完整的12个板块，与参考图片一致。所有数据从 TMDB API 动态获取，首次加载并行请求所有数据。
