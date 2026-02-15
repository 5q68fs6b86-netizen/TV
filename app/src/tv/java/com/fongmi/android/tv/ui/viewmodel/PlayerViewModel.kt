package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.data.repository.DanmuEpisodesState
import com.fongmi.android.tv.data.repository.DanmuRepository
import com.fongmi.android.tv.data.repository.DanmuSearchAnime
import com.fongmi.android.tv.data.repository.DanmuSearchEpisode
import com.fongmi.android.tv.data.repository.DanmuSearchState
import com.fongmi.android.tv.data.repository.PlayUrlResult
import com.fongmi.android.tv.data.repository.VodRepository
import com.fongmi.android.tv.ui.dialog.CastDevice
import com.fongmi.android.tv.ui.dialog.TrackInfo
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Player UI State
 */
data class PlayerUiState(
    val vodName: String = "",
    val episodeName: String = "",
    val url: String = "",
    val headers: Map<String, String>? = null,
    val danmuUrl: String = "",
    val flags: List<Flag> = emptyList(),
    val currentFlagIndex: Int = 0,
    val currentEpisodeIndex: Int = 0,
    val isLoadingEpisode: Boolean = false,
    val error: String? = null,
    // Live TV specific fields
    val isLive: Boolean = false,
    val liveGroups: List<Group> = emptyList(),
    val currentGroupIndex: Int = 0,
    val currentChannelIndex: Int = 0,
    // Dialog states
    val showSpeedDialog: Boolean = false,
    val showPlayerDialog: Boolean = false,
    val showDecodeDialog: Boolean = false,
    val showEpisodeDialog: Boolean = false,
    val showDisplayDialog: Boolean = false,
    val showTrackDialog: Boolean = false,
    val currentSpeed: Float = 1.0f,
    val currentPlayer: Int = 0,
    val currentDecode: Int = 0,
    val currentScale: Int = 0,
    // Track selection
    val audioTracks: List<TrackInfo> = emptyList(),
    val subtitleTracks: List<TrackInfo> = emptyList(),
    val selectedAudioTrack: Int = -1,
    val selectedSubtitleTrack: Int = -1,
    // Cast
    val showCastDialog: Boolean = false,
    val castDevices: List<CastDevice> = emptyList(),
    val isCastScanning: Boolean = false,
    // PiP
    val isInPipMode: Boolean = false,
    // Danmu search dialog
    val showDanmuSearchDialog: Boolean = false,
    val danmuSearchResults: List<DanmuSearchAnime> = emptyList(),
    val danmuEpisodeResults: List<DanmuSearchEpisode> = emptyList(),
    val isDanmuSearching: Boolean = false,
    val isDanmuLoadingEpisodes: Boolean = false,
    val selectedDanmuAnime: DanmuSearchAnime? = null,
    val isDanmuMatching: Boolean = false
) {
    val hasEpisodes: Boolean
        get() = flags.isNotEmpty() && flags.any { (it.episodes?.size ?: 0) > 0 }

    val currentFlag: Flag?
        get() = flags.getOrNull(currentFlagIndex)

    val currentEpisode: Episode?
        get() = currentFlag?.episodes?.getOrNull(currentEpisodeIndex)

    val hasDanmu: Boolean
        get() = danmuUrl.isNotEmpty()

    // Live TV helpers
    val hasChannels: Boolean
        get() = isLive && liveGroups.any { (it.channel?.size ?: 0) > 0 }

    val currentGroup: Group?
        get() = liveGroups.getOrNull(currentGroupIndex)

    val currentChannel: Channel?
        get() = currentGroup?.channel?.getOrNull(currentChannelIndex)

    val totalChannels: Int
        get() = liveGroups.sumOf { it.channel?.size ?: 0 }

    val flatChannelIndex: Int
        get() {
            var index = 0
            for (i in 0 until currentGroupIndex) {
                index += liveGroups.getOrNull(i)?.channel?.size ?: 0
            }
            return index + currentChannelIndex
        }
}

