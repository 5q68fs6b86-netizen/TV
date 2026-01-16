package com.fongmi.android.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.bean.Live
import com.fongmi.android.tv.data.repository.LiveConfigResult
import com.fongmi.android.tv.data.repository.LiveRepository
import com.fongmi.android.tv.impl.Callback
import com.fongmi.android.tv.ui.state.PlayerStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Live Screen
 */
data class LiveUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val selectedChannel: Channel? = null,
    val playUrl: String? = null,
    val error: String? = null
)

/**
 * ViewModel for Live Screen
 * Manages live TV data and channel selection
 */
@HiltViewModel
class LiveViewModel @Inject constructor(
    private val playerStateHolder: PlayerStateHolder,
    private val liveRepository: LiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        loadLive()
    }

    /**
     * Load live configuration
     */
    fun loadLive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Check if LiveConfig is already loaded
                val groups = liveRepository.getGroups()
                if (groups.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            groups = groups,
                            selectedGroup = groups.firstOrNull()
                        )
                    }
                } else {
                    // Load live config via Flow
                    liveRepository.loadLiveConfig().collect { result ->
                        when (result) {
                            is LiveConfigResult.Loading -> {
                                _uiState.update { it.copy(isLoading = true, error = null) }
                            }
                            is LiveConfigResult.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        groups = result.groups,
                                        selectedGroup = result.groups.firstOrNull()
                                    )
                                }
                            }
                            is LiveConfigResult.Error -> {
                                _uiState.update {
                                    it.copy(isLoading = false, error = result.message)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            }
        }
    }

    /**
     * Select a group
     */
    fun selectGroup(group: Group) {
        _uiState.update {
            it.copy(selectedGroup = group, selectedChannel = null)
        }
    }

    /**
     * Select a channel
     */
    fun selectChannel(channel: Channel) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }

    /**
     * Get channel URL for playback
     */
    fun getChannelUrl(channel: Channel): String {
        return liveRepository.getChannelUrl(channel)
    }

    /**
     * Prepare channel for playback (sets PlayerStateHolder)
     * Returns URL if valid, empty string otherwise
     */
    fun prepareChannelForPlayback(channel: Channel): String {
        val url = liveRepository.getChannelUrl(channel)
        if (url.isNotEmpty()) {
            val state = _uiState.value
            val groupIndex = state.groups.indexOf(state.selectedGroup).coerceAtLeast(0)
            val channelIndex = state.selectedGroup?.channel?.indexOf(channel)?.coerceAtLeast(0) ?: 0

            playerStateHolder.setLivePlayback(
                channelName = channel.name ?: "直播",
                url = url,
                headers = null,
                groups = state.groups,
                groupIndex = groupIndex,
                channelIndex = channelIndex
            )
        }
        return url
    }

    /**
     * Get channels for current selected group
     */
    fun getChannels(): List<Channel> {
        return _uiState.value.selectedGroup?.channel ?: emptyList()
    }

    /**
     * Get channel by number (1-indexed)
     */
    fun getChannelByNumber(number: Int): Channel? {
        return liveRepository.getChannelByNumber(number)
    }

    /**
     * Switch to next URL for current channel
     */
    fun switchChannelUrl(channel: Channel) {
        liveRepository.nextChannelUrl(channel)
    }

    /**
     * Refresh live data
     */
    fun refresh() {
        LiveConfig.get().clear()
        loadLive()
    }
}

