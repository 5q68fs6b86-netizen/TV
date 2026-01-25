package com.fongmi.android.tv.ui.state

import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.data.repository.SiteSearchResult

/**
 * UI State for Home Screen
 * Follows MVI pattern with immutable state
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val currentSite: Site? = null,
    val categories: List<Class> = emptyList(),
    val selectedCategory: Class? = null,
    val featuredContent: List<Vod> = emptyList(),
    val categoryContent: Map<String, List<Vod>> = emptyMap(),
    val error: String? = null
)

/**
 * UI State for Detail Screen
 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val vod: Vod? = null,
    val relatedContent: List<Vod> = emptyList(),
    val error: String? = null
)

/**
 * UI State for Search Screen
 */
data class SearchUiState(
    val keyword: String = "",
    val isSearching: Boolean = false,
    val results: List<Vod> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val hotSearches: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val error: String? = null,
    // Multi-site search
    val isAggregatedSearch: Boolean = false,
    val siteResults: List<SiteSearchResult> = emptyList(),
    val searchableSites: List<Site> = emptyList(),
    val selectedSites: List<Site> = emptyList()
)

/**
 * UI State for Player Screen
 */
data class PlayerUiState(
    val isLoading: Boolean = true,
    val vodName: String = "",
    val currentEpisodeName: String = "",
    val playUrl: String? = null,
    val subtitles: List<String> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)

/**
 * UI State for Settings Screen
 */
data class SettingsUiState(
    val sites: List<Site> = emptyList(),
    val currentSite: Site? = null,
    val playerSettings: PlayerSettings = PlayerSettings(),
    val displaySettings: DisplaySettings = DisplaySettings(),
    // Dialog states
    val showVodConfigDialog: Boolean = false,
    val showLiveConfigDialog: Boolean = false,
    val showWallConfigDialog: Boolean = false,
    val showSiteDialog: Boolean = false,
    val showProxyDialog: Boolean = false,
    val showDohDialog: Boolean = false,
    val showBackupDialog: Boolean = false,
    val showVodHistoryDialog: Boolean = false,
    val showLiveHistoryDialog: Boolean = false,
    val showLiveDialog: Boolean = false,
    val showUaDialog: Boolean = false,
    val dohIndex: Int = 0
)

data class PlayerSettings(
    val defaultPlayer: Int = 0,
    val decodeType: Int = 0,
    val bufferSize: Int = 2
)

data class DisplaySettings(
    val size: Int = 4,
    val wallpaper: Int = 1,
    val language: Int = 0
)
