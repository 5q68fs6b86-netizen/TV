package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.data.repository.PlayUrlResult
import com.fongmi.android.tv.data.repository.VodRepository
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
    val currentChannelIndex: Int = 0
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
    private val vodRepository: VodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadFromStateHolder()
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

    override fun onCleared() {
        super.onCleared()
        // Don't clear state here as user might come back
    }
}
