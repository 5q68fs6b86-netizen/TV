package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.db.AppDatabase
import com.fongmi.android.tv.data.repository.DanmuRepository
import com.fongmi.android.tv.data.repository.DetailResult
import com.fongmi.android.tv.data.repository.ParseUrlResult
import com.fongmi.android.tv.data.repository.PlayUrlResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.state.DetailUiState
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Detail Screen
 * Manages video detail, episodes, and playback preparation
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    private val danmuRepository: DanmuRepository,
    private val playerStateHolder: PlayerStateHolder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Selected flag (play source) and episode
    private val _selectedFlag = MutableStateFlow<Flag?>(null)
    val selectedFlag: StateFlow<Flag?> = _selectedFlag.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    val selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    // Play URL result
    private val _playUrlState = MutableStateFlow<PlayUrlState>(PlayUrlState.Idle)
    val playUrlState: StateFlow<PlayUrlState> = _playUrlState.asStateFlow()

    // Favorite state
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // History state
    private val _history = MutableStateFlow<History?>(null)
    val history: StateFlow<History?> = _history.asStateFlow()

    // Get site key and vod id from navigation arguments
    private val siteKey: String? = savedStateHandle["siteKey"]
    private val vodId: String? = savedStateHandle["vodId"]

    init {
        if (siteKey != null && vodId != null) {
            loadDetail(siteKey, vodId)
        }
    }

    /**
     * Load video detail
     */
    fun loadDetail(siteKey: String, vodId: String) {
        val site = VodConfig.get().getSite(siteKey) ?: return

        viewModelScope.launch {
            vodRepository.loadDetail(site, vodId).collect { result ->
                when (result) {
                    is DetailResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is DetailResult.Success -> {
                        val vod = result.vod
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                vod = vod,
                                error = null
                            )
                        }
                        // Auto-select first flag and episode
                        vod.vodFlags?.firstOrNull()?.let { flag ->
                            selectFlag(flag)
                        }
                        // Check favorite status
                        checkFavoriteStatus(siteKey, vod.vodId ?: "")
                        // Load history
                        loadHistory(siteKey, vod.vodId ?: "", vod.vodFlags ?: emptyList())
                    }
                    is DetailResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Get all available flags (play sources)
     */
    fun getFlags(): List<Flag> = _uiState.value.vod?.vodFlags ?: emptyList()

    /**
     * Get episodes for current selected flag
     */
    fun getEpisodes(): List<Episode> = _selectedFlag.value?.episodes ?: emptyList()

    /**
     * Select a flag (play source)
     */
    fun selectFlag(flag: Flag) {
        _selectedFlag.update { flag }

        // Auto-select first episode
        flag.episodes?.firstOrNull()?.let { episode ->
            selectEpisode(episode)
        }
    }

    /**
     * Select an episode
     */
    fun selectEpisode(episode: Episode) {
        _selectedEpisode.update { episode }
    }

    /**
     * Play selected episode
     * Handles both direct URLs and URLs that need parsing
     */
    fun play() {
        val vod = _uiState.value.vod ?: return
        val flag = _selectedFlag.value ?: return
        val episode = _selectedEpisode.value ?: return
        val site = vod.site ?: return

        // Calculate current indices
        val flags = vod.vodFlags ?: emptyList()
        val flagIndex = flags.indexOf(flag).coerceAtLeast(0)
        val episodeIndex = (flag.episodes?.indexOf(episode) ?: 0).coerceAtLeast(0)

        viewModelScope.launch {
            _playUrlState.update { PlayUrlState.Loading }

            vodRepository.getPlayUrl(site, flag.flag ?: "", episode.url ?: "").collect { result ->
                when (result) {
                    is PlayUrlResult.Loading -> {
                        _playUrlState.update { PlayUrlState.Loading }
                    }
                    is PlayUrlResult.Success -> {
                        if (result.needParse) {
                            // URL needs parsing - use ParseJob
                            _playUrlState.update { PlayUrlState.Parsing }
                            parseAndPlay(
                                siteKey = site.key ?: "",
                                url = result.url,
                                flag = result.parseFlag ?: flag.flag ?: "",
                                vod = vod,
                                episode = episode,
                                flags = flags,
                                flagIndex = flagIndex,
                                episodeIndex = episodeIndex
                            )
                        } else {
                            // Direct URL - play immediately
                            setPlayerStateAndPlay(
                                url = result.url,
                                headers = result.headers,
                                vod = vod,
                                episode = episode,
                                site = site,
                                flags = flags,
                                flagIndex = flagIndex,
                                episodeIndex = episodeIndex
                            )
                        }
                    }
                    is PlayUrlResult.Error -> {
                        _playUrlState.update { PlayUrlState.Error(result.message) }
                    }
                }
            }
        }
    }

    /**
     * Parse URL and then play
     */
    private fun parseAndPlay(
        siteKey: String,
        url: String,
        flag: String,
        vod: Vod,
        episode: Episode,
        flags: List<Flag>,
        flagIndex: Int,
        episodeIndex: Int
    ) {
        viewModelScope.launch {
            vodRepository.parseUrl(siteKey, url, flag).collect { result ->
                when (result) {
                    is ParseUrlResult.Loading -> {
                        _playUrlState.update { PlayUrlState.Parsing }
                    }
                    is ParseUrlResult.Success -> {
                        setPlayerStateAndPlay(
                            url = result.url,
                            headers = result.headers,
                            vod = vod,
                            episode = episode,
                            site = vod.site!!,
                            flags = flags,
                            flagIndex = flagIndex,
                            episodeIndex = episodeIndex
                        )
                    }
                    is ParseUrlResult.Error -> {
                        _playUrlState.update { PlayUrlState.Error(result.message) }
                    }
                }
            }
        }
    }

    /**
     * Set player state and trigger playback
     */
    private fun setPlayerStateAndPlay(
        url: String,
        headers: Map<String, String>?,
        vod: Vod,
        episode: Episode,
        site: com.fongmi.android.tv.bean.Site,
        flags: List<Flag>,
        flagIndex: Int,
        episodeIndex: Int
    ) {
        // Set player state for sharing with PlayerScreen
        playerStateHolder.setPlaybackData(
            vodName = vod.vodName ?: "",
            vodPic = vod.vodPic ?: "",
            siteKey = site.key ?: "",
            vodId = vod.vodId ?: "",
            flags = flags,
            currentFlagIndex = flagIndex,
            currentEpisodeIndex = episodeIndex,
            url = url,
            headers = headers
        )

        _playUrlState.update {
            PlayUrlState.Ready(
                url = url,
                headers = headers,
                vodName = vod.vodName ?: "",
                episodeName = episode.name ?: ""
            )
        }

        // Async match danmu - fire and forget, don't block playback
        if (com.fongmi.android.tv.Setting.isDanmuLoad()) {
            viewModelScope.launch {
                val result = danmuRepository.matchDanmu(
                    title = vod.vodName ?: "",
                    episode = episode.name ?: ""
                )
                if (result.success && result.danmuUrl.isNotEmpty()) {
                    playerStateHolder.updateUrl(url, headers, result.danmuUrl)
                }
            }
        }
    }

    /**
     * Clear play URL state
     */
    fun clearPlayState() {
        _playUrlState.update { PlayUrlState.Idle }
    }

    /**
     * Retry loading detail
     */
    fun retry() {
        if (siteKey != null && vodId != null) {
            loadDetail(siteKey, vodId)
        }
    }

    /**
     * Check if current vod is in favorites
     */
    private fun checkFavoriteStatus(siteKey: String, vodId: String) {
        viewModelScope.launch {
            val key = siteKey + AppDatabase.SYMBOL + vodId
            val keep = Keep.find(VodConfig.getCid(), key)
            _isFavorite.update { keep != null }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite() {
        val vod = _uiState.value.vod ?: return
        val currentSiteKey = siteKey ?: return
        val currentVodId = vod.vodId ?: return

        viewModelScope.launch {
            val key = currentSiteKey + AppDatabase.SYMBOL + currentVodId
            val cid = VodConfig.getCid()

            if (_isFavorite.value) {
                // Remove from favorites
                Keep.find(cid, key)?.delete()
                _isFavorite.update { false }
            } else {
                // Add to favorites
                val keep = Keep().apply {
                    setKey(key)
                    setSiteName(VodConfig.get().home?.name ?: "")
                    setVodName(vod.vodName ?: "")
                    setVodPic(vod.vodPic ?: "")
                    setCreateTime(System.currentTimeMillis())
                    setType(0) // VOD type
                }
                keep.save(cid)
                _isFavorite.update { true }
            }
        }
    }

    /**
     * Load history for current vod
     */
    private fun loadHistory(siteKey: String, vodId: String, flags: List<Flag>) {
        viewModelScope.launch {
            val key = siteKey + AppDatabase.SYMBOL + vodId
            val history = History.find(key)
            _history.update { history }

            // If history exists, restore last played episode
            if (history != null && flags.isNotEmpty()) {
                // Find the flag that matches history
                val historyFlag = flags.find { it.flag == history.vodFlag }
                if (historyFlag != null) {
                    selectFlag(historyFlag)
                    // Find the episode that matches history
                    val historyEpisode = historyFlag.episodes?.find {
                        it.name == history.vodRemarks
                    }
                    if (historyEpisode != null) {
                        selectEpisode(historyEpisode)
                    }
                }
            }
        }
    }

    /**
     * Continue playing from history
     */
    fun continuePlay() {
        play()
    }

    /**
     * Get formatted progress text
     */
    fun getProgressText(): String {
        val history = _history.value ?: return ""
        val position = history.position
        val duration = history.duration
        if (position <= 0 || duration <= 0) return ""

        val progress = (position * 100 / duration).toInt()
        val positionMin = position / 1000 / 60
        val positionSec = position / 1000 % 60
        return String.format("已观看 %d:%02d (%d%%)", positionMin, positionSec, progress)
    }
}

/**
 * Play URL state
 */
sealed class PlayUrlState {
    data object Idle : PlayUrlState()
    data object Loading : PlayUrlState()
    data object Parsing : PlayUrlState()  // URL is being parsed (jx/sniff)
    data class Ready(
        val url: String,
        val headers: Map<String, String>?,
        val vodName: String,
        val episodeName: String
    ) : PlayUrlState()
    data class Error(val message: String) : PlayUrlState()
}