/**
 * ViewModel for PlayerScreen
 * Manages playback state and episode switching
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerStateHolder: PlayerStateHolder,
    private val vodRepository: VodRepository,
    private val danmuRepository: DanmuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadFromStateHolder()
        observeDanmuUrlUpdates()
    }

    /**
     * Load initial state from PlayerStateHolder
     */
    private fun loadFromStateHolder() {
        val data = playerStateHolder.playbackData ?: return

        _uiState.update {
            PlayerUiState(
                vodName = data.vodName,
                episodeName = data.episodeName,
                url = data.url,
                headers = data.headers,
                danmuUrl = data.danmuUrl,
                flags = data.flags,
                currentFlagIndex = data.currentFlagIndex,
                currentEpisodeIndex = data.currentEpisodeIndex,
                // Live TV fields
                isLive = data.isLive,
                liveGroups = data.liveGroups,
                currentGroupIndex = data.currentGroupIndex,
                currentChannelIndex = data.currentChannelIndex
            )
        }
    }

    /**
     * Observe danmu URL updates from PlayerStateHolder (set by DetailViewModel async match)
     */
    private fun observeDanmuUrlUpdates() {
        viewModelScope.launch {
            playerStateHolder.danmuUrlFlow.collect { danmuUrl ->
                if (danmuUrl.isNotEmpty() && danmuUrl != _uiState.value.danmuUrl) {
                    _uiState.update { it.copy(danmuUrl = danmuUrl) }
                }
            }
        }
    }

    /**
     * Select a different flag (play source)
     */
    fun selectFlag(flagIndex: Int) {
        if (flagIndex == _uiState.value.currentFlagIndex) return

        _uiState.update {
            it.copy(currentFlagIndex = flagIndex)
        }
        playerStateHolder.updateSelection(flagIndex, _uiState.value.currentEpisodeIndex)
    }

    /**
     * Select and play a different episode
     */
    fun selectEpisode(episode: Episode) {
        val state = _uiState.value
        val currentFlag = state.currentFlag ?: return
        val episodeIndex = currentFlag.episodes?.indexOf(episode) ?: -1
        if (episodeIndex < 0) return

        // If same episode, do nothing
        if (episodeIndex == state.currentEpisodeIndex &&
            state.currentFlagIndex == _uiState.value.currentFlagIndex) {
            return
        }

        // Update state and load new URL
        _uiState.update {
            it.copy(
                currentEpisodeIndex = episodeIndex,
                isLoadingEpisode = true,
                error = null
            )
        }
        playerStateHolder.updateSelection(state.currentFlagIndex, episodeIndex)

        loadEpisodeUrl(currentFlag, episode)
    }

    /**
     * Play next episode
     */
    fun playNext(): Boolean {
        val state = _uiState.value
        val currentFlag = state.currentFlag ?: return false
        val episodes = currentFlag.episodes ?: return false

        val nextIndex = state.currentEpisodeIndex + 1
        if (nextIndex >= episodes.size) return false

        val nextEpisode = episodes[nextIndex]
        selectEpisode(nextEpisode)
        return true
    }

    /**
     * Play previous episode
     */
    fun playPrevious(): Boolean {
        val state = _uiState.value
        val currentFlag = state.currentFlag ?: return false
        val episodes = currentFlag.episodes ?: return false

        val prevIndex = state.currentEpisodeIndex - 1
        if (prevIndex < 0) return false

        val prevEpisode = episodes[prevIndex]
        selectEpisode(prevEpisode)
        return true
    }

    /**
     * Load play URL for selected episode
     */
    private fun loadEpisodeUrl(flag: Flag, episode: Episode) {
        val data = playerStateHolder.playbackData ?: return
        val site = VodConfig.get().getSite(data.siteKey) ?: return

        viewModelScope.launch {
            vodRepository.getPlayUrl(site, flag.flag ?: "", episode.url ?: "").collect { result ->
                when (result) {
                    is PlayUrlResult.Loading -> {
                        _uiState.update { it.copy(isLoadingEpisode = true) }
                    }
                    is PlayUrlResult.Success -> {
                        playerStateHolder.updateUrl(result.url, result.headers)
                        _uiState.update {
                            it.copy(
                                url = result.url,
                                headers = result.headers,
                                episodeName = episode.name ?: "",
                                isLoadingEpisode = false,
                                error = null
                            )
                        }
                        // Auto-match danmu for new episode
                        if (com.fongmi.android.tv.Setting.isDanmuLoad()) {
                            matchDanmuForEpisode(
                                _uiState.value.vodName,
                                episode.name ?: "",
                                result.url,
                                result.headers
                            )
                        }
                    }
                    is PlayUrlResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingEpisode = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Clear player state on exit
     */
    fun clearState() {
        playerStateHolder.clear()
    }

    // ========== Live TV Channel Methods ==========

    /**
     * Switch to next channel (live mode only)
     */
    fun nextChannel(): Boolean {
        if (!_uiState.value.isLive) return false

        val success = playerStateHolder.nextChannel()
        if (success) {
            refreshFromStateHolder()
        }
        return success
    }

    /**
     * Switch to previous channel (live mode only)
     */
    fun previousChannel(): Boolean {
        if (!_uiState.value.isLive) return false

        val success = playerStateHolder.previousChannel()
        if (success) {
            refreshFromStateHolder()
        }
        return success
    }

    /**
     * Switch to a specific channel by flat index (live mode only)
     */
    fun switchToChannel(flatIndex: Int): Boolean {
        if (!_uiState.value.isLive) return false

        val success = playerStateHolder.switchToChannel(flatIndex)
        if (success) {
            refreshFromStateHolder()
        }
        return success
    }

    /**
     * Refresh UI state from PlayerStateHolder
     */
    private fun refreshFromStateHolder() {
        val data = playerStateHolder.playbackData ?: return
        _uiState.update {
            it.copy(
                vodName = data.vodName,
                url = data.url,
                headers = data.headers,
                currentGroupIndex = data.currentGroupIndex,
                currentChannelIndex = data.currentChannelIndex
            )
        }
    }

    // ========== Dialog Control Methods ==========

    fun showSpeedDialog() {
        _uiState.update { it.copy(showSpeedDialog = true) }
    }

    fun dismissSpeedDialog() {
        _uiState.update { it.copy(showSpeedDialog = false) }
    }

    fun setSpeed(speed: Float) {
        _uiState.update { it.copy(currentSpeed = speed, showSpeedDialog = false) }
    }

    fun showPlayerDialog() {
        _uiState.update { it.copy(showPlayerDialog = true) }
    }

    fun dismissPlayerDialog() {
        _uiState.update { it.copy(showPlayerDialog = false) }
    }

    fun setPlayer(player: Int) {
        com.fongmi.android.tv.Setting.putPlayer(player)
        _uiState.update { it.copy(currentPlayer = player, showPlayerDialog = false) }
    }

    fun showDecodeDialog() {
        _uiState.update { it.copy(showDecodeDialog = true) }
    }

    fun dismissDecodeDialog() {
        _uiState.update { it.copy(showDecodeDialog = false) }
    }

    fun setDecode(decode: Int) {
        com.fongmi.android.tv.Setting.putDecode(com.fongmi.android.tv.Setting.getPlayer(), decode)
        _uiState.update { it.copy(currentDecode = decode, showDecodeDialog = false) }
    }

    fun showEpisodeDialog() {
        _uiState.update { it.copy(showEpisodeDialog = true) }
    }

    fun dismissEpisodeDialog() {
        _uiState.update { it.copy(showEpisodeDialog = false) }
    }

    fun showDisplayDialog() {
        _uiState.update { it.copy(showDisplayDialog = true) }
    }

    fun dismissDisplayDialog() {
        _uiState.update { it.copy(showDisplayDialog = false) }
    }

    fun setScale(scale: Int) {
        _uiState.update { it.copy(currentScale = scale, showDisplayDialog = false) }
    }

    // ========== Track Dialog Methods ==========

    fun showTrackDialog() {
        _uiState.update { it.copy(showTrackDialog = true) }
    }

    fun dismissTrackDialog() {
        _uiState.update { it.copy(showTrackDialog = false) }
    }

    fun updateTracks(audioTracks: List<TrackInfo>, subtitleTracks: List<TrackInfo>) {
        _uiState.update {
            it.copy(audioTracks = audioTracks, subtitleTracks = subtitleTracks)
        }
    }

    fun selectAudioTrack(index: Int) {
        _uiState.update { it.copy(selectedAudioTrack = index) }
    }

    fun selectSubtitleTrack(index: Int) {
        _uiState.update { it.copy(selectedSubtitleTrack = index) }
    }

    // ========== Cast Methods ==========

    fun showCastDialog() {
        _uiState.update { it.copy(showCastDialog = true) }
        scanCastDevices()
    }

    fun dismissCastDialog() {
        _uiState.update { it.copy(showCastDialog = false) }
    }

    fun scanCastDevices() {
        _uiState.update { it.copy(isCastScanning = true) }
        viewModelScope.launch {
            // Simulate device scanning - in real implementation, use DLNA/Cast SDK
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isCastScanning = false) }
        }
    }

    fun connectCastDevice(device: CastDevice) {
        val updatedDevices = _uiState.value.castDevices.map {
            it.copy(isConnected = it.id == device.id)
        }
        _uiState.update { it.copy(castDevices = updatedDevices) }
    }

    fun disconnectCast() {
        val updatedDevices = _uiState.value.castDevices.map {
            it.copy(isConnected = false)
        }
        _uiState.update { it.copy(castDevices = updatedDevices) }
    }

    // ========== PiP Methods ==========

    fun enterPipMode() {
        _uiState.update { it.copy(isInPipMode = true) }
    }

    fun exitPipMode() {
        _uiState.update { it.copy(isInPipMode = false) }
    }

    // ========== Danmu Methods ==========

    /**
     * Auto-match danmu for current episode
     */
    private fun matchDanmuForEpisode(
        vodName: String,
        episodeName: String,
        currentUrl: String,
        currentHeaders: Map<String, String>?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDanmuMatching = true) }
            val result = danmuRepository.matchDanmu(vodName, episodeName)
            if (result.success && result.danmuUrl.isNotEmpty()) {
                playerStateHolder.updateUrl(currentUrl, currentHeaders, result.danmuUrl)
                _uiState.update {
                    it.copy(
                        danmuUrl = result.danmuUrl,
                        isDanmuMatching = false
                    )
                }
            } else {
                _uiState.update { it.copy(isDanmuMatching = false) }
            }
        }
    }

    /**
     * Show danmu search dialog
     */
    fun showDanmuSearchDialog() {
        _uiState.update {
            it.copy(
                showDanmuSearchDialog = true,
                danmuSearchResults = emptyList(),
                danmuEpisodeResults = emptyList(),
                selectedDanmuAnime = null
            )
        }
    }

    /**
     * Dismiss danmu search dialog
     */
    fun dismissDanmuSearchDialog() {
        _uiState.update { it.copy(showDanmuSearchDialog = false) }
    }

    /**
     * Search danmu by keyword
     */
    fun searchDanmu(keyword: String) {
        viewModelScope.launch {
            danmuRepository.searchDanmu(keyword).collect { state ->
                when (state) {
                    is DanmuSearchState.Loading -> {
                        _uiState.update { it.copy(isDanmuSearching = true) }
                    }
                    is DanmuSearchState.Success -> {
                        _uiState.update {
                            it.copy(
                                isDanmuSearching = false,
                                danmuSearchResults = state.animeList
                            )
                        }
                    }
                    is DanmuSearchState.Error -> {
                        _uiState.update { it.copy(isDanmuSearching = false) }
                    }
                    is DanmuSearchState.Idle -> {}
                }
            }
        }
    }

    /**
     * Select a danmu anime to load its episodes
     */
    fun selectDanmuAnime(anime: DanmuSearchAnime) {
        _uiState.update {
            it.copy(
                selectedDanmuAnime = anime,
                danmuEpisodeResults = emptyList()
            )
        }
        viewModelScope.launch {
            danmuRepository.getEpisodes(anime.animeId).collect { state ->
                when (state) {
                    is DanmuEpisodesState.Loading -> {
                        _uiState.update { it.copy(isDanmuLoadingEpisodes = true) }
                    }
                    is DanmuEpisodesState.Success -> {
                        _uiState.update {
                            it.copy(
                                isDanmuLoadingEpisodes = false,
                                danmuEpisodeResults = state.episodes
                            )
                        }
                    }
                    is DanmuEpisodesState.Error -> {
                        _uiState.update { it.copy(isDanmuLoadingEpisodes = false) }
                    }
                    is DanmuEpisodesState.Idle -> {}
                }
            }
        }
    }

    /**
     * Select a specific danmu episode
     */
    fun selectDanmuEpisode(episode: DanmuSearchEpisode) {
        val danmuUrl = danmuRepository.buildDanmuUrl(episode.episodeId)
        val state = _uiState.value
        playerStateHolder.updateUrl(state.url, state.headers, danmuUrl)
        _uiState.update {
            it.copy(
                danmuUrl = danmuUrl,
                showDanmuSearchDialog = false
            )
        }
    }

    /**
     * Clear current danmu
     */
    fun clearDanmu() {
        val state = _uiState.value
        playerStateHolder.updateUrl(state.url, state.headers, "")
        _uiState.update { it.copy(danmuUrl = "") }
    }

    /**
     * Go back to anime list in danmu search
     */
    fun backToDanmuAnimeList() {
        _uiState.update {
            it.copy(
                selectedDanmuAnime = null,
                danmuEpisodeResults = emptyList()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't clear state here as user might come back
    }
}
